package org.apache.velocity.runtime;

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

import org.apache.velocity.runtime.loader.TemplateFactory;
import org.apache.velocity.runtime.loader.TemplateLoader;

import org.apache.velocity.runtime.directive.Foreach;
import org.apache.velocity.runtime.directive.Dummy;
import org.apache.velocity.runtime.directive.Include;
import org.apache.velocity.runtime.directive.Parse;

import org.apache.velocity.util.SimplePool;
import org.apache.velocity.util.StringUtils;
import org.apache.velocity.util.cache.GlobalCache;
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
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:jlb@houseofdistraction.com">Jeff Bowden</a>
 * @version $Id: Runtime.java,v 1.43 2000/11/16 02:15:29 jvanzyl Exp $
 */
public class Runtime
{
    /** Location of the log file */
    public static final String RUNTIME_LOG = "runtime.log";
    
    /** The encoding to use for the template */
    public static final String TEMPLATE_ENCODING = "template.encoding";
    
    /** Initial counter value in #foreach directives */
    public static final String COUNTER_NAME = "counter.name";

    /** Initial counter value in #foreach directives */
    public static final String COUNTER_INITIAL_VALUE = "counter.initial.value";

    /** Content type */
    public static final String DEFAULT_CONTENT_TYPE = "default.contentType";

    /** External service initialization of the Velocity Runtime */
    public static final String EXTERNAL_INIT = "external.init";

    /** Prefix for warning messages */
    private final static String WARN  = "  [warn] ";
    
    /** Prefix for info messages */
    private final static String INFO  = "  [info] ";
    
    /**  Prefix for debug messages */
    private final static String DEBUG = " [debug] ";
    
    /** Prefix for error messages */
    private final static String ERROR = " [error] ";

    /** TemplateLoader used by the Runtime */
    private static TemplateLoader templateLoader;
    
    /** Turn Runtime debugging on with this field */
    private final static boolean DEBUG_ON = true;

    /** Default Runtime properties */
    private final static String DEFAULT_RUNTIME_PROPERTIES = 
        "org/apache/velocity/runtime/defaults/velocity.properties";

    /** Include paths property used by Runtime for #included content */
    private final static String INCLUDE_PATHS = "include.path";
    
    /** A list of paths that we can pull static content from. */
    private static String[] includePaths;

    /** The Runtime logger */
    private static Logger logger;

    /** The caching system used by the Velocity Runtime */
    //private static GlobalCache globalCache;
    private static Hashtable globalCache;
    
    /**
     * The List of templateLoaders that the Runtime will
     * use to locate the InputStream source of a template.
     */
    private static List templateLoaders;

    /** 
      * The Runtime parser. This has to be changed to
      * a pool of parsers!
      */
    private static SimplePool parserPool;
    
    /**
      * Number of parsers to create
      */
    private static final int NUMBER_OF_PARSERS = 20;
    
    /** Indicate whether the Runtime has been fully initialized */
    private static boolean initialized;
    
    /**
     * The logging systems initialization may be defered if
     * it is to be initialized by an external system. There
     * may be messages that need to be stored until the
     * logger is instantiated. They will be stored here
     * until the logger is alive.
     */
    private static StringBuffer pendingMessages = new StringBuffer();

    /**
     * Get the default properties for the Velocity Runtime.
     * This would allow the retrieval and modification of
     * the base properties before initializing the Velocity
     * Runtime.
     */
    public static void setDefaultProperties()
    {
        ClassLoader classLoader = Runtime.class.getClassLoader();
        try
        {
            InputStream inputStream = classLoader.getResourceAsStream(
                DEFAULT_RUNTIME_PROPERTIES);
            VelocityResources.setPropertiesInputStream( inputStream );
            info ("Default Properties File: " + new File(DEFAULT_RUNTIME_PROPERTIES).getAbsolutePath());
        }
        catch (IOException ioe)
        {
            System.err.println("Cannot get Velocity Runtime default properties!");
        }
    }

    /**
     * Initializes the Velocity Runtime.
     */
    public synchronized static void init(String propertiesFileName)
        throws Exception
    {
        /*
         * Try loading propertiesFile as a straight file first,
         * if that fails, then try and use the classpath, if
         * that fails then use the default values.
         */
        try
        {
            VelocityResources.setPropertiesFileName( propertiesFileName );
            info ("Properties File: " + new File(propertiesFileName).getAbsolutePath());
        }
        catch(Exception ex) 
        {
            ClassLoader classLoader = Runtime.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(propertiesFileName);
            
            if (inputStream != null)
            {
                VelocityResources.setPropertiesInputStream( inputStream );
                info ("Properties File: " + new File(propertiesFileName).getAbsolutePath());
            }
            else
            {
                // Do Default
                setDefaultProperties();
            }                
        }

        init();
    }

    public synchronized static void init()
        throws Exception
    {
        if (! initialized)
        {
            try
            {
                initializeLogger();
                initializeTemplateLoader();           
                initializeParserPool();
                initializeGlobalCache();
                initializeIncludePaths();
                
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
     * Allows an external system to set a property in
     * the Velocity Runtime.
     */
    public static void setProperty(String key, String value)
    {
        VelocityResources.setProperty( key, value );
    }        

    /**
     * Initialize the Velocity logging system.
     */
    public static void initializeLogger() throws
        MalformedURLException
    {
        if (!getString(EXTERNAL_INIT).equalsIgnoreCase("true"))
        {
            // Let's look at the log file entry and
            // correct it if it is not a property 
            // fomratted URL.
            String logFile = getString(RUNTIME_LOG);

            // Initialize the logger.
            logger = LogKit.createLogger("velocity", 
                fileToURL(logFile), "DEBUG");
                
            LogTarget[] t = logger.getLogTargets();            

            ((FileOutputLogTarget)t[0])
                .setFormater((Formater) new VelocityFormater());

            ((FileOutputLogTarget)t[0])
                .setFormat("%{time} %{message}\\n%{throwable}" );

            if (pendingMessages.length() > 0)
            {
                logger.info(pendingMessages.toString());
            }
            
            Runtime.info("Log file being used is: " + new File(logFile).getAbsolutePath());
        }
    }

    /**
     * This was borrowed form xml-fop. Convert a file
     * name into a string that represents a well-formed
     * URL.
     *
     * d:\path\to\logfile
     * file://d:/path/to/logfile
     *
     * NOTE: this is a total hack-a-roo! This should
     * be dealt with in the org.apache.log package. Client
     * packages should not have to mess around making
     * properly formed URLs when log files are almost
     * always going to be specified with file paths!
     */
    private static String fileToURL(String filename)
        throws MalformedURLException
    {
        File file = new File(filename);
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        
        if (fSep != null && fSep.length() == 1)
            path = "file://" + path.replace(fSep.charAt(0), '/');
        
        return path;
    }

    /**
     * Initialize the template loader if there
     * is a real path set for the template.path
     * property. Otherwise defer initialization
     * of the template loader because it is going
     * to be set by some external mechanism: Turbine
     * for example.
     */
    
    // We have to change this to allow multiple template
    // loaders. We need a list of them.
    
    public static void initializeTemplateLoader() throws Exception
    {
        if (!getString(EXTERNAL_INIT).equalsIgnoreCase("true"))
        {
            List initializers = getTemplateLoaderInitializers();
            templateLoaders = new ArrayList();
            
            for (int i = 0; i < initializers.size(); i++)
            {
                Map initializer = (Map) initializers.get(i);
                String loaderClass = (String) initializer.get("class");
                templateLoader = TemplateFactory.getLoader(loaderClass);
                templateLoader.init(initializer);
                templateLoaders.add(templateLoader);
            }
        }            
    }

    /**
     * This will produce a List of Hashtables, each
     * hashtable contains the intialization info for
     * a particular template loader. This Hastable
     * will be passed in when initializing the
     * the template loader.
     */
    private static List getTemplateLoaderInitializers()
    {
        ArrayList loaderInitializers = new ArrayList();
        
        for (int i = 0; i < 10; i++)
        {
            String loaderID = "template.loader." + new Integer(i).toString();
            Enumeration e = VelocityResources.getKeys(loaderID);
            
            if (!e.hasMoreElements())
                continue;
            
            Hashtable loaderInitializer = new Hashtable();
            
            while (e.hasMoreElements())
            {
                String property = (String) e.nextElement();
                String value = VelocityResources.getString(property);
                
                property = property.substring(loaderID.length() + 1);
                loaderInitializer.put(property, value);
            }    
            
            loaderInitializers.add(loaderInitializer);
        }
        
        return loaderInitializers;
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
        Hashtable directives = new Hashtable();
        directives.put("foreach", new Foreach());
        directives.put("dummy", new Dummy());
        directives.put("include", new Include() );
        directives.put("parse", new Parse() );
        parser.setDirectives(directives);
        return parser;
    }

    /**
     * Parse the input stream and return the root of
     * AST node structure.
     */
    public static SimpleNode parse(InputStream inputStream)
        throws ParseException
    {
        SimpleNode AST = null;
        Parser parser = (Parser) parserPool.get();
        
        if (parser != null)
        {
            AST = parser.parse(inputStream);
            parserPool.put(parser);
            return AST;
        }
        else
            error("Runtime : ran out of parsers!");

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
    
    private static void initializeIncludePaths()
    {
        includePaths = VelocityResources.getStringArray(INCLUDE_PATHS);
    }

    public static String[] getIncludePaths()
    {
        return includePaths;
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
            // This is an ObjectExpiredException, but
            // I don't want to try the structure of the
            // caching system to the Runtime.
            return null;            
        }
    }        

    /**
     * Get a template via the TemplateLoader.
     */
    public static Template getTemplate(String template)
    {
        // Try the cache first.
        
        InputStream is = null;
        Template t= null;
        TemplateLoader tl = null;
        
        
        // Check to see if the template was placed in the cache.
        // If it was placed in the cache then we will use
        // the cached version of the template. If not we
        // will load it.
        
        if (globalCache.containsKey(template))
        {
            t = (Template) globalCache.get(template);
            tl = t.getTemplateLoader();

            // The template knows whether it needs to be checked
            // or not, and the template's loader can check to
            // see if the source has been modified. If both
            // these conditions are true then we must reload
            // the input stream and parse it to make a new
            // AST for the template.
            
            if (t.requiresChecking() && tl.isSourceModified(t))
            {
                try
                {
                    is = tl.getTemplateStream(template);
                    t.setDocument(parse(is));
                    return t;
                }
                catch (Exception e)
                {
                    error(e);
                }
            }
            return t;
        }
        else
        {
            try
            {
                t = new Template();
                t.setName(template);
                
                // Now we have to try to find the appropriate
                // loader for this template. We have to cycle through
                // the list of available template loaders and see
                // which one gives us a stream that we can use to
                // make a template with.
                
                for (int i = 0; i < templateLoaders.size(); i++)
                {
                    tl = (TemplateLoader) templateLoaders.get(i);
                    is = tl.getTemplateStream(template);
                    
                    // If we get an InputStream then we have found
                    // our loader.
                    if (is != null)
                        break;
                }
                
                // Return null if we can't find a template.
                if (is == null)
                    return null;
                
                t.setLastModified(tl.getLastModified(t));
                t.setModificationCheckInterval(tl.getModificationCheckInterval());
                t.setTemplateLoader(tl);
                t.setDocument(parse(is));
                t.touch();
                
                // Place the template in the cache if the template
                // loader says to.
                
                if (tl.useCache())
                    globalCache.put(template, t);
            }
            catch (Exception e)
            {
                error(e);
            }
        }
        return t;
    }

    /**
     *  Get an integer property with a default value
     */
    public static int getInt( String property, int iDef )
    {
        return VelocityResources.getInt( property, iDef );
    }

    /**
     *  Get an integer property 
     */
    public static int getInt( String property )
    {
        return VelocityResources.getInt( property  );
    }

    /**
     * Get a boolean property.
     */
    public static boolean getBoolean(String property)
    {
        return VelocityResources.getBoolean( property );
    }

    /**
     * Get a string property.
     */
    public static String getString(String property)
    {
        return VelocityResources.getString( property );
    }
    /**
     * Get a string property. with a default value
     */
    public static String getString(String property, String defaultValue)
    {
        String prop = getString( property );
        return (prop == null ? defaultValue : prop);
    }

    private static void log(String message)
    {
        if (logger != null)
            logger.info(message);
        else
            pendingMessages.append(message);
    }

    /** Log a warning message */
    public static void warn(Object message)
    {
        String out = null;
        if (message instanceof Throwable || message instanceof Exception)
            out = StringUtils.stackTrace((Throwable)message);
        else
            out = message.toString();    
        log(WARN + out);
    }
    
    /** Log an info message */
    public static void info(Object message)
    {
        String out = null;
        if (message instanceof Throwable || message instanceof Exception)
            out = StringUtils.stackTrace((Throwable)message);
        else
            out = message.toString();    
        log(INFO + out);
    }
    
    /** Log an error message */
    public static void error(Object message)
    {
        String out = null;
        if (message instanceof Throwable || message instanceof Exception)
            out = StringUtils.stackTrace((Throwable)message);
        else
            out = message.toString();    
        log(ERROR + out);
    }
    
    /** Log a debug message */
    public static void debug(Object message)
    {
        if (DEBUG_ON)
            log(DEBUG + message.toString());
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(fileToURL(args[0]));
    }
}
