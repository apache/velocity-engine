package org.apache.velocity.util;

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
import java.io.FileReader;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class provides some methods for dynamically
 * invoking methods in objects, and some string
 * manipulation methods used by torque. The string
 * methods will soon be moved into the turbine
 * string utilities class.
 *
 *  @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 *  @version $Id: StringUtils.java,v 1.2 2000/10/24 23:34:41 jvanzyl Exp $
 */
public class StringUtils
{
    /**
     * Create a string array from a string separated by delim
     *
     * @param line the line to split
     * @param delim the delimter to split by
     * @return a string array of the split fields
     */
    public static String [] split(String line, String delim)
    {
        Vector v = new Vector();
        StringTokenizer t = new StringTokenizer(line, delim);
        while (t.hasMoreTokens())
            v.addElement(t.nextToken());

        String [] s = new String[v.size()];
        for (int i = 0; i < v.size(); i++)
            s[i] = (String) v.elementAt(i);

        return s;
    }

    public static StringBuffer stringSubstitution(String argStr,
            Hashtable vars)
    {
        StringBuffer argBuf = new StringBuffer();

        for (int cIdx = 0 ; cIdx < argStr.length();)
        {
            char ch = argStr.charAt(cIdx);

            switch (ch)
            {
                case '$':
                    StringBuffer nameBuf = new StringBuffer();
                    for (++cIdx ; cIdx < argStr.length(); ++cIdx)
                    {
                        ch = argStr.charAt(cIdx);
                        if (ch == '_' || Character.isLetterOrDigit(ch))
                            nameBuf.append(ch);
                        else
                            break;
                    }

                    if (nameBuf.length() > 0)
                    {
                        String value =
                                (String) vars.get(nameBuf.toString());

                        if (value != null)
                        {
                            argBuf.append(value);
                        }
                    }
                    break;

                default:
                    argBuf.append(ch);
                    ++cIdx;
                    break;
            }
        }

        return argBuf;
    }

    public static String fileContentsToString(String file)
    {
        String contents = "";
        
        File f = new File(file);
        
        if (f.exists())
        {
            try
            {
                FileReader fr = new FileReader(f);
                char[] template = new char[(int) f.length()];
                fr.read(template, 0, (int) f.length());
                contents = new String(template);
            }
            catch (Exception e)
            {
                System.out.println(e);
                e.printStackTrace();
            }
        }
        
        return contents;
    }
    
    /**
     * Remove/collapse multiple newline characters.
     */
    public static String collapseNewlines(String argStr)
    {
        char last = argStr.charAt(0);
        StringBuffer argBuf = new StringBuffer();

        for (int cIdx = 0 ; cIdx < argStr.length(); cIdx++)
        {
            char ch = argStr.charAt(cIdx);
            if (ch != '\n' || last != '\n')
            {
                argBuf.append(ch);
                last = ch;
            }
        }

        return argBuf.toString();
    }

    /**
     * Remove/collapse multiple spaces.
     */
    public static String collapseSpaces(String argStr)
    {
        char last = argStr.charAt(0);
        StringBuffer argBuf = new StringBuffer();

        for (int cIdx = 0 ; cIdx < argStr.length(); cIdx++)
        {
            char ch = argStr.charAt(cIdx);
            if (ch != ' ' || last != ' ')
            {
                argBuf.append(ch);
                last = ch;
            }
        }

        return argBuf.toString();
    }

    /**
       * Chop i characters off the end of a string.
       *
       * @param string String to chop
       * @param i Number of characters to chop
        */
    public static String chop(String string, int i)
    {
        return(string.substring(0, string.length() - i));
    }

    /**
      * Replaces all instances of oldString with newString in line.
      * Taken from the Jive forum package.
      */
    public static final String replace(String line, String oldString,
            String newString)
    {
        int i = 0;
        if ((i = line.indexOf(oldString, i)) >= 0)
        {
            char [] line2 = line.toCharArray();
            char [] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            while ((i = line.indexOf(oldString, i)) > 0)
            {
                buf.append(line2, j, i - j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(line2, j, line2.length - j);
            return buf.toString();
        }
        return line;
    }
}
