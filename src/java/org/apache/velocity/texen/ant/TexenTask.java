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

import java.util.Map;
import java.util.Hashtable;

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
 * @version $Id: TexenTask.java,v 1.2 2000/11/03 15:28:39 jvanzyl Exp $
 */

public abstract class TexenTask extends Task
{
    protected String controlTemplate;
    protected String templatePath;
    protected String outputDirectory;

    protected Hashtable options = new Hashtable();

    /**
     * Set the output directory.  This directory must exist.
     */
    public void setControlTemplate (String controlTemplate)
    {
        this.controlTemplate = controlTemplate;
    }

    public String getControlTemplate()
    {
        return controlTemplate;
    }

    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }
    
    public String getTemplatePath()
    {
        return templatePath;
    }        

    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }
    
    public String getOutputDirectory()
    {
        return outputDirectory;
    }        

    public void setOption(String option)
    {
        String optionName = option.substring(0, option.indexOf(":") - 1);
        String optionValue = option.substring(option.indexOf(":"));
        
        options.put(optionName, optionValue);
    }

    public String getOption(String optionName)
    {
        return (String) options.get(optionName);
    }

    public abstract Context initControlContext();
    
    /**
     * Execute the input script with WM
     */
    public void execute () throws BuildException
    {
        // Make sure the template path is set.
        if (templatePath == null)
            throw new BuildException("The template path needs to be " +
                                     "defined! Texen can't run.");

        if (controlTemplate == null)
            throw new BuildException("The control template needs to be " +
                                     "defined! Texen can't run.");

        try
        {
            // Setup the Velocity Runtime.
            Runtime.setDefaultProperties();
            Runtime.setProperty(Runtime.TEMPLATE_PATH, templatePath);
            Runtime.init();
        
            // Create the text generator.
            Generator generator = Generator.getInstance();
            generator.setProperty(Generator.PATH_INPUT,templatePath);
            
            System.out.println(generator.parse(controlTemplate, initControlContext()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
