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

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.*;

import org.apache.velocity.Context;
import org.apache.velocity.Template;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.io.FastWriter;

/**
 * Easily add test cases which evaluate templates and check their output.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: TemplateTestCase.java,v 1.6 2000/10/23 22:19:37 dlr Exp $
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
    private static final String RESULT_DIR = "../test/templates/results/";

    /**
     * The base file name of the template and comparison file (i.e. array for 
     * array.vm and array.cmp).
     */
    protected String baseFileName;

    /**
     * The writer used to output evaluated templates.
     */
    private FastWriter writer;

    private TestProvider provider;
    private ArrayList al;
    private Hashtable h;
    private Context context;
    
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
        
        context = new Context();
        context.put("provider", provider);
        context.put("name", "jason");
        context.put("providers", provider.getCustomers2());
        context.put("list", al);
        context.put("hashtable", h);
        context.put("search", provider.getSearch());
        context.put("relatedSearches", provider.getRelSearches());
        context.put("searchResults", provider.getRelSearches());
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            StringBuffer buf = new StringBuffer();
            buf.append(baseFileName).append('.').append(TMPL_FILE_EXT);
            Template template = Runtime.getTemplate(buf.toString());

            template.merge(context, getWriter(
                new FileOutputStream(
                    RESULT_DIR + baseFileName + "." + RESULT_FILE_EXT)));
            
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
     * Get the containing <code>TestSuite</code>.
     *
     * @return The <code>TestSuite</code> to run.
     */
    public static junit.framework.Test suite ()
    {
        return BaseTestCase.suite();
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
        // TODO: Implement matching.
        return true;
    }

    /**
     * Performs cleanup activities for this test case.
     */
    protected void tearDown ()
    {
        try
        {
            closeWriter();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    /**
     * Returns a <code>FastWriter</code> instance.
     *
     * @param out The output stream for the writer to write to.  If 
     *            <code>null</code>, defaults to <code>System.out</code>.
     * @return    The writer.
     */
    protected Writer getWriter (OutputStream out)
        throws UnsupportedEncodingException, IOException
    {
        if (writer == null)
        {
            if (out == null)
            {
                out = System.out;
            }

            writer = new FastWriter
                (out, Runtime.getString(Runtime.TEMPLATE_ENCODING));
            writer.setAsciiHack
                (Runtime.getBoolean(Runtime.TEMPLATE_ASCIIHACK));
        }
        return writer;
    }

    /**
     * Closes the writer (if it has been opened).
     */
    protected void closeWriter ()
        throws IOException
    {
        if (writer != null)
        {
            writer.flush();
            writer.close();
        }
    }
}
