package org.apache.velocity.test;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.app.FieldMethodizer;

import junit.framework.TestCase;
import org.apache.oro.text.perl.Perl5Util;

/**
 * Base test case that provides a few utility methods for
 * the rest of the tests.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: BaseTestCase.java,v 1.11 2001/08/07 22:20:28 geirm Exp $
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
