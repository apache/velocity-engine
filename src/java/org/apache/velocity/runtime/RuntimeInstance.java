package org.apache.velocity.runtime;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.log.LogManager;
import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.log.PrimordialLogSystem;
import org.apache.velocity.runtime.log.NullLogSystem;

import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.VelocimacroFactory;

import org.apache.velocity.runtime.resource.ContentResource;
import org.apache.velocity.runtime.resource.ResourceManager;

import org.apache.velocity.util.SimplePool;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.UberspectLoggable;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This is the Runtime system for Velocity. It is the
 * single access point for all functionality in Velocity.
 * It adheres to the mediator pattern and is the only
 * structure that developers need to be familiar with
 * in order to get Velocity to perform.
 *
 * The Runtime will also cooperate with external
 * systems like Turbine. Runtime properties can
 * set and then the Runtime is initialized.
 *
 * Turbine, for example, knows where the templates
 * are to be loaded from, and where the Velocity
 * log file should be placed.
 *
 * So in the case of Velocity cooperating with Turbine
 * the code might look something like the following:
 *
 * <blockquote><code><pre>
 * ri.setProperty(Runtime.FILE_RESOURCE_LOADER_PATH, templatePath);
 * ri.setProperty(Runtime.RUNTIME_LOG, pathToVelocityLog);
 * ri.init();
 * </pre></code></blockquote>
 *
 * <pre>
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
 * @version $Id: RuntimeInstance.java,v 1.19.4.1 2004/03/03 23:22:55 geirm Exp $
 */
public class RuntimeInstance implements RuntimeConstants, RuntimeServices
{    
    /**
     *  VelocimacroFactory object to manage VMs
     */
    private  VelocimacroFactory vmFactory = null;

    /** 
     *  The Runtime logger.  We start with an instance of
     *  a 'primordial logger', which just collects log messages
     *  then, when the log system is initialized, we dump
     *  all messages out of the primordial one into the real one.
     */
    private  LogSystem logSystem = new PrimordialLogSystem();

    /** 
     * The Runtime parser pool 
     */
    private  SimplePool parserPool;
    
    /** 
     * Indicate whether the Runtime has been fully initialized.
     */
    private  boolean initialized;

    /**
     * These are the properties that are laid down over top
     * of the default properties when requested.
     */
    private  ExtendedProperties overridingProperties = null;

    /**
     * This is a hashtable of initialized directives.
     * The directives that populate this hashtable are
     * taken from the RUNTIME_DEFAULT_DIRECTIVES
     * property file. This hashtable is passed
     * to each parser that is created.
     */
    private Hashtable runtimeDirectives;

    /**
     * Object that houses the configuration options for
     * the velocity runtime. The ExtendedProperties object allows
     * the convenient retrieval of a subset of properties.
     * For example all the properties for a resource loader
     * can be retrieved from the main ExtendedProperties object
     * using something like the following:
     *
     * ExtendedProperties loaderConfiguration = 
     *         configuration.subset(loaderID);
     *
     * And a configuration is a lot more convenient to deal
     * with then conventional properties objects, or Maps.
     */
    private  ExtendedProperties configuration = new ExtendedProperties();

    private ResourceManager resourceManager = null;

    /*
     *  Each runtime instance has it's own introspector
     *  to ensure that each instance is completely separate.
     */
    private Introspector introspector = null;


    /*
     *  Opaque reference to something specificed by the 
     *  application for use in application supplied/specified
     *  pluggable components
     */
    private Map applicationAttributes = null;


    private Uberspect uberSpect;

    public RuntimeInstance()
    {
        /*
         *  create a VM factory, resource manager
         *  and introspector
         */

        vmFactory = new VelocimacroFactory( this );

        /*
         *  make a new introspector and initialize it
         */
         
        introspector = new Introspector( this );

        /*
         * and a store for the application attributes
         */

        applicationAttributes = new HashMap();
    }

    /*
     * This is the primary initialization method in the Velocity
     * Runtime. The systems that are setup/initialized here are
     * as follows:
     *
     * <ul>
     *   <li>Logging System</li>
     *   <li>ResourceManager</li>
     *   <li>Parser Pool</li>
     *   <li>Global Cache</li>
     *   <li>Static Content Include System</li>
     *   <li>Velocimacro System</li>
     * </ul>
     */
    public synchronized void init()
        throws Exception
    {
        if (initialized == false)
        {
            info("************************************************************** ");
            info("Starting Jakarta Velocity v@version@");
            info("RuntimeInstance initializing.");
            initializeProperties();
            initializeLogger();
            initializeResourceManager();
            initializeDirectives();
            initializeParserPool();

            initializeIntrospection();
            /*
             *  initialize the VM Factory.  It will use the properties 
             * accessable from Runtime, so keep this here at the end.
             */
            vmFactory.initVelocimacro();

            info("Velocity successfully started.");
            
            initialized = true;
        }
    }


    /**
     *  Gets the classname for the Uberspect introspection package and
     *  instantiates an instance.
     */
    private void initializeIntrospection()
        throws Exception
    {
        String rm = getString(RuntimeConstants.UBERSPECT_CLASSNAME);

        if (rm != null && rm.length() > 0)
        {
            Object o = null;

            try
            {
               o = Class.forName(rm).newInstance();
            }
            catch (ClassNotFoundException cnfe)
            {
                String err = "The specified class for Uberspect ("
                    + rm
                    + ") does not exist (or is not accessible to the current classlaoder.";
                 error(err);
                 throw new Exception(err);
            }

            if (!(o instanceof Uberspect))
            {
                String err = "The specified class for Uberspect ("
                    + rm
                    + ") does not implement org.apache.velocity.util.introspector.Uberspect."
                    + " Velocity not initialized correctly.";

                error(err);
                throw new Exception(err);
            }

            uberSpect = (Uberspect) o;

            if (uberSpect instanceof UberspectLoggable)
            {
                ((UberspectLoggable) uberSpect).setRuntimeLogger(this);
            }

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

            error(err);
            throw new Exception(err);
        }
    }

    /**
     * Initializes the Velocity Runtime with properties file.
     * The properties file may be in the file system proper,
     * or the properties file may be in the classpath.
     */
    private void setDefaultProperties()
    {
        try
        {
            InputStream inputStream = getClass()
                .getResourceAsStream('/' + DEFAULT_RUNTIME_PROPERTIES);
            
            configuration.load( inputStream );
            
            info ("Default Properties File: " + 
                new File(DEFAULT_RUNTIME_PROPERTIES).getPath());
        }
        catch (IOException ioe)
        {
            System.err.println("Cannot get Velocity Runtime default properties!");
        }
    }

    /**
     * Allows an external system to set a property in
     * the Velocity Runtime.
     *
     * @param String property key
     * @param String property value
     */
    public void setProperty(String key, Object value)
    {
        if (overridingProperties == null)
        {
            overridingProperties = new ExtendedProperties();
        }            
            
        overridingProperties.setProperty( key, value );
    }        

    /**
     * Allow an external system to set an ExtendedProperties
     * object to use. This is useful where the external
     * system also uses the ExtendedProperties class and
     * the velocity configuration is a subset of
     * parent application's configuration. This is
     * the case with Turbine.
     *
     * @param ExtendedProperties configuration
     */
    public void setConfiguration( ExtendedProperties configuration)
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
     * @param String key
     * @param String value
     */
    public void addProperty(String key, Object value)
    {
        if (overridingProperties == null)
        {
            overridingProperties = new ExtendedProperties();
        }            
            
        overridingProperties.addProperty( key, value );
    }
    
    /**
     * Clear the values pertaining to a particular
     * property.
     *
     * @param String key of property to clear
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
     */
    public Object getProperty( String key )
    {
        return configuration.getProperty( key );
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
     * @param Properties
     */
    public void init(Properties p) throws Exception
    {
        overridingProperties = ExtendedProperties.convertProperties(p);
        init();
    }
    
    /**
     * Initialize the Velocity Runtime with the name of
     * ExtendedProperties object.
     *
     * @param Properties
     */
    public void init(String configurationFile)
        throws Exception
    {
        overridingProperties = new ExtendedProperties(configurationFile);
        init();
    }

    private void initializeResourceManager()
        throws Exception
    {
        /*
         * Which resource manager?
         */
         
        String rm = getString( RuntimeConstants.RESOURCE_MANAGER_CLASS );

        if ( rm != null && rm.length() > 0 )
        {
            /*
             *  if something was specified, then make one.
             *  if that isn't a ResourceManager, consider
             *  this a huge error and throw
             */
            
            Object o = null;
            
            try
            {
               o = Class.forName( rm ).newInstance();
            }
            catch (ClassNotFoundException cnfe )
            {
                String err = "The specified class for Resourcemanager ("
                    + rm    
                    + ") does not exist (or is not accessible to the current classlaoder.";
                 error( err );
                 throw new Exception( err );
            }
            
            if (!(o instanceof ResourceManager) )
            {
                String err = "The specified class for ResourceManager ("
                    + rm 
                    + ") does not implement org.apache.runtime.resource.ResourceManager."
                    + " Velocity not initialized correctly.";
                    
                error( err);
                throw new Exception(err);
            }

            resourceManager = (ResourceManager) o;
            
            resourceManager.initialize( this );        
         }
         else
         {
            /*
             *  someone screwed up.  Lets not fool around...
             */
             
            String err = "It appears that no class was specified as the"
            + " ResourceManager.  Please ensure that all configuration"
            + " information is correct.";
            
            error( err);
            throw new Exception( err );
        }                            
    }
    
    /**
     * Initialize the Velocity logging system.
     *
     * @throws Exception
     */
    private void initializeLogger() throws Exception
    { 
        /*
         * Initialize the logger. We will eventually move all
         * logging into the logging manager.
         */
        if (logSystem instanceof PrimordialLogSystem )
        {
            PrimordialLogSystem pls = (PrimordialLogSystem) logSystem;
            logSystem = LogManager.createLogSystem( this );
            
            /*
             * in the event of failure, lets do something to let it 
             * limp along.
             */
             
             if (logSystem == null)
             {
                logSystem = new NullLogSystem();
             }
             else
             {
                pls.dumpLogMessages( logSystem );
             }
        }
   }

    
    /**
     * This methods initializes all the directives
     * that are used by the Velocity Runtime. The
     * directives to be initialized are listed in
     * the RUNTIME_DEFAULT_DIRECTIVES properties
     * file.
     *
     * @throws Exception
     */
    private void initializeDirectives() throws Exception
    {
        /*
         * Initialize the runtime directive table.
         * This will be used for creating parsers.
         */
        runtimeDirectives = new Hashtable();
        
        Properties directiveProperties = new Properties();
        
        /*
         * Grab the properties file with the list of directives
         * that we should initialize.
         */

        InputStream inputStream =
            getClass().getResourceAsStream('/' + DEFAULT_RUNTIME_DIRECTIVES);
    
        if (inputStream == null)
            throw new Exception("Error loading directive.properties! " +
                                "Something is very wrong if these properties " +
                                "aren't being located. Either your Velocity " +
                                "distribution is incomplete or your Velocity " +
                                "jar file is corrupted!");
        
        directiveProperties.load(inputStream);
        
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
            loadDirective( directiveClass, "System" );
        }
        
        /*
         *  now the user's directives
         */
         
        String[] userdirective = configuration.getStringArray("userdirective");
        
        for( int i = 0; i < userdirective.length; i++)
        {
            loadDirective( userdirective[i], "User");
        }
        
    }
    
    /**
     *  instantiates and loads the directive with some basic checks
     * 
     *  @param directiveClass classname of directive to load 
     */
    private void loadDirective( String directiveClass, String caption )
    {    
        try
        {
            Object o = Class.forName( directiveClass ).newInstance();
            
            if ( o instanceof Directive )
            {
                Directive directive = (Directive) o;
                runtimeDirectives.put(directive.getName(), directive);
                    
                info("Loaded " + caption + " Directive: " 
                    + directiveClass);
            }
            else
            {
                error( caption + " Directive " + directiveClass 
                    + " is not org.apache.velocity.runtime.directive.Directive."
                    + " Ignoring. " );
            }
        }
        catch (Exception e)
        {
            error("Exception Loading " + caption + " Directive: " 
                + directiveClass + " : " + e);    
        }
    }
    
    
    /**
     * Initializes the Velocity parser pool.
     * This still needs to be implemented.
     */
    private void initializeParserPool()
    {
        int numParsers = getInt( PARSER_POOL_SIZE, NUMBER_OF_PARSERS);

        parserPool = new SimplePool( numParsers);

        for (int i=0; i < numParsers ;i++ )
        {
            parserPool.put (createNewParser());
        }

        info ("Created: " + numParsers + " parsers.");
    }

    /**
     * Returns a JavaCC generated Parser.
     *
     * @return Parser javacc generated parser
     */
    public Parser createNewParser()
    {
        Parser parser = new Parser( this );
        parser.setDirectives(runtimeDirectives);
        return parser;
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
     * @param InputStream inputstream retrieved by a resource loader
     * @param String name of the template being parsed
     */
    public SimpleNode parse( Reader reader, String templateName )
        throws ParseException
    {
        /*
         *  do it and dump the VM namespace for this template
         */
        return parse( reader, templateName, true );
    }

    /**
     *  Parse the input and return the root of the AST node structure.
     *
     * @param InputStream inputstream retrieved by a resource loader
     * @param String name of the template being parsed
     * @param dumpNamespace flag to dump the Velocimacro namespace for this template
     */
    public SimpleNode parse( Reader reader, String templateName, boolean dumpNamespace )
        throws ParseException
    {

        SimpleNode ast = null;
        Parser parser = (Parser) parserPool.get();
        boolean madeNew = false;

        if (parser == null)
        {
            /*
             *  if we couldn't get a parser from the pool
             *  make one and log it.
             */
            
            error("Runtime : ran out of parsers. Creating new.  "
                  + " Please increment the parser.pool.size property."
                  + " The current value is too small.");

            parser = createNewParser();

            if( parser != null )
            {
                madeNew = true;
            }
        }

        /*
         *  now, if we have a parser
         */

        if (parser != null)
        {
            try
            {
                /*
                 *  dump namespace if we are told to.  Generally, you want to 
                 *  do this - you don't in special circumstances, such as 
                 *  when a VM is getting init()-ed & parsed
                 */

                if ( dumpNamespace )
                {
                    dumpVMNamespace( templateName );
                }

                ast = parser.parse( reader, templateName );
            }
            finally
            {
                /*
                 *  if this came from the pool, then put back
                 */
                if (!madeNew)
                {
                    parserPool.put(parser);
                }
            }
        }
        else
        {
            error("Runtime : ran out of parsers and unable to create more.");
        }
        return ast;
    }
    
    /**
     * Returns a <code>Template</code> from the resource manager.
     * This method assumes that the character encoding of the 
     * template is set by the <code>input.encoding</code>
     * property.  The default is "ISO-8859-1"
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return getTemplate( name, getString( INPUT_ENCODING, ENCODING_DEFAULT) );
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
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name, String  encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return (Template) resourceManager.getResource(name, ResourceManager.RESOURCE_TEMPLATE, encoding);
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
     */
    public ContentResource getContent(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        /*
         *  the encoding is irrelvant as we don't do any converstion
         *  the bytestream should be dumped to the output stream
         */

        return getContent( name,  getString( INPUT_ENCODING, ENCODING_DEFAULT));
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
     */
    public ContentResource getContent( String name, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return (ContentResource) resourceManager.getResource(name,ResourceManager.RESOURCE_CONTENT, encoding );
    }


    /**
     *  Determines is a template exists, and returns name of the loader that 
     *  provides it.  This is a slightly less hokey way to support
     *  the Velocity.templateExists() utility method, which was broken
     *  when per-template encoding was introduced.  We can revisit this.
     *
     *  @param resourceName Name of template or content resource
     *  @return class name of loader than can provide it
     */
    public String getLoaderNameForResource( String resourceName )
    {
        return resourceManager.getLoaderNameForResource( resourceName );
    }

    /**
     * Added this to check and make sure that the configuration
     * is initialized before trying to get properties from it.
     * This occurs when there are errors during initialization
     * and the default properties have yet to be layed down.
     */
    private boolean showStackTrace()
    {
        if (configuration.isInitialized())
        {
            return getBoolean(RUNTIME_LOG_WARN_STACKTRACE, false);
        }            
        else
        {
            return false;
        }            
    }

    /**
     * Handle logging.
     *
     * @param String message to log
     */
    private void log(int level, Object message)
    {
        String out;
     
        /*
         *  now,  see if the logging stacktrace is on
         *  and modify the message to suit
         */
        if ( showStackTrace() &&
            (message instanceof Throwable || message instanceof Exception) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }            

        /*
         *  just log it, as we are guaranteed now to have some
         *  kind of logger - save the if()
         */
        logSystem.logVelocityMessage( level, out);
    }

    /**
     * Log a warning message.
     *
     * @param Object message to log
     */
    public void warn(Object message)
    {
        log(LogSystem.WARN_ID, message);
    }
    
    /** 
     * Log an info message.
     *
     * @param Object message to log
     */
    public void info(Object message)
    {
        log(LogSystem.INFO_ID, message);
    }
    
    /**
     * Log an error message.
     *
     * @param Object message to log
     */
    public void error(Object message)
    {
        log(LogSystem.ERROR_ID, message);
    }
    
    /**
     * Log a debug message.
     *
     * @param Object message to log
     */
    public void debug(Object message)
    {
        log(LogSystem.DEBUG_ID, message);
    }

    /**
     * String property accessor method with default to hide the
     * configuration implementation.
     * 
     * @param String key property key
     * @param String defaultValue  default value to return if key not 
     *               found in resource manager.
     * @return String  value of key or default 
     */
    public String getString( String key, String defaultValue)
    {
        return configuration.getString(key, defaultValue);
    }

    /**
     * Returns the appropriate VelocimacroProxy object if strVMname
     * is a valid current Velocimacro.
     *
     * @param String vmName  Name of velocimacro requested
     * @return String VelocimacroProxy 
     */
    public Directive getVelocimacro( String vmName, String templateName  )
    {
        return vmFactory.getVelocimacro( vmName, templateName );
    }

   /**
     * Adds a new Velocimacro. Usually called by Macro only while parsing.
     *
     * @param String name  Name of velocimacro 
     * @param String macro  String form of macro body
     * @param String argArray  Array of strings, containing the 
     *                         #macro() arguments.  the 0th is the name.
     * @return boolean  True if added, false if rejected for some 
     *                  reason (either parameters or permission settings) 
     */
    public boolean addVelocimacro( String name, 
                                          String macro, 
                                          String argArray[], 
                                          String sourceTemplate )
    {    
        return vmFactory.addVelocimacro(  name, macro,  argArray,  sourceTemplate );
    }
 
    /**
     *  Checks to see if a VM exists
     *
     * @param name  Name of velocimacro
     * @return boolean  True if VM by that name exists, false if not
     */
    public boolean isVelocimacro( String vmName, String templateName )
    {
        return vmFactory.isVelocimacro( vmName, templateName );
    }

    /**
     *  tells the vmFactory to dump the specified namespace.  This is to support
     *  clearing the VM list when in inline-VM-local-scope mode
     */
    public boolean dumpVMNamespace( String namespace )
    {
        return vmFactory.dumpVMNamespace( namespace );
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
        return configuration.getString( key );
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param String key property key
     * @return int value
     */
    public int getInt( String key )
    {
        return configuration.getInt( key );
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key  property key
     * @param int default value
     * @return int  value
     */
    public int getInt( String key, int defaultValue )
    {
        return configuration.getInt( key, defaultValue );
    }

    /**
     * Boolean property accessor method to hide the configuration implementation.
     * 
     * @param String key  property key
     * @param boolean default default value if property not found
     * @return boolean  value of key or default value
     */
    public boolean getBoolean( String key, boolean def )
    {
        return configuration.getBoolean( key, def );
    }

    /**
     * Return the velocity runtime configuration object.
     *
     * @return ExtendedProperties configuration object which houses
     *                       the velocity runtime properties.
     */
    public ExtendedProperties getConfiguration()
    {
        return configuration;
    }        
    
    /**
     *  Return the Introspector for this instance
     */
    public Introspector getIntrospector()
    {
        return introspector;
    }

    public Object getApplicationAttribute( Object key)
    {
        return applicationAttributes.get( key );
    }

    public Object setApplicationAttribute( Object key, Object o )
    {
        return applicationAttributes.put( key, o );
    }

    public Uberspect getUberspect()
    {
        return uberSpect;
    }

}
