package org.apache.velocity.texen.util;


// JDK Classes
import java.io.*;
import java.util.*;

/**
 * A string utility class for the texen text/code generator
 * Usually this class is only used from a Velcity context.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 */
public class StringUtil extends BaseUtil
{
    
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
     *
     * @param string String to chop.
     * @param i Number of characters to chop.
     * @return String with processed answer.
     */
    public static String chop(String string, int i)
    {
        return(string.substring(0, string.length() - i));
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
            if (list[i].toString().length() > 0)
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
