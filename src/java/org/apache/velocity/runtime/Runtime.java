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
import java.net.MalformedURLException;
import java.util.Hashtable;

import org.apache.log.LogKit;
import org.apache.log.Logger;
import org.apache.log.LogTarget;
import org.apache.log.output.FileOutputLogTarget;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.SimpleNode;

import org.apache.velocity.runtime.loader.TemplateFactory;
import org.apache.velocity.runtime.loader.TemplateLoader;

import org.apache.velocity.runtime.configuration.Configuration;

import org.apache.velocity.runtime.directive.Foreach;
import org.apache.velocity.runtime.directive.Dummy;

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
 */
public class Runtime
{
    /** Location of the log file */
    public static final String RUNTIME_LOG = "runtime.log";
    
    /** Location of templates */
    public static final String TEMPLATE_PATH = "template.path";
    
    /** Template loader to be used */
    public static final String TEMPLATE_LOADER = "template.loader";
    
    /** Specify template caching true/false */
    public static final String TEMPLATE_CACHE = "template.cache";
    
    /** Template processor to use. A processor is associated
      * with a parser generator tool, and its utilities
      * used for parsing.
      */
    public static final String TEMPLATE_PROCESSOR = "template.processor";
    
    /** The encoding to use for the template */
    public static final String TEMPLATE_ENCODING = "template.encoding";
    
    /** Enable the speed up provided by FastWriter */
    public static final String TEMPLATE_ASCIIHACK = "template.asciihack";

    /** Initial counter value in #foreach directives */
    public static final String COUNTER_NAME = "counter.name";

    /** Initial counter value in #foreach directives */
    public static final String COUNTER_INITIAL_VALUE = "counter.initial.value";

    /** Initial counter value in #foreach directives */
    public static final String DEFAULT_CONTENT_TYPE = "default.contentType";

    /** How often to check for modified templates. */
    public static final String TEMPLATE_MOD_CHECK_INTERVAL = 
        "template.modificationCheckInterval";

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
    
    /** The Runtime logger */
    private static Logger logger;
    
    /** 
      * The Runtime parser. This has to be changed to
      * a pool of parsers!
      */
    private static Parser parser;
    
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
     * Initializes the Velocity Runtime.
     */
    public synchronized static void init(String properties)
        throws Exception
    {
        if (! initialized)
        {
            try
            {
                Configuration.setPropertiesFile(properties);
        
                initializeLogger();
                initializeTemplateLoader();           
                initializeParserPool();
                
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
        Configuration.setProperty(key, value);
    }        

    /**
     * Initialize the Velocity logging system.
     */
    public static void initializeLogger() throws
        MalformedURLException
    {
        if (!getString(RUNTIME_LOG).equals("system"))
        {
            // Let's look at the log file entry and
            // correct it if it is not a property 
            // fomratted URL.
            String logFile = getString(RUNTIME_LOG);
            
            if (! logFile.startsWith("file"))
                logFile = "file://" + logFile;
            
            // Initialize the logger.
            logger = LogKit.createLogger("velocity", 
                getString(RUNTIME_LOG), "DEBUG");
                
            LogTarget[] t = logger.getLogTargets();            
            ((FileOutputLogTarget)t[0])
                .setFormat("%5.5{time} %{message}\\n%{throwable}" );
        
            if (pendingMessages.length() > 0)
            {
                logger.info(pendingMessages.toString());
            }
        }
    }

    /**
     * Initialize the template loader if there
     * is a real path set for the template.path
     * property. Otherwise defer initialization
     * of the template loader because it is going
     * to be set by some external mechanism: Turbine
     * for example.
     */
    public static void initializeTemplateLoader()
        throws Exception
    {
        if (!getString(TEMPLATE_PATH).equals("system"))
        {
            templateLoader = TemplateFactory
                .getLoader(getString(TEMPLATE_LOADER));
            
            templateLoader.init();
        }            
    }
    
    /**
     * Initializes the Velocity parser pool.
     * This still needs to be implemented.
     */
    private static void initializeParserPool()
    {
        // put this in a method and make a pool of
        // parsers.
        parser = new Parser();
        Hashtable directives = new Hashtable();
        directives.put("foreach", new Foreach());
        directives.put("dummy", new Dummy());
        parser.setDirectives(directives);
    }

    /**
     * Parse the input stream and return the root of
     * AST node structure.
     */
    public synchronized static SimpleNode parse(InputStream inputStream)
        throws Exception
    {
        return parser.parse(inputStream);
    }
    
    /**
     * Get a template via the TemplateLoader.
     */
    public static Template getTemplate(String template)
    {
        try
        {
            return templateLoader.getTemplate(template);
        }
        catch (Exception e)
        {
            error(e);
            return null;
        }            
    }

    /**
     * Get a boolean property.
     */
    public static boolean getBoolean(String property)
    {
        return Configuration.getBoolean(property);    
    }

    /**
     * Get a string property.
     */
    public static String getString(String property)
    {
        return Configuration.getString(property);    
    }
    /**
     * Get a string property. with a default value
     */
    public static String getString(String property, String defaultValue)
    {
        return Configuration.getString(property, defaultValue);    
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
        log(WARN + message.toString());
    }
    
    /** Log an info message */
    public static void info(Object message)
    {
        log(INFO + message.toString());
    }
    
    /** Log an error message */
    public static void error(Object message)
    {
        log(ERROR + message.toString());
    }
    
    /** Log a debug message */
    public static void debug(Object message)
    {
        if (DEBUG_ON)
            log(DEBUG + message.toString());
    }
}
