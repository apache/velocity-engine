package org.apache.velocity.texen.ant;

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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Map;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;

// Ant Stuff
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

// Velocity Stuff
import org.apache.velocity.Context;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.texen.Generator;

/**
 * An ant task for generating output by using Velocity
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: TexenTask.java,v 1.7 2000/11/23 17:48:00 jvanzyl Exp $
 */

public abstract class TexenTask extends Task
{
    /**
     * This is the control template that governs the output.
     * It may or may not invoke the services of worker
     * templates.
     */
    protected String controlTemplate;
    
    /**
     * This is where Velocity will look for templates
     * using the file template loader.
     */
    protected String templatePath;
    
    /**
     * This is where texen will place all the output
     * that is a product of the generation process.
     */
    protected String outputDirectory;
    
    /**
     * This is the file where the generated text
     * will be placed.
     */
    protected String outputFile;
    
    /**
     * These are properties that are fed into the
     * initial context from a properties file. This
     * is simply a convenient way to set some values
     * that you wish to make available in the context.
     *
     * These values are not critical, like the template path or
     * or output path, but allow a convenient way to
     * set a value that may be specific to a particular
     * generation task.
     *
     * For example, if you are generating scripts to allow
     * user to automatically create a database, then
     * you might want the $databaseName to be placed
     * in the initial context so that it is available
     * in a script that might look something like the
     * following:
     *
     * #!bin/sh
     * 
     * echo y | mysqladmin create $databaseName
     *
     * The value of $databaseName isn't critical to
     * output, and you obviously don't want to change
     * the ant task to simply take a database name.
     * So initial context values can be set with
     * properties file.
     */
    protected Properties contextProperties;

    /**
     * Get the control template for the
     * generating process.
     */
    public void setControlTemplate (String controlTemplate)
    {
        this.controlTemplate = controlTemplate;
    }

    /**
     * Get the control template for the
     * generating process.
     */
    public String getControlTemplate()
    {
        return controlTemplate;
    }

    /**
     * Set the path where Velocity will look
     * for templates using the file template
     * loader.
     */
    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }
    
    /**
     * Get the path where Velocity will look
     * for templates using the file template
     * loader.
     */
    public String getTemplatePath()
    {
        return templatePath;
    }        

    /**
     * Set the output directory. It will be
     * created if it doesn't exist.
     */
    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Get the output directory.
     */
    public String getOutputDirectory()
    {
        return outputDirectory;
    }        

    /**
     * Set the output file for the
     * generation process.
     */
    public void setOutputFile(String outputFile)
    {
        this.outputFile = outputFile;
    }
    
    /**
     * Get the output file for the
     * generation process.
     */
    public String getOutputFile()
    {
        return outputFile;
    }        

    /**
     * Set the context properties that will be
     * fed into the initial context be the
     * generating process starts.
     */
    public void setContextProperties(String file)
    {
        contextProperties = new Properties();
        
        try
        {
            contextProperties.load(new FileInputStream(file));
        }
        catch (Exception e)
        {
            contextProperties = null;
        }
    }

    /**
     * Set the context properties that will be
     * fed into the initial context be the
     * generating process starts.
     */
    public Properties getContextProperties()
    {
        return contextProperties;
    }

    public abstract Context initControlContext();
    
    /**
     * Execute the input script with WM
     */
    public void execute () throws BuildException
    {
        // Make sure the template path is set.
        if (templatePath == null)
            throw new BuildException("The template path needs to be defined!");

        if (controlTemplate == null)
            throw new BuildException("The control template needs to be defined!");

        if (outputDirectory == null)
            throw new BuildException("The output directory needs to be defined!");

        if (outputFile == null)
            throw new BuildException("The output file needs to be defined!");

        try
        {
            // Setup the Velocity Runtime.
            Runtime.setDefaultProperties();
            
            // This is strictly to allow vel to compile for now.
            // I need a new way to set what was the template path
            // now that templates streams can come from anywhere.
            Runtime.setSourceProperty(Runtime.FILE_TEMPLATE_PATH, templatePath);
            Runtime.init();

            // Create the text generator.
            Generator generator = Generator.getInstance();
            generator.setOutputPath(outputDirectory);
            generator.setTemplatePath(templatePath);
            
            // Make sure the output directory exists, if it doesn't
            // then create it.
            File file = new File(outputDirectory);
            if (! file.exists())
                file.mkdirs();
            
            String path = outputDirectory + File.separator + outputFile;
            System.out.println(path);
            FileWriter writer = new FileWriter(path);
            
            // The generator and the output path should
            // be placed in the init context here and
            // not in the generator class itself.
            
            Context c = initControlContext();
            
            // Feed all the options into the initial
            // control context so they are available
            // in the control/worker templates.
            
            if (contextProperties != null)
            {
                Enumeration e = contextProperties.propertyNames();
        
                while (e.hasMoreElements())
                {
                    String property = (String) e.nextElement();
                    String value = (String) contextProperties.get(property);
                    
                    // Now lets quickly check to see if what
                    // we have is numeric and try to put it
                    // into the context as an Integer.
                    try
                    {
                        c.put(property, new Integer(value)); 
                    }
                    catch (NumberFormatException nfe)
                    {
                        c.put(property, value);
                    }
                }
            }
            
            //c.put("generator", generator);
            
            writer.write(generator.parse(controlTemplate, c));
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
