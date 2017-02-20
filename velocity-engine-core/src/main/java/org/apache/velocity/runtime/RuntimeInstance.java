package org.apache.velocity.runtime;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.Template;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.EventHandler;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.Macro;
import org.apache.velocity.runtime.directive.Scope;
import org.apache.velocity.runtime.directive.StopCommand;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.ContentResource;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.ExtProperties;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.ChainableUberspector;
import org.apache.velocity.util.introspection.LinkingUberspector;
import org.apache.velocity.util.introspection.Uberspect;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * This is the Runtime system for Velocity. It is the
 * single access point for all functionality in Velocity.
 * It adheres to the mediator pattern and is the only
 * structure that developers need to be familiar with
 * in order to get Velocity to perform.
 *
 * The Runtime will also cooperate with external
 * systems, which can make all needed setProperty() calls
 * before calling init().
 *
 * -----------------------------------------------------------------------
 * N O T E S  O N  R U N T I M E  I N I T I A L I Z A T I O N
 * -----------------------------------------------------------------------
 * init()
 *
 * If init() is called by itself the RuntimeInstance will initialize
 * with a set of default values.
 * -----------------------------------------------------------------------
 * init(String/Properties)
 *
 * In this case the default velocity properties are layed down
 * first to provide a solid base, then any properties provided
 * in the given properties object will override the corresponding
 * default property.
 * -----------------------------------------------------------------------
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jlb@houseofdistraction.com">Jeff Bowden</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magusson Jr.</a>
 * @version $Id$
 */
public class RuntimeInstance implements RuntimeConstants, RuntimeServices
{
    /**
     *  VelocimacroFactory object to manage VMs
     */
    private  VelocimacroFactory vmFactory = null;

    /**
     * The Runtime logger.  The default instance is the "org.apache.velocity" logger.
     */
    private Logger log = LoggerFactory.getLogger(DEFAULT_RUNTIME_LOG_NAME);

    /**
     * The Runtime parser pool
     */
    private  ParserPool parserPool;

    /**
     * Indicate whether the Runtime is in the midst of initialization.
     */
    private boolean initializing = false;

    /**
     * Indicate whether the Runtime has been fully initialized.
     */
    private volatile boolean initialized = false;

    /**
     * These are the properties that are laid down over top
     * of the default properties when requested.
     */
    private  ExtProperties overridingProperties = null;

    /**
     * This is a hashtable of initialized directives.
     * The directives that populate this hashtable are
     * taken from the RUNTIME_DEFAULT_DIRECTIVES
     * property file.
     */
    private Map runtimeDirectives = new Hashtable();
    /**
     * Copy of the actual runtimeDirectives that is shared between
     * parsers. Whenever directives are updated, the synchronized
     * runtimeDirectives is first updated and then an unsynchronized
     * copy of it is passed to parsers.
     */
    private Map runtimeDirectivesShared;

    /**
     * Object that houses the configuration options for
     * the velocity runtime. The ExtProperties object allows
     * the convenient retrieval of a subset of properties.
     * For example all the properties for a resource loader
     * can be retrieved from the main ExtProperties object
     * using something like the following:
     *
     * ExtProperties loaderConfiguration =
     *         configuration.subset(loaderID);
     *
     * And a configuration is a lot more convenient to deal
     * with then conventional properties objects, or Maps.
     */
    private  ExtProperties configuration = new ExtProperties();

    private ResourceManager resourceManager = null;

    /**
     * This stores the engine-wide set of event handlers.  Event handlers for
     * each specific merge are stored in the context.
     */
    private EventCartridge eventCartridge = null;

    /*
     * Whether to use string interning
     */
    private boolean stringInterning = false;

    /*
     * Settings for provision of root scope for evaluate(...) calls.
     */
    private String evaluateScopeName = "evaluate";
    private boolean provideEvaluateScope = false;

    /*
     *  Opaque reference to something specified by the
     *  application for use in application supplied/specified
     *  pluggable components
     */
    private Map applicationAttributes = null;
    private Uberspect uberSpect;
    private String defaultEncoding;

    /*
     * Space gobbling mode
     */
    private SpaceGobbling spaceGobbling;

    /**
     * Creates a new RuntimeInstance object.
     */
    public RuntimeInstance()
    {
        reset();
    }

    /**
     * This is the primary initialization method in the Velocity
     * Runtime. The systems that are setup/initialized here are
     * as follows:
     *
     * <ul>
     *   <li>Logging System</li>
     *   <li>ResourceManager</li>
     *   <li>EventHandler</li>
     *   <li>Parser Pool</li>
     *   <li>Global Cache</li>
     *   <li>Static Content Include System</li>
     *   <li>Velocimacro System</li>
     * </ul>
     */
    public synchronized void init()
    {
        if (!initialized && !initializing)
        {
            try
            {
                log.debug("Initializing Velocity, Calling init()...");
                initializing = true;

                log.trace("*****************************");
                log.debug("Starting Apache Velocity v2.0");
                log.trace("RuntimeInstance initializing.");

                initializeProperties();
                initializeSelfProperties();
                initializeLog();
                initializeResourceManager();
                initializeDirectives();
                initializeEventHandlers();
                initializeParserPool();

                initializeIntrospection();
                initializeEvaluateScopeSettings();
                /*
                 *  initialize the VM Factory.  It will use the properties
                 * accessible from Runtime, so keep this here at the end.
                 */
                vmFactory.initVelocimacro();

                log.trace("RuntimeInstance successfully initialized.");

                initialized = true;
                initializing = false;
            }
            catch(RuntimeException re)
            {
                // initialization failed at some point... try to reset everything
                try
                {
                    reset();
                }
                catch(RuntimeException re2) {} // prefer throwing the original exception
                throw re;
            }
            finally
            {
                initializing = false;
            }
        }
    }

    /**
     * Resets the instance, so Velocity can be re-initialized again.
     *
     * @since 2.0.0
     */
    public synchronized void reset()
    {
        this.configuration = new ExtProperties();
        this.defaultEncoding = null;
        this.evaluateScopeName = "evaluate";
        this.eventCartridge = null;
        this.initialized = false;
        this.initializing = false;
        this.overridingProperties = null;
        this.parserPool = null;
        this.provideEvaluateScope = false;
        this.resourceManager = null;
        this.runtimeDirectives = new Hashtable();
        this.runtimeDirectivesShared = null;
        this.uberSpect = null;
        this.stringInterning = false;

        /*
         *  create a VM factory, introspector, and application attributes
         */
        vmFactory = new VelocimacroFactory( this );

        /*
         * and a store for the application attributes
         */
        applicationAttributes = new HashMap();
    }

    /**
     * Returns true if the RuntimeInstance has been successfully initialized.
     * @return True if the RuntimeInstance has been successfully initialized.
     * @since 1.5
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Init or die! (with some log help, of course)
     */
    private void requireInitialization()
    {
        if (!initialized)
        {
            try
            {
                init();
            }
            catch (Exception e)
            {
                log.error("Could not auto-initialize Velocity", e);
                throw new RuntimeException("Velocity could not be initialized!", e);
            }
        }
    }

    /**
     *  Initialize runtime internal properties
     */
    private void initializeSelfProperties()
    {
        /* initialize string interning (defaults to false) */
        stringInterning = getBoolean(RUNTIME_STRING_INTERNING, true);

        /* initialize indentation mode (defaults to 'lines') */
        String im = getString(SPACE_GOBBLING, "lines");
        try
        {
            spaceGobbling = SpaceGobbling.valueOf(im.toUpperCase());
        }
        catch (NoSuchElementException nse)
        {
            spaceGobbling = SpaceGobbling.LINES;
        }
    }

    /**
     *  Gets the classname for the Uberspect introspection package and
     *  instantiates an instance.
     */
    private void initializeIntrospection()
    {
        String[] uberspectors = configuration.getStringArray(RuntimeConstants.UBERSPECT_CLASSNAME);
        for (String rm : uberspectors)
        {
            Object o = null;

            try
            {
                o = ClassUtils.getNewInstance(rm);
            }
            catch (ClassNotFoundException cnfe)
            {
                String err = "The specified class for Uberspect (" + rm
                    + ") does not exist or is not accessible to the current classloader.";
                log.error(err);
                throw new VelocityException(err, cnfe);
            }
            catch (InstantiationException ie)
            {
                throw new VelocityException("Could not instantiate class '" + rm + "'", ie);
            }
            catch (IllegalAccessException ae)
            {
                throw new VelocityException("Cannot access class '" + rm + "'", ae);
            }

            if (!(o instanceof Uberspect))
            {
                String err = "The specified class for Uberspect ("
                    + rm + ") does not implement " + Uberspect.class.getName()
                    + "; Velocity is not initialized correctly.";

                log.error(err);
                throw new VelocityException(err);
            }

            Uberspect u = (Uberspect) o;

            if (u instanceof RuntimeServicesAware)
            {
                ((RuntimeServicesAware) u).setRuntimeServices(this);
            }

            if (uberSpect == null)
            {
                uberSpect = u;
            } else
            {
                if (u instanceof ChainableUberspector)
                {
                    ((ChainableUberspector) u).wrap(uberSpect);
                    uberSpect = u;
                } else
                {
                    uberSpect = new LinkingUberspector(uberSpect, u);
                }
            }
        }

        if(uberSpect != null)
        {
            uberSpect.init();
        }
        else
        {
           /*
            *  someone screwed up.  Lets not fool around...
            */

           String err = "It appears that no class was specified as the"
           + " Uberspect.  Please ensure that all configuration"
           + " information is correct.";

           log.error(err);
           throw new VelocityException(err);
        }
    }

    /**
     * Initializes the Velocity Runtime with properties file.
     * The properties file may be in the file system proper,
     * or the properties file may be in the classpath.
     */
    private void setDefaultProperties()
    {
        InputStream inputStream = null;
        try
        {
            inputStream = getClass().getClassLoader()
                .getResourceAsStream(DEFAULT_RUNTIME_PROPERTIES);

            if (inputStream == null)
                throw new IOException("Resource not found: " + DEFAULT_RUNTIME_PROPERTIES);

            configuration.load( inputStream );

            /* populate 'defaultEncoding' member */
            defaultEncoding = getString(INPUT_ENCODING, ENCODING_DEFAULT);

            log.debug("Default Properties resource: {}", DEFAULT_RUNTIME_PROPERTIES);
        }
        catch (IOException ioe)
        {
            String msg = "Cannot get Velocity Runtime default properties!";
            log.error(msg, ioe);
            throw new RuntimeException(msg, ioe);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                String msg = "Cannot close Velocity Runtime default properties!";
                log.error(msg, ioe);
                throw new RuntimeException(msg, ioe);
            }
        }
    }

    /**
     * Allows an external system to set a property in
     * the Velocity Runtime.
     *
     * @param key property key
     * @param  value property value
     */
    public void setProperty(String key, Object value)
    {
        if (overridingProperties == null)
        {
            overridingProperties = new ExtProperties();
        }

        overridingProperties.setProperty(key, value);
    }


    /**
     * Add all properties contained in the file fileName to the RuntimeInstance properties
     */
    public void setProperties(String fileName)
    {
        ExtProperties props = null;
        try
        {
              props = new ExtProperties(fileName);
        }
        catch (IOException e)
        {
              throw new VelocityException("Error reading properties from '"
                + fileName + "'", e);
        }

        Enumeration en = props.keys();
        while (en.hasMoreElements())
        {
            String key = en.nextElement().toString();
            setProperty(key, props.get(key));
        }
    }


    /**
     * Add all the properties in props to the RuntimeInstance properties
     */
    public void setProperties(Properties props)
    {
        Enumeration en = props.keys();
        while (en.hasMoreElements())
        {
            String key = en.nextElement().toString();
            setProperty(key, props.get(key));
        }
    }

    /**
     * Allow an external system to set an ExtProperties
     * object to use.
     *
     * @param  configuration
     * @since 2.0
     */
    public void setConfiguration( ExtProperties configuration)
    {
        if (overridingProperties == null)
        {
            overridingProperties = configuration;
        }
        else
        {
            // Avoid possible ConcurrentModificationException
            if (overridingProperties != configuration)
            {
                overridingProperties.combine(configuration);
            }
        }
    }

    /**
     * Add a property to the configuration. If it already
     * exists then the value stated here will be added
     * to the configuration entry. For example, if
     *
     * resource.loader = file
     *
     * is already present in the configuration and you
     *
     * addProperty("resource.loader", "classpath")
     *
     * Then you will end up with a Vector like the
     * following:
     *
     * ["file", "classpath"]
     *
     * @param  key
     * @param  value
     */
    public void addProperty(String key, Object value)
    {
        if (overridingProperties == null)
        {
            overridingProperties = new ExtProperties();
        }

        overridingProperties.addProperty(key, value);
    }

    /**
     * Clear the values pertaining to a particular
     * property.
     *
     * @param key of property to clear
     */
    public void clearProperty(String key)
    {
        if (overridingProperties != null)
        {
            overridingProperties.clearProperty(key);
        }
    }

    /**
     *  Allows an external caller to get a property.  The calling
     *  routine is required to know the type, as this routine
     *  will return an Object, as that is what properties can be.
     *
     *  @param key property to return
     *  @return Value of the property or null if it does not exist.
     */
    public Object getProperty(String key)
    {
        Object o = null;

        /**
         * Before initialization, check the user-entered properties first.
         */
        if (!initialized && overridingProperties != null)
        {
            o = overridingProperties.get(key);
        }

        /**
         * After initialization, configuration will hold all properties.
         */
        if (o == null)
        {
            o = configuration.getProperty(key);
        }
        if (o instanceof String)
        {
            return StringUtils.trim((String) o);
        }
        else
        {
            return o;
        }
    }

    /**
     * Initialize Velocity properties, if the default
     * properties have not been laid down first then
     * do so. Then proceed to process any overriding
     * properties. Laying down the default properties
     * gives a much greater chance of having a
     * working system.
     */
    private void initializeProperties()
    {
        /*
         * Always lay down the default properties first as
         * to provide a solid base.
         */
        if (configuration.isInitialized() == false)
        {
            setDefaultProperties();
        }

        if( overridingProperties != null)
        {
            configuration.combine(overridingProperties);
        }
    }

    /**
     * Initialize the Velocity Runtime with a Properties
     * object.
     *
     * @param p Velocity properties for initialization
     */
    public void init(Properties p)
    {
        setConfiguration(ExtProperties.convertProperties(p));
        init();
    }

    /**
     * Initialize the Velocity Runtime with a
     * properties file path.
     *
     * @param configurationFile
     */
    public void init(String configurationFile)
    {
        try
        {
            setConfiguration(new ExtProperties(configurationFile));
        }
        catch (IOException e)
        {
            throw new VelocityException("Error reading properties from '"
                + configurationFile + "'", e);
        }
        init();
    }

    private void initializeResourceManager()
    {
        /*
         * Which resource manager?
         */
        Object inst = getProperty(RuntimeConstants.RESOURCE_MANAGER_INSTANCE);
        String rm = getString(RuntimeConstants.RESOURCE_MANAGER_CLASS);

        if (inst != null)
        {
            if (ResourceManager.class.isAssignableFrom(inst.getClass()))
            {
                resourceManager = (ResourceManager)inst;
                resourceManager.initialize(this);
            }
            else
            {
                String msg = inst.getClass().getName() + " object set as resource.manager.instance is not a valid org.apache.velocity.runtime.resource.ResourceManager.";
                log.error(msg);
                throw new VelocityException(msg);
            }
        }
        else if (rm != null && rm.length() > 0)
        {
            /*
             *  if something was specified, then make one.
             *  if that isn't a ResourceManager, consider
             *  this a huge error and throw
             */

            Object o = null;

            try
            {
               o = ClassUtils.getNewInstance( rm );
            }
            catch (ClassNotFoundException cnfe )
            {
                String err = "The specified class for ResourceManager (" + rm
                    + ") does not exist or is not accessible to the current classloader.";
                log.error(err);
                throw new VelocityException(err, cnfe);
            }
            catch (InstantiationException ie)
            {
              throw new VelocityException("Could not instantiate class '" + rm + "'", ie);
            }
            catch (IllegalAccessException ae)
            {
              throw new VelocityException("Cannot access class '" + rm + "'", ae);
            }

            if (!(o instanceof ResourceManager))
            {
                String err = "The specified class for ResourceManager (" + rm
                    + ") does not implement " + ResourceManager.class.getName()
                    + "; Velocity is not initialized correctly.";

                log.error(err);
                throw new VelocityException(err);
            }

            resourceManager = (ResourceManager) o;
            resourceManager.initialize(this);
            setProperty(RESOURCE_MANAGER_INSTANCE, resourceManager);
         }
         else
         {
            /*
             *  someone screwed up.  Lets not fool around...
             */

            String err = "It appears that no class or instance was specified as the"
            + " ResourceManager.  Please ensure that all configuration"
            + " information is correct.";

            log.error(err);
            throw new VelocityException( err );
        }
    }

    private void initializeEventHandlers()
    {

        eventCartridge = new EventCartridge();
        eventCartridge.setRuntimeServices(this);

        /**
         * For each type of event handler, get the class name, instantiate it, and store it.
         */

        String[] referenceinsertion = configuration.getStringArray(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION);
        if ( referenceinsertion != null )
        {
            for (String aReferenceinsertion : referenceinsertion)
            {
                EventHandler ev = initializeSpecificEventHandler(aReferenceinsertion, RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, ReferenceInsertionEventHandler.class);
                if (ev != null)
                    eventCartridge.addReferenceInsertionEventHandler((ReferenceInsertionEventHandler) ev);
            }
        }

        String[] methodexception = configuration.getStringArray(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION);
        if ( methodexception != null )
        {
            for (String aMethodexception : methodexception)
            {
                EventHandler ev = initializeSpecificEventHandler(aMethodexception, RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, MethodExceptionEventHandler.class);
                if (ev != null)
                    eventCartridge.addMethodExceptionHandler((MethodExceptionEventHandler) ev);
            }
        }

        String[] includeHandler = configuration.getStringArray(RuntimeConstants.EVENTHANDLER_INCLUDE);
        if ( includeHandler != null )
        {
            for (String anIncludeHandler : includeHandler)
            {
                EventHandler ev = initializeSpecificEventHandler(anIncludeHandler, RuntimeConstants.EVENTHANDLER_INCLUDE, IncludeEventHandler.class);
                if (ev != null)
                    eventCartridge.addIncludeEventHandler((IncludeEventHandler) ev);
            }
        }

        String[] invalidReferenceSet = configuration.getStringArray(RuntimeConstants.EVENTHANDLER_INVALIDREFERENCES);
        if ( invalidReferenceSet != null )
        {
            for (String anInvalidReferenceSet : invalidReferenceSet)
            {
                EventHandler ev = initializeSpecificEventHandler(anInvalidReferenceSet, RuntimeConstants.EVENTHANDLER_INVALIDREFERENCES, InvalidReferenceEventHandler.class);
                if (ev != null)
                {
                    eventCartridge.addInvalidReferenceEventHandler((InvalidReferenceEventHandler) ev);
                }
            }
        }


    }

    private EventHandler initializeSpecificEventHandler(String classname, String paramName, Class EventHandlerInterface)
    {
        if ( classname != null && classname.length() > 0)
        {
            Object o = null;
            try
            {
                o = ClassUtils.getNewInstance(classname);
            }
            catch (ClassNotFoundException cnfe )
            {
                String err = "The specified class for "
                    + paramName + " (" + classname
                    + ") does not exist or is not accessible to the current classloader.";
                log.error(err);
                throw new VelocityException(err, cnfe);
            }
            catch (InstantiationException ie)
            {
              throw new VelocityException("Could not instantiate class '" + classname + "'", ie);
            }
            catch (IllegalAccessException ae)
            {
              throw new VelocityException("Cannot access class '" + classname + "'", ae);
            }

            if (!EventHandlerInterface.isAssignableFrom(EventHandlerInterface))
            {
                String err = "The specified class for " + paramName + " ("
                    + classname + ") does not implement "
                    + EventHandlerInterface.getName()
                    + "; Velocity is not initialized correctly.";

                log.error(err);
                throw new VelocityException(err);
            }

            EventHandler ev = (EventHandler) o;
            if ( ev instanceof RuntimeServicesAware )
                ((RuntimeServicesAware) ev).setRuntimeServices(this);
            return ev;

        } else
            return null;
    }

    /**
     * Initialize the Velocity logging system.
     */
    private void initializeLog()
    {
        // if we were provided a specific logger or logger name, let's use it
        try
        {
            /* If a Logger instance was set as a configuration
             * value, use that.  This is any class the user specifies.
             */
            Object o = getProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE);
            if (o != null)
            {
                // check for a Logger
                if (Logger.class.isAssignableFrom(o.getClass()))
                {
                    //looks ok
                    log = (Logger)o;
                }
                else
                {
                    String msg = o.getClass().getName() + " object set as runtime.log.instance is not a valid org.slf4j.Logger implementation.";
                    log.error(msg);
                    throw new VelocityException(msg);
                }
            }
            else
            {
                /* otherwise, see if a logger name was specified.
                 */
                o = getProperty(RuntimeConstants.RUNTIME_LOG_NAME);
                if (o != null)
                {
                    if (o instanceof String)
                    {
                        log = LoggerFactory.getLogger((String)o);
                    }
                    else
                    {
                        String msg = o.getClass().getName() + " object set as runtime.log.name is not a valid string.";
                        log.error(msg);
                        throw new VelocityException(msg);
                    }
                }
            }
            /* else keep our default Velocity logger
             */
        }
        catch (Exception e)
        {
            throw new VelocityException("Error initializing log: " + e.getMessage(), e);
        }
    }


    /**
     * This methods initializes all the directives
     * that are used by the Velocity Runtime. The
     * directives to be initialized are listed in
     * the RUNTIME_DEFAULT_DIRECTIVES properties
     * file.
     */
    private void initializeDirectives()
    {
        Properties directiveProperties = new Properties();

        /*
         * Grab the properties file with the list of directives
         * that we should initialize.
         */

        InputStream inputStream = null;

        try
        {
            inputStream = getClass().getResourceAsStream('/' + DEFAULT_RUNTIME_DIRECTIVES);

            if (inputStream == null)
            {
                throw new VelocityException("Error loading directive.properties! " +
                                    "Something is very wrong if these properties " +
                                    "aren't being located. Either your Velocity " +
                                    "distribution is incomplete or your Velocity " +
                                    "jar file is corrupted!");
            }

            directiveProperties.load(inputStream);

        }
        catch (IOException ioe)
        {
            String msg = "Error while loading directive properties!";
            log.error(msg, ioe);
            throw new RuntimeException(msg, ioe);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException ioe)
            {
                String msg = "Cannot close directive properties!";
                log.error(msg, ioe);
                throw new RuntimeException(msg, ioe);
            }
        }


        /*
         * Grab all the values of the properties. These
         * are all class names for example:
         *
         * org.apache.velocity.runtime.directive.Foreach
         */
        Enumeration directiveClasses = directiveProperties.elements();

        while (directiveClasses.hasMoreElements())
        {
            String directiveClass = (String) directiveClasses.nextElement();
            loadDirective(directiveClass);
            log.debug("Loaded System Directive: {}", directiveClass);
        }

        /*
         *  now the user's directives
         */

        String[] userdirective = configuration.getStringArray("userdirective");

        for (String anUserdirective : userdirective)
        {
            loadDirective(anUserdirective);
            log.debug("Loaded User Directive: {}", anUserdirective);
        }

    }

    /**
     * Programatically add a directive.
     * @param directive
     */
    public synchronized void addDirective(Directive directive)
    {
        runtimeDirectives.put(directive.getName(), directive);
        updateSharedDirectivesMap();
    }

    /**
     * Retrieve a previously instantiated directive.
     * @param name name of the directive
     * @return the {@link Directive} for that name
     */
    public Directive getDirective(String name)
    {
        return (Directive) runtimeDirectivesShared.get(name);
    }

    /**
     * Remove a directive.
     * @param name name of the directive.
     */
    public synchronized void removeDirective(String name)
    {
        runtimeDirectives.remove(name);
        updateSharedDirectivesMap();
    }

    /**
     * Makes an unsynchronized copy of the directives map
     * that is used for Directive lookups by all parsers.
     *
     * This follows Copy-on-Write pattern. The cost of creating
     * a new map is acceptable since directives are typically
     * set and modified only during Velocity setup phase.
     */
    private void updateSharedDirectivesMap()
    {
        runtimeDirectivesShared = new HashMap(runtimeDirectives);
    }

    /**
     *  instantiates and loads the directive with some basic checks
     *
     *  @param directiveClass classname of directive to load
     */
    public void loadDirective(String directiveClass)
    {
        try
        {
            Object o = ClassUtils.getNewInstance( directiveClass );

            if (o instanceof Directive)
            {
                Directive directive = (Directive) o;
                addDirective(directive);
            }
            else
            {
                String msg = directiveClass + " does not implement "
                    + Directive.class.getName() + "; it cannot be loaded.";
                log.error(msg);
                throw new VelocityException(msg);
            }
        }
        // The ugly threesome:  ClassNotFoundException,
        // IllegalAccessException, InstantiationException.
        // Ignore Findbugs complaint for now.
        catch (Exception e)
        {
            String msg = "Failed to load Directive: " + directiveClass;
            log.error(msg, e);
            throw new VelocityException(msg, e);
        }
    }


    /**
     * Initializes the Velocity parser pool.
     */
    private void initializeParserPool()
    {
        /*
         * Which parser pool?
         */
        String pp = getString(RuntimeConstants.PARSER_POOL_CLASS);

        if (pp != null && pp.length() > 0)
        {
            /*
             *  if something was specified, then make one.
             *  if that isn't a ParserPool, consider
             *  this a huge error and throw
             */

            Object o = null;

            try
            {
                o = ClassUtils.getNewInstance( pp );
            }
            catch (ClassNotFoundException cnfe )
            {
                String err = "The specified class for ParserPool ("
                    + pp
                    + ") does not exist (or is not accessible to the current classloader.";
                log.error(err);
                throw new VelocityException(err, cnfe);
            }
            catch (InstantiationException ie)
            {
              throw new VelocityException("Could not instantiate class '" + pp + "'", ie);
            }
            catch (IllegalAccessException ae)
            {
              throw new VelocityException("Cannot access class '" + pp + "'", ae);
            }

            if (!(o instanceof ParserPool))
            {
                String err = "The specified class for ParserPool ("
                    + pp + ") does not implement " + ParserPool.class
                    + " Velocity not initialized correctly.";

                log.error(err);
                throw new VelocityException(err);
            }

            parserPool = (ParserPool) o;

            parserPool.initialize(this);
        }
        else
        {
            /*
             *  someone screwed up.  Lets not fool around...
             */

            String err = "It appears that no class was specified as the"
                + " ParserPool.  Please ensure that all configuration"
                + " information is correct.";

            log.error(err);
            throw new VelocityException( err );
        }

    }

    /**
     * Returns a JavaCC generated Parser.
     *
     * @return Parser javacc generated parser
     */
    public Parser createNewParser()
    {
        requireInitialization();

        return new Parser(this);
    }

    /**
     * Parse the input and return the root of
     * AST node structure.
     * <br><br>
     *  In the event that it runs out of parsers in the
     *  pool, it will create and let them be GC'd
     *  dynamically, logging that it has to do that.  This
     *  is considered an exceptional condition.  It is
     *  expected that the user will set the
     *  PARSER_POOL_SIZE property appropriately for their
     *  application.  We will revisit this.
     *
     * @param reader Reader retrieved by a resource loader
     * @param template template being parsed
     * @return A root node representing the template as an AST tree.
     * @throws ParseException When the template could not be parsed.
     */
    public SimpleNode parse(Reader reader, Template template)
        throws ParseException
    {
        requireInitialization();

        Parser parser = parserPool.get();
        boolean keepParser = true;
        if (parser == null)
        {
            /*
             *  if we couldn't get a parser from the pool make one and log it.
             */
            log.info("Runtime: ran out of parsers. Creating a new one. "
                     + " Please increment the parser.pool.size property."
                     + " The current value is too small.");
            parser = createNewParser();
            keepParser = false;
        }

        try
        {
            return parser.parse(reader, template);
        }
        finally
        {
            if (keepParser)
            {
                /* drop the parser Template reference to allow garbage collection */
                parser.currentTemplate = null;
                parserPool.put(parser);
            }

        }
    }

    private void initializeEvaluateScopeSettings()
    {
        String property = evaluateScopeName+'.'+PROVIDE_SCOPE_CONTROL;
        provideEvaluateScope = getBoolean(property, provideEvaluateScope);
    }

    /**
     * Renders the input string using the context into the output writer.
     * To be used when a template is dynamically constructed, or want to use
     * Velocity as a token replacer.
     * <br>
     * Note! Macros defined in evaluate() calls are not persisted in memory so next evaluate() call
     * does not know about macros defined during previous calls.
     *
     * @param context context to use in rendering input string
     * @param out  Writer in which to render the output
     * @param logTag  string to be used as the template name for log
     *                messages in case of error
     * @param instring input string containing the VTL to be rendered
     *
     * @return true if successful, false otherwise.  If false, see
     *              Velocity runtime log
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @since Velocity 1.6
     */
    public boolean evaluate(Context context,  Writer out,
                            String logTag, String instring)
    {
        return evaluate(context, out, logTag, new StringReader(instring));
    }

    /**
     * Renders the input reader using the context into the output writer.
     * To be used when a template is dynamically constructed, or want to
     * use Velocity as a token replacer.
     * <br>
     * Note! Macros defined in evaluate() calls are not persisted in memory so next evaluate() call
     * does not know about macros defined during previous calls.
     *
     * @param context context to use in rendering input string
     * @param writer  Writer in which to render the output
     * @param logTag  string to be used as the template name for log messages
     *                in case of error
     * @param reader Reader containing the VTL to be rendered
     *
     * @return true if successful, false otherwise.  If false, see
     *              Velocity runtime log
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @since Velocity 1.6
     */
    public boolean evaluate(Context context, Writer writer,
                            String logTag, Reader reader)
    {
        if (logTag == null)
        {
            throw new NullPointerException("logTag (i.e. template name) cannot be null, you must provide an identifier for the content being evaluated");
        }

        SimpleNode nodeTree = null;
        Template t = new Template();
        t.setName(logTag);
        try
        {
            nodeTree = parse(reader, t);
        }
        catch (ParseException pex)
        {
            throw new ParseErrorException(pex, null);
        }
        catch (TemplateInitException pex)
        {
            throw new ParseErrorException(pex, null);
        }

        if (nodeTree == null)
        {
            return false;
        }
        else
        {
            return render(context, writer, logTag, nodeTree);
        }
    }


    /**
     * Initializes and renders the AST {@link SimpleNode} using the context
     * into the output writer.
     *
     * @param context context to use in rendering input string
     * @param writer  Writer in which to render the output
     * @param logTag  string to be used as the template name for log messages
     *                in case of error
     * @param nodeTree SimpleNode which is the root of the AST to be rendered
     *
     * @return true if successful, false otherwise.  If false, see
     *              Velocity runtime log for errors
     * @throws ParseErrorException The template could not be parsed.
     * @throws MethodInvocationException A method on a context object could not be invoked.
     * @throws ResourceNotFoundException A referenced resource could not be loaded.
     * @since Velocity 1.6
     */
    public boolean render(Context context, Writer writer,
                          String logTag, SimpleNode nodeTree)
    {
        /*
         * we want to init then render
         */
        InternalContextAdapterImpl ica =
            new InternalContextAdapterImpl(context);

        ica.pushCurrentTemplateName(logTag);

        try
        {
            try
            {
                nodeTree.init(ica, this);
            }
            catch (TemplateInitException pex)
            {
                throw new ParseErrorException(pex, null);
            }
            /**
             * pass through application level runtime exceptions
             */
            catch(RuntimeException e)
            {
                throw e;
            }
            catch(Exception e)
            {
                String msg = "RuntimeInstance.render(): init exception for tag = "+logTag;
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }

            try
            {
                if (provideEvaluateScope)
                {
                    Object previous = ica.get(evaluateScopeName);
                    context.put(evaluateScopeName, new Scope(this, previous));
                }
                /**
                 * optionally put the context in itself if asked so
                 */
                String self = getString(CONTEXT_AUTOREFERENCE_KEY);
                if (self != null) context.put(self, context);
                nodeTree.render(ica, writer);
            }
            catch (StopCommand stop)
            {
                if (!stop.isFor(this))
                {
                    throw stop;
                }
                else
                {
                    log.debug(stop.getMessage());
                }
            }
            catch (IOException e)
            {
                throw new VelocityException("IO Error in writer: " + e.getMessage(), e);
            }
        }
        finally
        {
            ica.popCurrentTemplateName();
            if (provideEvaluateScope)
            {
                Object obj = ica.get(evaluateScopeName);
                if (obj instanceof Scope)
                {
                    Scope scope = (Scope)obj;
                    if (scope.getParent() != null)
                    {
                        ica.put(evaluateScopeName, scope.getParent());
                    }
                    else if (scope.getReplaced() != null)
                    {
                        ica.put(evaluateScopeName, scope.getReplaced());
                    }
                    else
                    {
                        ica.remove(evaluateScopeName);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Invokes a currently registered Velocimacro with the params provided
     * and places the rendered stream into the writer.
     * <br>
     * Note: currently only accepts args to the VM if they are in the context.
     * <br>
     * Note: only macros in the global context can be called. This method doesn't find macros defined by
     * templates during previous mergeTemplate calls if Velocity.VM_PERM_INLINE_LOCAL has been enabled.
     *
     * @param vmName name of Velocimacro to call
     * @param logTag string to be used for template name in case of error. if null,
     *               the vmName will be used
     * @param params keys for args used to invoke Velocimacro, in java format
     *               rather than VTL (eg  "foo" or "bar" rather than "$foo" or "$bar")
     * @param context Context object containing data/objects used for rendering.
     * @param writer  Writer for output stream
     * @return true if Velocimacro exists and successfully invoked, false otherwise.
     * @since 1.6
     */
    public boolean invokeVelocimacro(final String vmName, String logTag,
                                     String[] params, final Context context,
                                     final Writer writer)
     {
        /* check necessary parameters */
        if (vmName == null || context == null || writer == null)
        {
            String msg = "RuntimeInstance.invokeVelocimacro(): invalid call: vmName, context, and writer must not be null";
            log.error(msg);
            throw new NullPointerException(msg);
        }

        /* handle easily corrected parameters */
        if (logTag == null)
        {
            logTag = vmName;
        }
        if (params == null)
        {
            params = new String[0];
        }

        /* does the VM exist? (only global scope is scanned so this doesn't find inline macros in templates) */
        if (!isVelocimacro(vmName, null))
        {
            String msg = "RuntimeInstance.invokeVelocimacro(): VM '" + vmName
                         + "' is not registered.";
            log.error(msg);
            throw new VelocityException(msg);
        }

        /* now just create the VM call, and use evaluate */
        StringBuilder template = new StringBuilder("#");
        template.append(vmName);
        template.append("(");
         for (String param : params)
         {
             template.append(" $");
             template.append(param);
         }
        template.append(" )");

        return evaluate(context, writer, logTag, template.toString());
    }

    /**
     * Retrieves and caches the configured default encoding
     * for better performance. (VELOCITY-606)
     */
    private String getDefaultEncoding()
    {
        return defaultEncoding;
    }

    /**
     * Returns a <code>Template</code> from the resource manager.
     * This method assumes that the character encoding of the
     * template is set by the <code>input.encoding</code>
     * property. The default is UTF-8.
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException
    {
        return getTemplate(name, null);
    }

    /**
     * Returns a <code>Template</code> from the resource manager
     *
     * @param name The  name of the desired template.
     * @param encoding Character encoding of the template
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     */
    public Template getTemplate(String name, String  encoding)
        throws ResourceNotFoundException, ParseErrorException
    {
        requireInitialization();
        if (encoding == null) encoding = getDefaultEncoding();
        return (Template)
                resourceManager.getResource(name,
                    ResourceManager.RESOURCE_TEMPLATE, encoding);
    }

    /**
     * Returns a static content resource from the
     * resource manager.  Uses the current value
     * if INPUT_ENCODING as the character encoding.
     *
     * @param name Name of content resource to get
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException When the template could not be parsed.
     */
    public ContentResource getContent(String name)
        throws ResourceNotFoundException, ParseErrorException
    {
        /*
         *  the encoding is irrelvant as we don't do any converstion
         *  the bytestream should be dumped to the output stream
         */

        return getContent(name, getDefaultEncoding());
    }

    /**
     * Returns a static content resource from the
     * resource manager.
     *
     * @param name Name of content resource to get
     * @param encoding Character encoding to use
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException When the template could not be parsed.
     */
    public ContentResource getContent(String name, String encoding)
        throws ResourceNotFoundException, ParseErrorException
    {
        requireInitialization();

        return (ContentResource)
                resourceManager.getResource(name,
                        ResourceManager.RESOURCE_CONTENT, encoding);
    }


    /**
     *  Determines if a template exists and returns name of the loader that
     *  provides it.  This is a slightly less hokey way to support
     *  the Velocity.resourceExists() utility method, which was broken
     *  when per-template encoding was introduced.  We can revisit this.
     *
     *  @param resourceName Name of template or content resource
     *  @return class name of loader than can provide it
     */
    public String getLoaderNameForResource(String resourceName)
    {
        requireInitialization();

        return resourceManager.getLoaderNameForResource(resourceName);
    }

    /**
     * Returns a convenient Log instance that wraps the current LogChute.
     * Use this to log error messages. It has the usual methods.
     *
     * @return A convenience Log instance that wraps the current LogChute.
     * @since 1.5
     */
    public Logger getLog()
    {
        return log;
    }

    /**
     * Get a logger for the specified child namespace.
     * If a logger was configured using the runtime.log.instance configuration property, returns this instance.
     * Otherwise, uses SLF4J LoggerFactory on baseNamespace + childNamespace.
     * @param childNamespace
     * @return
     */
    public Logger getLog(String childNamespace)
    {
        Logger log = (Logger)getProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE);
        if (log == null)
        {
            String loggerName = getString(RUNTIME_LOG_NAME, DEFAULT_RUNTIME_LOG_NAME) + "." + childNamespace;
            log = LoggerFactory.getLogger(loggerName);
        }
        return log;
    }

    /**
     * String property accessor method with default to hide the
     * configuration implementation.
     *
     * @param key property key
     * @param defaultValue  default value to return if key not
     *               found in resource manager.
     * @return value of key or default
     */
    public String getString( String key, String defaultValue)
    {
        return configuration.getString(key, defaultValue);
    }

    /**
     * Returns the appropriate VelocimacroProxy object if vmName
     * is a valid current Velocimacro.
     *
     * @param vmName  Name of velocimacro requested
     * @param renderingTemplate Template we are currently rendering. This
     *    information is needed when VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL setting is true
     *    and template contains a macro with the same name as the global macro library.
     * @param template Template which acts as the host for the macro
     *
     * @return VelocimacroProxy
     */
    public Directive getVelocimacro(String vmName, Template renderingTemplate, Template template)
    {
        return vmFactory.getVelocimacro(vmName, renderingTemplate, template);
    }

    /**
     * Adds a new Velocimacro. Usually called by Macro only while parsing.
     *
     * @param name  Name of velocimacro
     * @param macro  root AST node of the parsed macro
     * @param macroArgs  Array of macro arguments, containing the
     *        #macro() arguments and default values.  the 0th is the name.
     * @param definingTemplate Template containing the source of the macro
     *
     * @return boolean  True if added, false if rejected for some
     *                  reason (either parameters or permission settings)
     */
    public boolean addVelocimacro( String name,
                                   Node macro,
                                   List<Macro.MacroArg> macroArgs,
                                   Template definingTemplate)
    {
        return vmFactory.addVelocimacro(stringInterning ? name.intern() : name, macro, macroArgs, definingTemplate);
    }

    /**
     *  Checks to see if a VM exists
     *
     * @param vmName Name of the Velocimacro.
     * @param template Template on which to look for the Macro.
     * @return True if VM by that name exists, false if not
     */
    public boolean isVelocimacro(String vmName, Template template)
    {
        return vmFactory.isVelocimacro(stringInterning ? vmName.intern() : vmName, template);
    }

    /* --------------------------------------------------------------------
     * R U N T I M E  A C C E S S O R  M E T H O D S
     * --------------------------------------------------------------------
     * These are the getXXX() methods that are a simple wrapper
     * around the configuration object. This is an attempt
     * to make a the Velocity Runtime the single access point
     * for all things Velocity, and allow the Runtime to
     * adhere as closely as possible the the Mediator pattern
     * which is the ultimate goal.
     * --------------------------------------------------------------------
     */

    /**
     * String property accessor method to hide the configuration implementation
     * @param key  property key
     * @return   value of key or null
     */
    public String getString(String key)
    {
        return StringUtils.trim(configuration.getString(key));
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key Property key
     * @return value
     */
    public int getInt(String key)
    {
        return configuration.getInt(key);
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key  property key
     * @param defaultValue The default value.
     * @return value
     */
    public int getInt(String key, int defaultValue)
    {
        return configuration.getInt(key, defaultValue);
    }

    /**
     * Boolean property accessor method to hide the configuration implementation.
     *
     * @param key property key
     * @param def The default value if property not found.
     * @return value of key or default value
     */
    public boolean getBoolean(String key, boolean def)
    {
        return configuration.getBoolean(key, def);
    }

    /**
     * Return the velocity runtime configuration object.
     *
     * @return Configuration object which houses the Velocity runtime
     * properties.
     */
    public ExtProperties getConfiguration()
    {
        return configuration;
    }

    /**
     * Returns the event handlers for the application.
     * @return The event handlers for the application.
     * @since 1.5
     */
    public EventCartridge getApplicationEventCartridge()
    {
        return eventCartridge;
    }


    /**
     *  Gets the application attribute for the given key
     *
     * @param key
     * @return The application attribute for the given key.
     */
    public Object getApplicationAttribute(Object key)
    {
        return applicationAttributes.get(key);
    }

    /**
     *   Sets the application attribute for the given key
     *
     * @param key
     * @param o The new application attribute.
     * @return The old value of this attribute or null if it hasn't been set before.
     */
    public Object setApplicationAttribute(Object key, Object o)
    {
        return applicationAttributes.put(key, o);
    }

    /**
     * Returns the Uberspect object for this Instance.
     *
     * @return The Uberspect object for this Instance.
     */
    public Uberspect getUberspect()
    {
        return uberSpect;
    }

    /**
     * Whether to use string interning
     *
     * @return boolean
     */
    public boolean useStringInterning()
    {
        return stringInterning;
    }

    /**
     * get space gobbling mode
     * @return indentation mode
     */
    public SpaceGobbling getSpaceGobbling()
    {
        return spaceGobbling;
    }
}
