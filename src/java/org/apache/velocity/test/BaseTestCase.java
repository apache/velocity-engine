package org.apache.velocity.test;

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
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.StringUtils;

import junit.framework.TestCase;
import org.apache.oro.text.perl.Perl5Util;

/**
 * Base test case that provides a few utility methods for
 * the rest of the tests.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: BaseTestCase.java,v 1.12.4.1 2004/03/03 23:23:04 geirm Exp $
 */
public class BaseTestCase extends TestCase
{
    /**
     *  used for nomalization of output and compare data
     */
    private Perl5Util perl = new Perl5Util();    

    /**
     * Default constructor.
     */
    public BaseTestCase(String name)
    {
        super(name);
    }

    /**
     * Concatenates the file name parts together appropriately.
     *
     * @return The full path to the file.
     */
    protected static String getFileName (String dir, String base, String ext)
    {
        StringBuffer buf = new StringBuffer();
        if (dir != null)
        {
            buf.append(dir).append('/');
        }
        buf.append(base).append('.').append(ext);
        return buf.toString();
    }

    /**
     * Assures that the results directory exists.  If the results directory
     * cannot be created, fails the test.
     */
    protected static void assureResultsDirectoryExists (String resultsDirectory)
    {
        File dir = new File(resultsDirectory);
        if (!dir.exists())
        {
            RuntimeSingleton.info("Template results directory does not exist");
            if (dir.mkdirs())
            {
                RuntimeSingleton.info("Created template results directory");
            }
            else
            {
                String errMsg = "Unable to create template results directory";
                RuntimeSingleton.warn(errMsg);
                fail(errMsg);
            }
        }
    }


    /**
     * Normalizes lines to account for platform differences.  Macs use
     * a single \r, DOS derived operating systems use \r\n, and Unix
     * uses \n.  Replace each with a single \n.
     *
     * @author <a href="mailto:rubys@us.ibm.com">Sam Ruby</a>
     * @return source with all line terminations changed to Unix style
     */
    protected String normalizeNewlines (String source)
    {
        return perl.substitute("s/\r[\n]/\n/g", source);
    }

    /**
     * Returns whether the processed template matches the 
     * content of the provided comparison file.
     *
     * @return Whether the output matches the contents 
     *         of the comparison file.
     *
     * @exception Exception Test failure condition.
     */
    protected boolean isMatch (String resultsDir,
                               String compareDir,
                               String baseFileName, 
                               String resultExt,
                               String compareExt)
        throws Exception
    {
        String result = StringUtils.fileContentsToString
            (getFileName(resultsDir, baseFileName, resultExt));
            
        String compare = StringUtils.fileContentsToString
             (getFileName(compareDir, baseFileName, compareExt));

        /*
         *  normalize each wrt newline
         */

        return normalizeNewlines(result).equals( 
                           normalizeNewlines( compare ) );
    }

    /**
     * Turns a base file name into a test case name.
     *
     * @param s The base file name.
     * @return  The test case name.
     */
    protected  static final String getTestCaseName (String s)
    {
        StringBuffer name = new StringBuffer();
        name.append(Character.toTitleCase(s.charAt(0)));
        name.append(s.substring(1, s.length()).toLowerCase());
        return name.toString();
    }
}
