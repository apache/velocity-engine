package org.apache.velocity.texen.util;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

/**
 * A general file utility for use in the context
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: FileUtil.java,v 1.9.8.1 2004/03/03 23:23:07 geirm Exp $ 
 */
public class FileUtil
{
    /**
     * Creates the directory s (and any parent directories needed).
     *
     * @param String path/directory to create.
     * @param String report of path/directory creation.
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

    /**
     * A method to get a File object.
     *
     * @param String path to file object to create.
     * @return File created file object.
     */
    public static File file(String s)
    {
        File f = new File(s);
        return f;
    }
    
    /**
     * A method to get a File object.
     *
     * @param String base path
     * @param String file name
     * @return File created file object.
     */
    public static File file(String base, String s)
    {
        File f = new File(base, s);
        return f;
    }
}
