package org.apache.velocity.texen.util;

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

import java.io.File;
import java.util.StringTokenizer;

/**
 * A string utility class for the texen text/code generator
 * Usually this class is only used from a Velcity context.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 */
public class StringUtil extends BaseUtil
{
    private static final String EOL = System.getProperty("line.separator");
    private static final int EOL_LENGTH = EOL.length();
    
    /**
     * Concatenates a list of objects as a String
     */
    public String concat (Object[] list)
    {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<list.length; i++)
        {
            sb.append (list[i].toString());
        }
        return sb.toString();
    }
    
    /**
     * Return a package name as a relative path name
     */
    static public String getPackageAsPath(String pckge)
    {
        return pckge.replace( '.', File.separator.charAt(0) ) + File.separator;
    }

  /**
   * Remove Underscores from a string and replaces first
   * Letters with Capitals.  foo_bar becomes FooBar
   */
    static public String removeUnderScores (String data)
    {
        String temp = null;
        StringBuffer out = new StringBuffer();
        temp = data;

        StringTokenizer st = new StringTokenizer(temp, "_");
        while (st.hasMoreTokens())
        {
            String element = (String) st.nextElement();
            out.append ( firstLetterCaps(element));
        }//while
        return out.toString();
    }

  /**
   * Makes the first letter caps and the rest lowercase
   */
    static public String firstLetterCaps ( String data )
    {
        String firstLetter = data.substring(0,1).toUpperCase();
        String restLetters = data.substring(1).toLowerCase();
        return firstLetter + restLetters;
    }

    /**
     * Chop i characters off the end of a string.
     * This method is sensitive to the EOL for a
     * particular platform. The EOL character will
     * considered a single character.
     *
     * @param string String to chop.
     * @param i Number of characters to chop.
     * @return String with processed answer.
     */
    public static String chop(String s, int i)
    {
        for (int j = 0; j < i; j++)
        {
            if (EOL.indexOf(s.substring(s.length() - 1)) > 0 && EOL_LENGTH == 2)
                s = s.substring(0, s.length() - 2);
            else
                s = s.substring(0, s.length() - 1);
        }            
    
        return s;
    }

    /**
     * Remove/collapse multiple spaces.
     *
     * @param argStr String to process.
     * @return String with processed answer.
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
     * Remove/collapse multiple newline characters.
     *
     * @param argStr String to process.
     * @return String with processed answer.
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

    public boolean allEmpty(Object[] list)
    {
        int size = list.length;
        
        for (int i = 0; i < size; i++)
            if ((list[i] != null) && (list[i].toString().length() > 0))
                return false;
        
        return true;
    }

    /**
      * Replaces all instances of oldString with newString in line.
      * Taken from the Jive forum package.
      */
    public static final String sub(String line, String oldString,String newString)
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

    public String select(boolean state, String trueString, String falseString)
    {
        if (state)
            return trueString;
        else
            return falseString;
    }            
}
