/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.runtime;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log.LogKit;
import org.apache.log.Logger;
import org.apache.log.LogTarget;
import org.apache.log.Formater;
import org.apache.log.output.FileOutputLogTarget;

import org.apache.velocity.runtime.log.VelocityFormater;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.VelocimacroFactory;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ContentResource;
import org.apache.velocity.runtime.resource.ResourceManager;

import org.apache.velocity.util.SimplePool;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.runtime.configuration.VelocityResources;

/**
 * This is the Runtime system for Velocity. It is the
 * single access point for all functionality in Velocity.
 * It adheres to the mediator pattern and is the only
 * structure that developers need to be familiar with
 * in order to get Velocity to perform.
 *
 * <pre>
 * Runtime.init(properties);
 *
 * Template template = Runtime.getTemplate("template.vm");
 *
 * Runtime.warn(message);
 * Runtime.info(message);
 * Runtime.error(message);
 * Runtime.debug(message);
 * </pre>
 *
 * The Runtime will also cooperate with external
 * systems like Turbine. Normally the Runtime will
 * be fully initialized from a properties file, but
 * certain phases of initialization can be delayed
 * if vital pieces of information are provided by
 * an external system.
 *
 * Turbine for example knows where the templates
 * are to be loaded from, and where the velocity
 * log file should be placed.
 *
 * In order for this to happen the velocity.properties
 * file must look something like the following:
 *
 * runtime.log = system
 * template.path = system
 *
 * Having these properties set to 'system' lets the
 * Velocity Runtime know that an external system
 * will set these properties and initialized 
 * the appropriates sub systems when these properties
 * are set.
 *
 * So in the case of Velocity cooperating with Turbine
 * the code might look something like the following:
 *
 * <pre>
 * Runtime.setProperty(Runtime.RUNTIME_LOG, pathToVelocityLog);
 * Runtime.initializeLogger();
 *
 * Runtime.setProperty(Runtime.TEMPLATE_PATH, pathToTemplates);
 * Runtime.initializeTemplateLoader();
 * </pre>
 *
 * It is simply a matter of setting the appropriate property
 * an initializing the matching sub system.
 *
 *
 * -----------------------------------------------------------------------
 * N O T E S  O N  R U N T I M E  I N I T I A L I Z A T I O N
 * -----------------------------------------------------------------------
 * Runtime.init()
 * 
 * If Runtime.init() is called by itself without any previous calls
 * to Runtime.setProperties(props) or Runtime.setDefaultProperties(props)
 * then the default velocity properties file will be loaded and
 * the velocity runtime will be initialized.
 * -----------------------------------------------------------------------
 * Runtime.init(properties)
 *
 * In this case the default velocity properties are layed down
 * first to provide a solid base, then any properties provided
 * in the given properties object will override the corresponding
 * default property.
 * -----------------------------------------------------------------------
 * Runtime.setProperties(properties) 
 * [ Runtime.setProperty() || Runtime.setSourceProperty() ]
 * Runtime.init()
 *
 * In this case the client app has decided to set its own default
 * properties file. So what happens is that the default velocity
 * properties are laid down first, then the properties file
 * specified by the client app is laid down on top of that
 * overriding any of the defaults, then any calls to setProperty()
 * or setSourceProperty() will override those.
 *
 * Turbine uses this method with its TurbineVelocityService. If
 * you would like to see an example of this method of initialization
 * look at org.apache.turbine.services.velocity.TurbineVelocityService.
 * -----------------------------------------------------------------------
 * Runtime.setDefaultProperties()
 * [ Runtime.setProperty || Runtime.setSourceProperty() ]
 * Runtime.init()
 *
 * In this case the client app is going to use the default
 * velocity properties file and change a few things before
 * initializing the velocity runtime.
 *
 * If you want to see an example of this, look at the Velocity
 * testbed. The org.apache.velocity.test.VelocityTestSuite class
 * uses this method: using all the defaults except for the file
 * template path.
 * -----------------------------------------------------------------------
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:jlb@houseofdistraction.com">Jeff Bowden</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magusson Jr.</a>
 * @version $Id: Runtime.java,v 1.78 2000/12/19 21:51:02 jvanzyl Exp $
 */
public class Runtime implements RuntimeConstants
{    
    /**
     *  VelocimacroFactory object to manage VMs
     */
    private static VelocimacroFactory vmFactory = new VelocimacroFactory();

    /** The Runtime logger */
    private static Logger logger;

    /** The caching system used by the Velocity Runtime */
    private static Hashtable globalCache;
    
    /** The Runtime parser pool */
    private static SimplePool parserPool;
    
    /** Indicate whether the Runtime has been fully initialized */
    private static boolean initialized;

    private static Properties overridingProperties = null;

    /**
     * The logging systems initialization may be defered if
     * it is to be initialized by an external system. There
     * may be messages that need to be stored until the
     * logger is instantiated. They will be stored here
     * until the logger is alive.
     */
    private static Vector pendingMessages = new Vector();

    /**
     * This is a hashtable of initialized directives.
     * The directives that populate this hashtable are
     * taken from the RUNTIME_DEFAULT_DIRECTIVES
     * property file. This hashtable is passed
     * to each parser that is created.
     */
    private static Hashtable runtimeDirectives;

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
    public synchronized static void init()
        throws Exception
    {
        if (initialized == false)
        {
            try
            {
                initializeProperties();
                initializeLogger();
                ResourceManager.initialize();
                initializeDirectives();
                initializeParserPool();
                initializeGlobalCache();
                
                /*
                 *  initialize the VM Factory.  It will use the properties 
                 * accessable from Runtime, so keep this here at the end.
                 */
                vmFactory.initVelocimacro();
                
                info("Velocity successfully started.");
                
                initialized = true;
            }
            catch (Exception e)
            {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the Velocity Runtime.
     */
    public synchronized static void init( Properties props )
        throws Exception
    {
        overridingProperties = props;
        init();
    }

    public synchronized static void init( String props )
        throws Exception
    {
        setProperties(props);
        init();
    }

    /**
     * Allow an external mechanism to set the properties for
     * Velocity Runtime. This is being use right now by Turbine.
     * There is a standard velocity properties file that is
     * employed by Turbine/Velocity apps, but certain properties
     * like the location of log file, and the template path
     * must be set by Turbine because the location of the
     * log file, and the template path are based on
     * the location of the context root.
     *
     * So common properties can be set with a standard properties
     * file, then certain properties can be changed before
     * the Velocity Runtime is initialized.
     */
    public synchronized static void setProperties(String propertiesFileName) 
        throws Exception
    {
        /*
         * Set the default properties because client apps are
         * using the:
         *
         * 1) Runtime.setProperties();
         * 2) Runtime.setProperty() | Runtime.setSourceProperty()
         * 3) Runtime.init();
         *
         * Sequence and the default props have to be present
         * in order for 2) to work.
         */
        setDefaultProperties();
        
        Properties p = new Properties();        
        
        /*
         * if we were passed propertis, try loading propertiesFile as a straight file first,
         * if that fails, then try and use the classpath
         */
        if (propertiesFileName != null && !propertiesFileName.equals(""))
        {
            File file = new File(propertiesFileName);
            try
            {
                if( file.exists() )
                {
                    FileInputStream is = new FileInputStream( file );
                    p.load(is);
                }
                else
                {
                    info ("Override Properties : " + file.getPath() + " not found. Looking in classpath.");
                    
                    /*
                     *  lets try the classpath
                     */
                    ClassLoader classLoader = Runtime.class.getClassLoader();
                    InputStream inputStream = classLoader
                        .getResourceAsStream( propertiesFileName );
                    
                    if (inputStream!= null)
                    {
                        p.load(inputStream);
                    }
                    else
                    {
                        info ("Override Properties : " + propertiesFileName + " not found in classpath.");
                    }
                }
            }
            catch (Exception ex)
            {
                error("Exception finding properties  " + propertiesFileName + " : " + ex);
            }
        }
    
        overridingProperties = p;
    }

    /**
     * Initializes the Velocity Runtime with properties file.
     * The properties file may be in the file system proper,
     * or the properties file may be in the classpath.
     */
    public static void setDefaultProperties()
    {
        ClassLoader classLoader = Runtime.class.getClassLoader();
        try
        {
            InputStream inputStream = classLoader
                .getResourceAsStream( DEFAULT_RUNTIME_PROPERTIES );
            
            VelocityResources.setPropertiesInputStream( inputStream );
            
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
     */
    public static void setProperty(String key, String value)
    {
        if (overridingProperties == null)
            overridingProperties = new Properties();
            
        overridingProperties.setProperty( key, value );
    }        

    private static void initializeProperties()
    {
        /* 
         * Always lay down the default properties first as
         * to provide a solid base.
         */
        if (VelocityResources.isInitialized() == false)
            setDefaultProperties();
    
        if( overridingProperties != null)
        {        
            /* Override each default property specified */
            for (Enumeration e = overridingProperties.keys(); e.hasMoreElements() ; ) 
            {
                String s = (String) e.nextElement();
                VelocityResources.setProperty( s, overridingProperties.getProperty(s) );
                info ("   ** Property Override : " + s + " = " + 
                    overridingProperties.getProperty(s));
            }
        }
    }

    /**
     * Initialize the Velocity logging system.
     */
    private static void initializeLogger() throws
        MalformedURLException
    {
        /* 
         * Let's look at the log file entry and
         * correct it if it is not a property 
         * fomratted URL.
         */
        String logFile = VelocityResources.getString(RUNTIME_LOG);

        /*
         * Initialize the logger.
         */
        logger = LogKit.createLogger("velocity", 
        StringUtils.fileToURL(logFile), "DEBUG");
                
        LogTarget[] t = logger.getLogTargets();            

        ((FileOutputLogTarget)t[0])
            .setFormater((Formater) new VelocityFormater());

        ((FileOutputLogTarget)t[0])
            .setFormat("%{time} %{message}\\n%{throwable}" );

        if ( !pendingMessages.isEmpty())
        {
            /*
             *  iterate and log each individual message...
             */
            for( Enumeration e = pendingMessages.elements(); e.hasMoreElements(); )
            {
                logger.info( (String) e.nextElement());
            }
        }
        Runtime.info("Log file being used is: " + new File(logFile).getAbsolutePath());
    }

    /**
     * This methods initializes all the directives
     * that are used by the Velocity Runtime. The
     * directives to be initialized are listed in
     * the RUNTIME_DEFAULT_DIRECTIVES properties
     * file.
     */
    private static void initializeDirectives() throws Exception
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
        ClassLoader classLoader = Runtime.class.getClassLoader();
        InputStream inputStream = classLoader
            .getResourceAsStream(DEFAULT_RUNTIME_DIRECTIVES);
    
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
        
            try
            {
                /*
                 * Attempt to instantiate the directive class. This
                 * should usually happen without error because the
                 * properties file that lists the directives is
                 * not visible. It's in a package that isn't 
                 * readily accessible.
                 *
                 */
                Class clazz = Class.forName(directiveClass);
                Directive directive = (Directive) clazz.newInstance();
                runtimeDirectives.put(directive.getName(), directive);
                
                Runtime.info("Loaded Pluggable Directive: " 
                    + directiveClass);
            }
            catch (Exception e)
            {
                Runtime.error("Error Loading Pluggable Directive: " 
                    + directiveClass);    
            }
        }
    }

    /**
     * Allow clients of Velocity to set a template stream
     * source property before the template source streams
     * are initialized. This would for example allow clients
     * to set the template path that would be used by the
     * file template stream source. Right now these properties
     * have to be set before the template stream source is
     * initialized. Maybe we should allow these properties
     * to be changed on the fly.
     */
    public static void setSourceProperty(String key, String value)
    {
        info ("   ** !!! Resource Loader Property Override : " + key + " = " + value);
        ResourceManager.setSourceProperty(key, value);
    }

    /**
     * Initializes the Velocity parser pool.
     * This still needs to be implemented.
     */
    private static void initializeParserPool()
    {
        parserPool = new SimplePool(NUMBER_OF_PARSERS);
        for (int i=0;i<NUMBER_OF_PARSERS ;i++ )
        {
            parserPool.put (createNewParser());
        }
        Runtime.info ("Created: " + NUMBER_OF_PARSERS + " parsers.");
    }

    /**
     * Returns a parser
     */
    public static Parser createNewParser()
    {
        Parser parser = new Parser();
        parser.setDirectives(runtimeDirectives);
        return parser;
    }

    /**
     * Parse the input stream and return the root of
     * AST node structure.
     */
    public static SimpleNode parse(InputStream inputStream, String strTemplateName )
        throws ParseException
    {
        SimpleNode AST = null;
        Parser parser = (Parser) parserPool.get();
        
        if (parser != null)
        {
            AST = parser.parse(inputStream, strTemplateName);
            parserPool.put(parser);
            return AST;
        }
        else
        {
            error("Runtime : ran out of parsers!");
        }
        return null;
    }
    
    /**
     * Initialize the global cache use by the Velocity
     * runtime. Cached templates will be stored here,
     * as well as cached content pulled in by the #include
     * directive. Who knows what else we'll find to
     * cache.
     */
    private static void initializeGlobalCache()
    {
        //globalCache = new GlobalCache();
        globalCache = new Hashtable();
    }
    
    /**
     * Set an object in the global cache for
     * subsequent use.
     */
    public static void setCacheObject(String key, Object object)
    {
        globalCache.put(key,object);
    }

    /**
     * Get an object from the cache.
     *
     * Hmm. Getting an object requires catching
     * an ObjectExpiredException, but how can we do
     * this without tying ourselves to the to
     * the caching system?
     */
    public static Object getCacheObject(String key)
    {
        try
        {
            return globalCache.get(key);
        }
        catch (Exception e)
        {
            /* 
             * This is an ObjectExpiredException, but
             * I don't want to try the structure of the
             * caching system to the Runtime.
             */
            return null;            
        }
    }        

    /**
     * Returns a Template from the resource manager
     */
    public static Template getTemplate(String name)
        throws Exception
    {
        return (Template) ResourceManager
            .getResource(name,ResourceManager.RESOURCE_TEMPLATE);
    }

    /**
     * Returns a static content resource from the
     * resource manager.
     */
    public static ContentResource getContent(String name)
        throws Exception
    {
        return (ContentResource) ResourceManager
            .getResource(name,ResourceManager.RESOURCE_CONTENT);
    }

    /**
     * Handle logging
     */
    private static void log(String message)
    {
        if (logger != null)
        {
            logger.info(message);
        }
        else
        {
            pendingMessages.addElement(message);
        }
    }

    /**
     * Added this to check and make sure that the VelocityResources
     * is initialized before trying to get properties from it.
     * This occurs when there are errors during initialization
     * and the default properties have yet to be layed down.
     */
    private static boolean showStackTrace()
    {
        if (VelocityResources.isInitialized())
            return getBoolean(RUNTIME_LOG_WARN_STACKTRACE, false);
        else
            return false;
    }

    /**
     * Log a warning message
     */
    public static void warn(Object message)
    {
        String out = null;
        if ( showStackTrace() &&
            (message instanceof Throwable || message instanceof Exception) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        log(WARN + out);
    }
    
    /** Log an info message */
    public static void info(Object message)
    {
        String out = null;
        if ( showStackTrace() &&
            ( message instanceof Throwable || message instanceof Exception) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        log(INFO + out);
    }
    
    /**
     * Log an error message
     */
    public static void error(Object message)
    {
        String out = null;
        if ( showStackTrace() &&
            ( message instanceof Throwable || message instanceof Exception ) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        log(ERROR + out);
    }
    
    /**
     * Log a debug message
     */
    public static void debug(Object message)
    {
        if (DEBUG_ON)
        {
            log(DEBUG + message.toString());
        }
    }

    /**
     * String property accessor method with defaultto hide the VelocityResources implementation
     * @param strKey  property key
     * @param strDefault  default value to return if key not found in resource manager
     * @return String  value of key or default 
     */
    public static String getString( String strKey, String strDefault)
    {
        return VelocityResources.getString(strKey, strDefault);
    }

    /**
     *  returns the appropriate VelocimacroProxy object if strVMname
     *  is a valid current Velocimacro
     *
     * @param strVMName  Name of velocimacro requested
     * @return VelocimacroProxy 
     */
    public static Directive getVelocimacro( String strVMName, String strTemplateName  )
    {
        return vmFactory.getVelocimacro( strVMName, strTemplateName );
    }

   /**
     *  adds a new Velocimacro.  Usually called by Macro only while parsing
     *
     * @param strName  Name of velocimacro 
     * @param strMacro  String form of macro body
     * @param strArgArray  Array of strings, containing the #macro() arguments.  the 0th is the name.
     * @param strMacroArray  The macro body as an array of strings.  Basically, the tokenized literal  representation
     * @param tmArgIndexMap  Indexes to the args in the macro body string
     * @return boolean  True if added, false if rejected for some reason (either parameters or permission settings) 
     */
    public static boolean addVelocimacro( String strName, String strMacro, String  strArgArray[], 
                                          String strMacroArray[], TreeMap tmArgIndexMap, 
                                          String strSourceTemplate )
    {    
        return vmFactory.addVelocimacro(  strName, strMacro,  strArgArray,  strMacroArray, 
                                          tmArgIndexMap, strSourceTemplate);
    }

    /**
     *  Checks to see if a VM exists
     *
     * @param strName  Name of velocimacro
     * @return boolean  True if VM by that name exists, false if not
     */
    public static boolean isVelocimacro( String strVMName, String strTemplateName )
    {
        return vmFactory.isVelocimacro( strVMName, strTemplateName );
    }

    /**
     *  tells the vmFactory to dump the specified namespace.  This is to support
     *  clearing the VM list when in inline-VM-local-scope mode
     */
    public static boolean dumpVMNamespace( String strNamespace )
    {
        return vmFactory.dumpVMNamespace( strNamespace );
    }

    /* --------------------------------------------------------------------
     * R U N T I M E  A C C E S S O R  M E T H O D S
     * --------------------------------------------------------------------
     * These are the getXXX() methods that are a simple wrapper
     * around the VelocityResources object. This is an attempt
     * to make a the Velocity Runtime the single access point
     * for all things Velocity, and allow the Runtime to
     * adhere as closely as possible the the Mediator pattern
     * which is the ultimate goal.
     * --------------------------------------------------------------------
     */

    /**
     * String property accessor method to hide the VelocityResources implementation
     * @param strKey  property key
     * @return   value of key or null
     */
    public static String getString(String key)
    {
        return VelocityResources.getString( key );
    }

    /**
     * int property accessor method to hide the VelocityResources implementation
     * @param strKey  property key
     * @return int  alue
     */
    public static int getInt( String strKey )
    {
        return VelocityResources.getInt( strKey );
    }

    public static int getInt( String strKey, int defaultValue )
    {
        return VelocityResources.getInt( strKey, defaultValue );
    }

    /**
     * boolean  property accessor method to hide the VelocityResources implementation
     * @param strKey  property key
     * @param default default value if property not found
     * @return boolean  value of key or default value
     */
    public static boolean getBoolean( String strKey, boolean def )
    {
        return VelocityResources.getBoolean( strKey, def );
    }
}
