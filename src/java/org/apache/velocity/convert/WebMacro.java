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

import org.apache.oro.text.perl.Perl5Util;
import org.apache.velocity.util.StringUtils;

/**
 * This class will convert a WebMacro template to
 * a Velocity template.
 */
public class WebMacro
{
    public static void main(String args[])
    {
        String orignalTemplate;
        Perl5Util perl;
        
        /*
         * The regexes to use for substition. The regexes come
         * in pairs. The first is the string to match, the
         * second is the substitution to make.
         */
        String[] res = 
        {
            // Remove all #begin statements.
            "#begin\\n", 
            "",
            
            // Remove the "{" for the start of block directives,
            // Velocity doesn't use them.
            "(#\\w+\\s+.*)\\n\\s*\\{",
            "$1",

            // Change the "}" to #end. Have to get more
            // sophisticated here. Will assume either {}
            // and no javascript, or #begin/#end with the
            // possibility of javascript.
            "}",
            "#end",
            
            // Convert WM style if/else to Velocity style.
            "#end.*\\n\\s*(#else)",
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

        if (args.length < 1)
        {
            System.err.println("Usage: convert-wm <template>.wm | directory");
            System.exit(1);
        }

        orignalTemplate = StringUtils.fileContentsToString(args[0]);
        perl = new Perl5Util();

        for (int i = 0; i < res.length; i += 2)
            while (perl.match("/" + res[i] + "/", orignalTemplate))
                orignalTemplate = perl.substitute(
                    "s/" + res[i] + "/" + res[i+1] + "/", orignalTemplate);

        System.out.println(orignalTemplate);
    }
}
