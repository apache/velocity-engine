package org.apache.velocity.texen;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * A text/code generator class
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Generator.java,v 1.17 2001/08/07 22:31:50 geirm Exp $ 
 */
public class Generator
{
    /**
     * Where the texen output will placed.
     */
    public static final String OUTPUT_PATH = "output.path";
    
    /**
     * Where the velocity templates live.
     */
    public static final String TEMPLATE_PATH = "template.path";
    
    /**
     * Default properties file used for controlling the
     * tools placed in the context.
     */
    private static final String DEFAULT_TEXEN_PROPERTIES =
        "org/apache/velocity/texen/defaults/texen.properties";

    /**
     * Default properties used by texen.
     */
    private Properties props = new Properties();
    
    /**
     * Context used for generating the texen output.
     */
    private Context controlContext;

    /**
     * Keep track of the file writers used for outputting
     * to files. If we come across a file writer more
     * then once then the additional output will be
     * appended to the file instead of overwritting
     * the contents.
     */
    private Hashtable fileWriters = new Hashtable();

    /**
     * The generator tools used for creating additional
     * output withing the control template. This could
     * use some cleaning up.
     */
    private static Generator instance = new Generator();

    /**
     * Default constructor.
     */
    private Generator()
    {
        setDefaultProps();
    }

    /**
     * Create a new generator object with default properties.
     *
     * @return Generator generator used in the control context.
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
     *
     * @param String properties used to help populate the control context.
     * @return Generator generator used in the control context.
     */
    public Generator (String propFile)
    {
        try
        {
            BufferedInputStream bi = null;
            try
            {
                bi = new BufferedInputStream (new FileInputStream (propFile));
                props.load (bi);
            }
            finally
            {
                if (bi != null)
                {
                    bi.close();
                }
            }
        }
        catch (Exception e)
        {
            /*
             * If something goes wrong we use default properties
             */
            setDefaultProps();
        }
    }
    
    /**
     * Create a new Generator object with a given property
     * set. The property set will be duplicated.
     *
     * @param Properties properties object to help populate the control context.
     */
    public Generator (Properties props)
    {
        this.props = (Properties)props.clone();
    }
    
    /**
     * Set default properties.
     */
    protected void setDefaultProps()
    {
        ClassLoader classLoader = Velocity.class.getClassLoader();
        try
        {
            InputStream inputStream = null;
            try
            {
                inputStream = classLoader.getResourceAsStream(
                    DEFAULT_TEXEN_PROPERTIES);
            
                props.load( inputStream );
            }
            finally
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
        }
        catch (Exception ioe)
        {
            System.err.println("Cannot get default properties!");
        }
    }
    
    /**
     * Set the template path, where Texen will look
     * for Velocity templates.
     *
     * @param String template path for velocity templates.
     */
    public void setTemplatePath(String templatePath)
    {
        props.put(TEMPLATE_PATH, templatePath);
    }

    /**
     * Get the template path.
     *
     * @return String template path for velocity templates.
     */
    public String getTemplatePath()
    {
        return props.getProperty(TEMPLATE_PATH);
    }

    /**
     * Set the output path for the generated
     * output.
     *
     * @return String output path for texen output.
     */
    public void setOutputPath(String outputPath)
    {
        props.put(OUTPUT_PATH, outputPath);
    }

    /**
     * Get the output path for the generated
     * output.
     *
     * @return String output path for texen output.
     */
    public String getOutputPath()
    {
        return props.getProperty(OUTPUT_PATH);
    }

    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     *
     * @param String input template
     * @param String output file
     */ 
    public String parse (String inputTemplate, String outputFile) 
        throws Exception
    {
        return parse(inputTemplate, outputFile, null, null);
    }
    
    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * You can add objects to the context with the objs Hashtable.
     *
     * @param String input template
     * @param String output file
     * @param String id for object to be placed in the control context
     * @param String object to be placed in the context
     * @return String generated output from velocity
     */ 
    public String parse (String inputTemplate, 
                         String outputFile, 
                         String objectID, 
                         Object object)
        throws Exception
    {
        if (objectID != null && object != null)
        {
            controlContext.put(objectID, object);
        }            
        
        Template template = Velocity.getTemplate(inputTemplate);
        
        if (outputFile == null || outputFile.equals(""))
        {
            StringWriter sw = new StringWriter();
            template.merge (controlContext,sw);
            return sw.toString();
        }
        else
        {
            FileWriter fileWriter = null;
            
            if (fileWriters.get(outputFile) == null)
            {
                /*
                 * We have never seen this file before so create
                 * a new file writer for it.
                 */
                fileWriter = new FileWriter(
                    getOutputPath() + File.separator + outputFile);
                    
                /*
                 * Place the file writer in our collection
                 * of file writers.
                 */
                fileWriters.put(outputFile, fileWriter);                    
            }
            else
            {
                fileWriter = (FileWriter) fileWriters.get(outputFile);
            }                
            
            VelocityContext vc = new VelocityContext( controlContext );
            template.merge (vc,fileWriter);

            // commented because it is closed in shutdown();
            //fw.close();
            
            return "";
        }
    }

    /**
     * Parse the control template and merge it with the control
     * context. This is the starting point in texen.
     *
     * @param String control template
     * @param Context control context
     * @return String generated output
     */
    public String parse (String controlTemplate, Context controlContext)
        throws Exception
    {
        this.controlContext = controlContext;
        fillContextDefaults(this.controlContext);
        fillContextProperties(this.controlContext);
        
        Template template = Velocity.getTemplate(controlTemplate);
        StringWriter sw = new StringWriter();
        template.merge (controlContext,sw);
        
        return sw.toString();
    }


    /**
     * Create a new context and fill it with the elements of the
     * objs Hashtable.  Default objects and objects that comes from
     * the properties of this Generator object is also added.
     *
     * @param Hashtable objects to place in the control context
     * @return Context context filled with objects
     */ 
    protected Context getContext (Hashtable objs)
    {
        fillContextHash (controlContext,objs);
        return controlContext;
    }

    /** 
     * Add all the contents of a Hashtable to the context.
     *
     * @param Context context to fill with objects
     * @param Hashtable source of objects
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
     *
     * @param Context control context to fill with default values.
     */
    protected void fillContextDefaults (Context context)
    {
        context.put ("generator", instance);
        context.put ("outputDirectory", getOutputPath());
    }
    
    /**
     * Add objects to the context from the current properties.
     *
     * @param Context control context to fill with objects
     *                that are specified in the default.properties
     *                file
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
                    Object o = cls.newInstance();
                    context.put (contextName,o);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    //TO DO: Log Something Here
                }
            }
        }
    }

    /**
     * Properly shut down the generator, right now
     * this is simply flushing and closing the file
     * writers that we have been holding on to.
     */
    public void shutdown()
    {
        Iterator iterator = fileWriters.values().iterator();
        
        while(iterator.hasNext())
        {
            FileWriter fileWriter = (FileWriter) iterator.next();
                        
            try
            {
                fileWriter.flush();
                fileWriter.close();
            }
            catch (Exception e)
            {
                /* do nothing */
            }
        }
        // clear the file writers cache
        fileWriters.clear();
    }
}
