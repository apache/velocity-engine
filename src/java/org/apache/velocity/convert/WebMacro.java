package org.apache.velocity.convert;

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

import java.io.File;
import java.io.FileWriter;

import org.apache.oro.text.perl.Perl5Util;
import org.apache.velocity.util.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

/**
 * This class will convert a WebMacro template to
 * a Velocity template. Uses the ORO Regexp package to do the 
 * rewrites. Note, it isn't 100% perfect, but will definitely get
 * you about 99.99% of the way to a converted system. Please
 * see the website documentation for more information on how to use
 * this class.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: WebMacro.java,v 1.19 2001/07/10 19:19:27 geirm Exp $ 
 */
public class WebMacro
{
    protected static final String VM_EXT = ".vm";
    protected static final String WM_EXT = ".wm";

    /**
     * The regexes to use for line by line substition. The regexes
     * come in pairs. The first is the string to match, the second is
     * the substitution to make.
     */
    protected static String[] perLineREs =
    {
        // Make #if directive match the Velocity directive style.
        "#if\\s*[(]\\s*(.*\\S)\\s*[)]\\s*(#begin|{)[ \\t]?",
        "#if( $1 )",

        // Remove the WM #end #else #begin usage.
        "[ \\t]?(#end|})[ \\t]*\n(\\s*)#else\\s*(#begin|{)[ \\t]?(\\w)",
        "$2#else#**#$4", // avoid touching followup word with embedded comment
        "[ \\t]?(#end|})[ \\t]*\n(\\s*)#else\\s*(#begin|{)[ \\t]?",
        "$2#else",
        "(#end|})(\\s*#else)\\s*(#begin|{)[ \\t]?",
        "$1\n$2",

        // Convert WM style #foreach to Velocity directive style.
        "#foreach\\s+(\\$\\w+)\\s+in\\s+(\\$[^\\s#]+)\\s*(#begin|{)[ \\t]?",
        "#foreach( $1 in $2 )",

        // Convert WM style #set to Velocity directive style.
        "#set\\s+(\\$[^\\s=]+)\\s*=\\s*([\\S \\t]+)",
        "#set( $1 = $2 )",
        "(##[# \\t\\w]*)\\)", // fix comments included at end of line
        ")$1",

        // Convert WM style #parse to Velocity directive style.
        "#parse\\s+([^\\s#]+)[ \\t]?",
        "#parse( $1 )",

        // Convert WM style #include to Velocity directive style.
        "#include\\s+([^\\s#]+)[ \\t]?",
        "#include( $1 )",

        // Convert WM formal reference to VTL syntax.
        "\\$\\(([^\\)]+)\\)",
        "${$1}",
        "\\${([^}\\(]+)\\(([^}]+)}\\)", // fix encapsulated brakets: {(})
        "${$1($2)}",

        // Velocity currently does not permit leading underscore.
        "\\$_",
        "$l_",
        "\\${(_[^}]+)}", // within a formal reference
        "${l$1}",

        // Eat semi-colons in (converted) VTL #set directives.
        "(#set\\s*\\([^;]+);(\\s*\\))",
        "$1$2",

        // Convert explicitly terminated WM statements to VTL syntax.
        "(^|[^\\\\])\\$(\\w[^=\n;'\"]*);",
        "$1${$2}",

        // Change extensions when seen.
        "\\.wm",
        ".vm"
    };
    
    /**
     * Iterate through the set of find/replace regexes
     * that will convert a given WM template to a VM template
     */
    public void convert(String target)
    {
        File file = new File(target);
        
        if (!file.exists())
        {
            System.err.println
                ("The specified template or directory does not exist");
            System.exit(1);
        }
        
        if (file.isDirectory())
        {
            String basedir = file.getAbsolutePath();
            String newBasedir = basedir + VM_EXT;

            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(basedir);
            ds.addDefaultExcludes();
            ds.scan();
            String[] files = ds.getIncludedFiles();
            
            for (int i = 0; i < files.length; i++)
            {
                writeTemplate(files[i], basedir, newBasedir);
            }
        }
        else
        {
            writeTemplate(file.getAbsolutePath(), "", "");
        }
    }

    /**
     * Write out the converted template to the given named file
     * and base directory.
     */
    private boolean writeTemplate(String file, String basedir,
                                  String newBasedir)
    {
        if (file.indexOf(WM_EXT) < 0)
        {
            return false;
        }
    
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
            template = basedir + File.separator + file;
            templateDir = newBasedir + extractPath(file);

            outputDirectory = new File(templateDir);
                
            if (! outputDirectory.exists())
            {
                outputDirectory.mkdirs();
            }
                
            newTemplate = newBasedir + File.separator + convertName(file);
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

    /**
     * Gets the path segment of the full path to a file (i.e. one
     * which originally included the file name).
     */
    private final String extractPath(String file)
    {
        int lastSepPos = file.lastIndexOf(File.separator);
        return (lastSepPos == -1 ? "" :
                File.separator + file.substring(0, lastSepPos));
    }

    /**
     * Simple extension conversion of .wm to .vm
     */
    private String convertName(String name)
    {
        if (name.indexOf(WM_EXT) > 0)
        {
            return name.substring(0, name.indexOf(WM_EXT)) + VM_EXT;
        }
        else
        {
            return name;
        }
    }

    /**
     * How to use this little puppy :-)
     */
    private static final void usage()
    {
        System.err.println("Usage: convert-wm <template.wm | directory>");
        System.exit(1);
    }

    /**
     * Apply find/replace regexes to our WM template
     */
    public String convertTemplate(String template)
    {
        String contents = StringUtils.fileContentsToString(template);

        // Overcome Velocity 0.71 limitation.
        // HELP: Is this still necessary?
        if (!contents.endsWith("\n"))
        {
            contents += "\n";
        }

        // Convert most markup.
        Perl5Util perl = new Perl5Util();
        for (int i = 0; i < perLineREs.length; i += 2)
        {
            contents = perl.substitute(makeSubstRE(i), contents);
        }

        // Convert closing curlies.
        if (perl.match("m/javascript/i", contents))
        {
            // ASSUMPTION: JavaScript is indented, WM is not.
            contents = perl.substitute("s/\n}/\n#end/g", contents);
        }
        else
        {
            contents = perl.substitute("s/(\n\\s*)}/$1#end/g", contents);
            contents = perl.substitute("s/#end\\s*\n\\s*#else/#else/g",
                                       contents);
        }

        return contents;
    }

    /**
     * Makes a Perl 5 regular expression for use by ORO.
     */
    private final String makeSubstRE(int i)
    {
        return ("s/" + perLineREs[i] + '/' + perLineREs[i + 1] + "/g");
    }

    /**
     * Main hook for the conversion process.
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            for (int x=0; x < args.length; x++)
            {
                WebMacro converter = new WebMacro();
                converter.convert(args[x]);
            }
        }
        else
        {
            usage();
        }
    }
}
