package org.apache.velocity.convert;

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

import java.io.File;
import java.io.FileWriter;

import org.apache.oro.text.perl.Perl5Util;
import org.apache.velocity.util.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

/**
 * This class will convert a WebMacro template to
 * a Velocity template.
 */
public class WebMacro
{
    protected String orignalTemplate;
    protected Perl5Util perl;
    protected String pathSeparator = File.separator;
    
    protected final static String VM_EXT = ".vm";
    protected final static String WM_EXT = ".wm";

    /*
     * The regexes to use for substition. The regexes come
     * in pairs. The first is the string to match, the
     * second is the substitution to make.
     */
    protected String[] res =
    {
        // Remove all #begin statements.
        "#begin\\n", 
        "",
            
        // Remove the "{" for the start of block directives,
        // Velocity doesn't use them.
        "(#\\w+\\s*.*)\\n?\\s*\\{",
        "$1",

        // Change the "}" to #end. Have to get more
        // sophisticated here. Will assume either {}
        // and no javascript, or #begin/#end with the
        // possibility of javascript.
        "}",
        "#end",
            
        // Convert WM style if/else to Velocity style.
        "#end.*\\n?\\s*(#else)",
        "$1",
            
        // Convert WM style #foreach to Velocity #foreach.
        "#foreach\\s+(\\$\\w+)\\s+in\\s+(\\$\\w+)",
        "#foreach ($1 in $2)",
        
        // Change parse to include.
        "#parse",
        "#include",
            
        // Change extensions when seen.
        "\\.wm",
        ".vm"
    };
    
    public void convert(String args[])
    {
        if (args.length < 1)
            usage();
        
        File file = new File(args[0]);
        
        if (!file.exists())
        {
            System.err.println("The template or directory specified doesn't exist!");
            System.exit(1);
        }
        
        if (file.isDirectory())
        {
            String basedir = args[0];
            String newBasedir = basedir + VM_EXT;
            
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(basedir);
            ds.addDefaultExcludes();
            ds.scan();
            String[] files = ds.getIncludedFiles();
            
            for (int i = 0; i < files.length; i++)
                writeTemplate(files[i], basedir, newBasedir);
        }
        else
            writeTemplate(args[0], "", "");
    }

    private boolean writeTemplate(String file, String basedir, String newBasedir)
    {
        if (file.indexOf(WM_EXT) < 0)
            return false;
    
        System.out.println("Converting " + file + "...");
        
        String template;
        String templateDir;
        String newTemplate;
        File outputDirectory;
        
        if (basedir.length() == 0)
        {
            template = file;
            templateDir = "";
            newTemplate = convertName(file);
        }            
        else
        {
            template = basedir + pathSeparator + file;
            templateDir = newBasedir + pathSeparator + 
                file.substring(0, file.lastIndexOf(pathSeparator));
        
            outputDirectory = new File(templateDir);
                
            if (! outputDirectory.exists())
                outputDirectory.mkdirs();
                
            newTemplate = newBasedir + pathSeparator + 
                convertName(file);
        }            
        
        String convertedTemplate = convertTemplate(template);
                    
        try
        {
            FileWriter fw = new FileWriter(newTemplate);
            fw.write(convertedTemplate);
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    
        return true;
    }

    private String convertName(String name)
    {
        if (name.indexOf(WM_EXT) > 0)
            return name.substring(0, name.indexOf(WM_EXT)) + VM_EXT;
        else
            return name;
    }

    public void usage()
    {
        System.err.println("Usage: convert-wm <template>.wm | directory");
        System.exit(1);
    }

    public String convertTemplate(String template)
    {
        orignalTemplate = StringUtils.fileContentsToString(template);
        perl = new Perl5Util();

        for (int i = 0; i < res.length; i += 2)
            while (perl.match("/" + res[i] + "/", orignalTemplate))
                orignalTemplate = perl.substitute(
                    "s/" + res[i] + "/" + res[i+1] + "/", orignalTemplate);

        return orignalTemplate;
    }

    public static void main(String[] args)
    {
        WebMacro converter = new WebMacro();
        converter.convert(args);
    }
}
