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
}
