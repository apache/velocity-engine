package org.apache.velocity.texen;

/*
 * Copyright (c) 1997-2000 The Java Apache Project.  All rights reserved.
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
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine", "Turbine",
 *    "Apache Turbine", "Turbine Project", "Apache Turbine Project" and
 *    "Java Apache Project" must not be used to endorse or promote products
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

import java.io.*;
import java.util.*;

import org.apache.velocity.Context;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.Runtime;

import org.apache.velocity.texen.util.BaseUtil;

/**
 * A text/code generator class
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Generator.java,v 1.8 2000/12/20 06:29:14 jvanzyl Exp $ 
 */
public class Generator
{
    public static final String OUTPUT_PATH = "output.path";
    public static final String TEMPLATE_PATH = "template.path";
    
    private static final String DEFAULT_TEXEN_PROPERTIES =
        "org/apache/velocity/texen/defaults/texen.properties";

    private Properties props = new Properties();
    private Context controlContext;
    
    private static Generator instance = new Generator();

    private Generator()
    {
        setDefaultProps();
    }

    /**
     * Create a new generator object with default properties
     */
    public static Generator getInstance()
    {
        return instance;
    }
    
    /**
     * Create a new generator object with properties loaded from
     * a file.  If the file does not exist or any other exception
     * occurs during the reading operation the default properties
     * are used.
     */
    public Generator (String propFile)
    {
        try
        {
            FileInputStream fi = new FileInputStream (propFile);
            BufferedInputStream bi = new BufferedInputStream (fi);
            try
            {
                props.load (bi);
            }
            finally
            {
                bi.close();
            }
        }
        catch (Exception e)
        {
            // If something goes wrong we use default properties
            setDefaultProps();
        }
    }
    
    /**
     * Create a new Generator object with a given property
     * set.  The property set will be duplicated.
     */
    public Generator (Properties props)
    {
        this.props = (Properties)props.clone();
    }
    
    
    /**
     * Set default properties
     */
    protected void setDefaultProps()
    {
        ClassLoader classLoader = Runtime.class.getClassLoader();
        try
        {
            InputStream inputStream = classLoader.getResourceAsStream(
                DEFAULT_TEXEN_PROPERTIES);
            
            props.load( inputStream );
        }
        catch (Exception ioe)
        {
            System.err.println("Cannot get default properties!");
        }
    }
    
    /**
     * Set the template path, where Texen will look
     * for Velocity templates.
     */
    public void setTemplatePath(String templatePath)
    {
        props.put(TEMPLATE_PATH, templatePath);
    }

    /**
     * Get the template path.
     */
    public String getTemplatePath()
    {
        return props.getProperty(TEMPLATE_PATH);
    }

    /**
     * Set the output path for the generated
     * output.
     */
    public void setOutputPath(String outputPath)
    {
        props.put(OUTPUT_PATH, outputPath);
    }

    /**
     * Get the output path for the generated
     * output.
     */
    public String getOutputPath()
    {
        return props.getProperty(OUTPUT_PATH);
    }

    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     */ 
    public String parse (String input, String output) throws Exception
    {
        return this.parse (input,output,null,null);
    }
    
    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * You can add objects to the context with the objs Hashtable.
     */ 
    public String parse (String input, String output, String objName, Object obj) 
        throws Exception
    {
        
        if (objName != null && obj != null)
            controlContext.put(objName, obj);
        
        Template template = Runtime.getTemplate(input);
        
        if (output == null || output.equals(""))
        {
            StringWriter sw = new StringWriter();
            template.merge (controlContext,sw);
            return sw.toString();
        }
        else
        {
            FileWriter fw = new FileWriter (getOutputPath() +
                File.separator + output);
            template.merge (controlContext,fw);
            fw.close();
            
            return "";
        }
    }

    public String parse (String controlTemplate, Context controlContext)
        throws Exception
    {
        this.controlContext = controlContext;
        fillContextDefaults(this.controlContext);
        fillContextProperties(this.controlContext);
        
        Template template = Runtime.getTemplate(controlTemplate);
        StringWriter sw = new StringWriter();
        template.merge (controlContext,sw);
        
        return sw.toString();
    }


    /**
     * Create a new context and fill it with the elements of the
     * objs Hashtable.  Default objects and objects that comes from
     * the properties of this Generator object is also added.
     */ 
    protected Context getContext (Hashtable objs)
    {
        fillContextHash (controlContext,objs);
        return controlContext;
    }

    /** 
     * Add all the contents of a Hashtable to the context
     */
    protected void fillContextHash (Context context, Hashtable objs)
    {
        Enumeration enum = objs.keys();
        while (enum.hasMoreElements())
        {
            String key = enum.nextElement().toString();
            context.put (key, objs.get(key));
        }
    }
    

    /**
     * Add properties that will aways be in the context by default
     */
    protected void fillContextDefaults (Context context)
    {
        context.put ("generator", instance);
        context.put ("outputDirectory", getOutputPath());
    }
    
    /**
     * Add objects to the context from the current properties
     */
    protected void fillContextProperties (Context context)
    {
        Enumeration enum = props.propertyNames();
        
        while (enum.hasMoreElements())
        {
            String nm = (String)enum.nextElement();
            if (nm.startsWith ("context.objects."))
            {
                
                String contextObj = props.getProperty (nm);
                int colon = nm.lastIndexOf ('.');
                String contextName = nm.substring (colon+1);
                
                try
                {
                    Class cls = Class.forName (contextObj);
                    BaseUtil b = (BaseUtil)cls.newInstance();
                    b.init();
                    context.put (contextName,b);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //TO DO: Log Something Here
                }
            }
        }
    }
}
