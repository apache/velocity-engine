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
//import org.apache.velocity.runtime.directive.Set;
import org.apache.velocity.runtime.directive.Dummy;

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
    /** How often to check for modified templates. */
    public static final String TEMPLATE_MOD_CHECK_INTERVAL = 
        "template.modificationCheckInterval";

    private final static String WARN  = "  [warn] ";
    private final static String INFO  = "  [info] ";
    private final static String DEBUG = " [debug] ";
    private final static String ERROR = " [error] ";

    private static TemplateLoader templateLoader;
    private final static boolean DEBUG_ON = true;
    private static Logger logger;

    private static Parser parser;

    private static boolean initialized;

    public synchronized static void init(String properties)
        throws Exception
    {
        if (! initialized)
        {
        try
        {
            Configuration.setPropertiesFile(properties);
        
            // Initialize the logger.
            logger = LogKit.createLogger("velocity", 
                getString(RUNTIME_LOG), "DEBUG");
                
            // I'm not sure how else to set the format
            // for the logfile? The time formatting just returns
            // a long and that's hard coded in the log package.
            // I should fix that.
            LogTarget[] t = logger.getLogTargets();            
            ((FileOutputLogTarget)t[0])
                .setFormat("%5.5{time} %{message}\\n%{throwable}" );
            
            info("Logging system initialized.");
        
            // Create the template loader.
            templateLoader = TemplateFactory
                .getLoader(getString(TEMPLATE_LOADER));
            
            // Initialize the template loader if there
            // is a real path set for the template.path
            // property. Otherwise defer initialization
            // of the template loader because it is going
            // to be set by some external mechanism. Turbine
            // for example.
            if (! getString(TEMPLATE_PATH).equals("system"))
                templateLoader.init();

            // put this in a method and make a pool of
            // parsers.
            parser = new Parser();
            Hashtable directives = new Hashtable();
            directives.put("foreach", new Foreach());
            directives.put("dummy", new Dummy());
            parser.setDirectives(directives);

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

    public static void setProperty(String key, String value)
    {
        Configuration.setProperty(key, value);
    }        

    // Used by the template class.
    public static SimpleNode parse(InputStream inputStream)
        throws Exception
    {
        return parser.parse(inputStream);
    }

    public static void initTemplateLoader()
    {
        templateLoader.init();
    }

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

    // Runtime properties methods

    public static boolean getBoolean(String property)
    {
        return Configuration.getBoolean(property);    
    }

    public static String getString(String property)
    {
        return Configuration.getString(property);    
    }

    // Runtime logging methods.

    public static void warn(Object message)
    {
        logger.warn(WARN + message.toString());
    }
    
    public static void info(Object message)
    {
        logger.info(INFO + message.toString());
    }
    
    public static void error(Object message)
    {
        logger.error(ERROR + message.toString());
    }
    
    public static void debug(Object message)
    {
        if (DEBUG_ON)
            logger.debug(DEBUG + message.toString());
    }
}
