package org.apache.velocity.anakia;

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

// JDK Stuff
import java.io.*;

// Ant Stuff
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

// JDOM Stuff
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

// Velocity Stuff
import org.apache.velocity.*;
import org.apache.velocity.runtime.Runtime;

/**
    The purpose of this Ant Task is to allow you to use 
    Velocity as an XML transformation tool like XSLT is.
    So, instead of using XSLT, you will be able to use this 
    class instead to do your transformations. It works very
    similar in concept to Ant's &lt;style&gt; task.
    <p>
    This class is still a work in progress, so more documentation
    will be following soon.
    <p>
    The name Anakia is just a cool name that I liked so I used
    it for this project.
    
    @author <a href="jon@latchkey.com">Jon S. Stevens</a>
    @version $Id: AnakiaTask.java,v 1.2 2000/11/22 06:56:34 jon Exp $
*/
public class AnakiaTask extends MatchingTask
{
    /** Default SAX Driver class to use */
    private static final String DEFAULT_SAX_DRIVER_CLASS =
        "org.apache.xerces.parsers.SAXParser";

    /** <code>{@link SAXBuilder}</code> instance to use */
    private SAXBuilder builder;

    /** the destination directory */
    private File destDir = null;
    /** the base directory */
    private File baseDir = null;

    /** the style= attribute */
    private String style = null;
    /** the File to the style file */
    private File styleFile = null;
    /** the projectFile= attribute */
    private String projectAttribute = null;
    /** the File for the project.xml file */
    private File projectFile = null;

    /** last modified of the style sheet */
    private long styleSheetLastModified = 0;

    /** the default output extension is .html */
    private String extension = ".html";

    /**
        Constructor creates the SAXBuilder.
    */
    public AnakiaTask()
    {
        builder = new SAXBuilder(DEFAULT_SAX_DRIVER_CLASS);
    }

    /**
     * Set the base directory.
    **/
    public void setBasedir(File dir)
    {
        baseDir = dir;
    }
    /**
     * Set the destination directory into which the VSL result
     * files should be copied to
     * @param dirName the name of the destination directory
    **/
    public void setDestdir(File dir)
    {
        destDir = dir;
    }
    /**
        Allow people to set the default output file extension
    */
    public void setExtension(String extension)
    {
        this.extension = extension;
    }
    /**
        Allow people to set the path to the .vsl file
    */
    public void setStyle(String style)
    {
        this.style = style;
    }
    /**
        Allow people to set the path to the project.xml file
    */
    public void setProjectFile(String projectAttribute)
    {
        this.projectAttribute = projectAttribute;
    }

    /**
        Main body of the application
    */
    public void execute () throws BuildException
    {
        DirectoryScanner scanner;
        String[]         list;
        String[]         dirs;

        if (baseDir == null)
        {
            baseDir = project.resolveFile(".");
        }
        if (destDir == null )
        {
            String msg = "destdir attribute must be set!";
            throw new BuildException(msg);
        }
        if (projectAttribute == null) 
        {
            throw new BuildException("projectFile attribute must be set!");
        }
        if (style == null) 
        {
            throw new BuildException("style attribute must be set!");
        }
         
        log("Transforming into: " + destDir.getAbsolutePath(), Project.MSG_INFO);

        // projectFile relative to baseDir
        projectFile = new File(baseDir, projectAttribute);
        
        try
        {
            // initialize Velocity
            Runtime.init(new File("velocity.properties").getAbsolutePath());
            // get the last modification of the VSL stylesheet
            styleSheetLastModified = Runtime.getTemplate(style).getLastModified();
        }
        catch (Exception e)
        {
            log("Error: " + e.toString(), Project.MSG_INFO);
        }
        
        // find the files/directories
        scanner = getDirectoryScanner(baseDir);

        // get a list of files to work on
        list = scanner.getIncludedFiles();
        for (int i = 0;i < list.length; ++i)
        {
            process( baseDir, list[i], destDir );
        }
    }    
    
    /**
        Process an XML file using Velocity
    */
    private void process(File baseDir, String xmlFile, File destDir)
        throws BuildException
    {
        File   outFile=null;
        File   inFile=null;
        Writer writer = null;
        try
        {
            // the current input file relative to the baseDir
            inFile = new File(baseDir,xmlFile);
            // the output file relative to basedir
            outFile = new File(destDir,xmlFile.substring(0,xmlFile.lastIndexOf('.'))+extension);

            // only process files that have changed
            if (inFile.lastModified() > outFile.lastModified() ||
                    styleSheetLastModified > outFile.lastModified())
            {
                ensureDirectoryFor( outFile );

                //-- command line status
                log("Input:  " + xmlFile, Project.MSG_INFO );
                log("Output: " + outFile, Project.MSG_INFO );
                // Build the JDOM Document
                Document doc = builder.build(inFile);
                // Build the Project file document
                // FIXME: this should happen in the execute method since
                // it really only needs to be done once
                Document project = builder.build(projectFile);
    
                // Shove things into the Context
                Context context = new Context();
                context.put ("jdom", doc);
                context.put ("project", project);
    
                // Process the VSL template with the context and write out
                // the result as the outFile.
                writer = new BufferedWriter(new FileWriter(outFile));
                // get the template to process
                Template template = Runtime.getTemplate(style);
                template.merge(context, writer);
            }
        }
        catch (JDOMException e)
        {
            if (e.getRootCause() != null)
            {
                e.getRootCause().printStackTrace();
            }
            else
            {
                e.printStackTrace();
            }
//            log("Failed to process " + inFile, Project.MSG_INFO);
            if (outFile != null ) outFile.delete();
        }
        catch (Exception e)
        {
//            log("Failed to process " + inFile, Project.MSG_INFO);
            if (outFile != null ) outFile.delete();
            e.printStackTrace();
        }        
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.flush();
                    writer.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }
    /**
        create directories as needed
    */
    private void ensureDirectoryFor( File targetFile ) throws BuildException {
        File directory = new File( targetFile.getParent() );
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new BuildException("Unable to create directory: " 
                                         + directory.getAbsolutePath() );
            }
        }
    }
}    