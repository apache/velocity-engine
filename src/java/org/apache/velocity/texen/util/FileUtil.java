package org.apache.velocity.texen.util;


import java.io.*;
import java.util.*;

/**
 * A general file utility for use in the context
 *
 * @author <a href="mailto:leon@opticode.co.za>Leon Messerschmidt</a>
 */

public class FileUtil extends BaseUtil
{
    /**
     * Creates the directory s (and any parent directories needed)
     */
    static public String mkdir (String s)
    {
        try
        {
            if ((new File(s)).mkdirs())
                return "Created dir: "+s;
            else
                return "Failed to create dir or dir already exists: "+s;
        }
        catch (Exception e)
        {
            return e.toString();
        }

    }

}
