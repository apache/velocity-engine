package org.apache.velocity.test;

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
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;

/**
 * Easily add test cases which evaluate templates and check their output.
 *
 * NOTE:
 * This class DOES NOT extend RuntimeTestCase because the VelocityTestSuite
 * already initializes the Velocity runtime and adds the template
 * test cases. Having this class extend RuntimeTestCase causes the
 * Runtime to be initialized twice which is not good. I only discovered
 * this after a couple hours of wondering why all the properties
 * being setup were ending up as Vectors. At first I thought it
 * was a problem with the Configuration class, but the Runtime
 * was being initialized twice: so the first time the property
 * is seen it's stored as a String, the second time it's seen
 * the Configuration class makes a Vector with both Strings.
 * As a result all the getBoolean(property) calls were failing because
 * the Configurations class was trying to create a Boolean from
 * a Vector which doesn't really work that well. I have learned
 * my lesson and now have to add some code to make sure the
 * Runtime isn't initialized more then once :-)
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: TemplateTestCase.java,v 1.22 2001/03/05 10:33:32 jvanzyl Exp $
 */
public class TemplateTestCase extends BaseTestCase
{
    /**
     * VTL file extension.
     */
    private static final String TMPL_FILE_EXT = "vm";

    /**
     * Comparison file extension.
     */
    private static final String CMP_FILE_EXT = "cmp";

    /**
     * Comparison file extension.
     */
    private static final String RESULT_FILE_EXT = "res";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULT_DIR = "../test/templates/results";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = "../test/templates/compare";

    /**
     * The base file name of the template and comparison file (i.e. array for 
     * array.vm and array.cmp).
     */
    protected String baseFileName;

    private TestProvider provider;
    private ArrayList al;
    private Hashtable h;
    private VelocityContext context;
    private VelocityContext context1;
    private VelocityContext context2;
    private Vector vec;

    /**
     * Creates a new instance.
     *
     * @param baseFileName The base name of the template and comparison file to 
     *                     use (i.e. array for array.vm and array.cmp).
     */
    public TemplateTestCase (String baseFileName)
    {
        super(getTestCaseName(baseFileName));
        this.baseFileName = baseFileName;
    }

    /**
     * Sets up the test.
     */
    protected void setUp ()
    {
        provider = new TestProvider();
        al = provider.getCustomers();
        h = new Hashtable();

        h.put("Bar", "this is from a hashtable!");
        h.put("Foo", "this is from a hashtable too!");

        /*
         *  lets set up a vector of objects to test late introspection. See ASTMethod.java
         */

        vec = new Vector();

        vec.addElement( new String("string1"));
        vec.addElement( new String("string2"));

        /*
         *  set up 3 chained contexts, and add our data 
         *  throught the 3 of them.
         */

        context2 = new VelocityContext();
        context1 = new VelocityContext( context2 );
        context = new VelocityContext( context1 );

        context.put("provider", provider);
        context1.put("name", "jason");
        context2.put("providers", provider.getCustomers2());
        context.put("list", al);
        context1.put("hashtable", h);
        context2.put("hashmap", new HashMap() );
        context2.put("search", provider.getSearch());
        context.put("relatedSearches", provider.getRelSearches());
        context1.put("searchResults", provider.getRelSearches());
        context2.put("stringarray", provider.getArray());
        context.put("vector", vec );

        /*
         *  we want to make sure we test all types of iterative objects
         *  in #foreach()
         */

        Object[] oarr = { "a","b","c","d" } ;

        context.put( "collection", vec );
        context2.put("iterator", vec.iterator());
        context1.put("map", h );
        context.put("obarr", oarr );

    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            Template template = Runtime.getTemplate
                (getFileName(null, baseFileName, TMPL_FILE_EXT));
            
            assureResultsDirectoryExists();

            /* get the file to write to */
            FileOutputStream fos = 
                new FileOutputStream (getFileName(
                    RESULT_DIR, baseFileName, RESULT_FILE_EXT));

            Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

            /* process the template */
            template.merge( context, writer);

            /* close the file */
            writer.flush();
            writer.close();
            
            if (!isMatch())
            {
                fail("Processed template did not match expected output");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    /**
     * Concatenates the file name parts together appropriately.
     *
     * @return The full path to the file.
     */
    private static String getFileName (String dir, String base, String ext)
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
    private static void assureResultsDirectoryExists ()
    {
        File resultDir = new File(RESULT_DIR);
        if (!resultDir.exists())
        {
            Runtime.info("Template results directory does not exist");
            if (resultDir.mkdirs())
            {
                Runtime.info("Created template results directory");
            }
            else
            {
                String errMsg = "Unable to create template results directory";
                Runtime.warn(errMsg);
                fail(errMsg);
            }
        }
    }

    /**
     * Turns a base file name into a test case name.
     *
     * @param s The base file name.
     * @return  The test case name.
     */
    private static final String getTestCaseName (String s)
    {
        StringBuffer name = new StringBuffer();
        name.append(Character.toTitleCase(s.charAt(0)));
        name.append(s.substring(1, s.length()).toLowerCase());
        return name.toString();
    }

    /**
     * Returns whether the processed template matches the content of the 
     * provided comparison file.
     *
     * @return Whether the output matches the contents of the comparison file.
     *
     * @exception Exception Test failure condition.
     */
    protected boolean isMatch () throws Exception
    {
        String result = StringUtils.fileContentsToString
            (getFileName(RESULT_DIR, baseFileName, RESULT_FILE_EXT));
            
        String compare = StringUtils.fileContentsToString
             (getFileName(COMPARE_DIR, baseFileName, CMP_FILE_EXT));

        return result.equals(compare);
    }

    /**
     * Performs cleanup activities for this test case.
     */
    protected void tearDown () throws Exception
    {
        /* No op. */
    }
}
