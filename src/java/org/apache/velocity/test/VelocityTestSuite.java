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

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import org.apache.velocity.runtime.Runtime;

import junit.framework.*;

/**
 * Test suite for Apache Velocity.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: VelocityTestSuite.java,v 1.6 2000/10/23 18:30:06 jvanzyl Exp $
 */
public class VelocityTestSuite extends TestSuite
{
    /** 
     * Path to test templates relative to the build directory
     * where the tests will be started from.
     */
     private final static String TEST_TEMPLATE_PATH =
        "../test/templates";

    /**
     * Creates an instace of the Apache Velocity test suite.
     */
    public VelocityTestSuite ()
    {
        super("Apache Velocity test suite");

        try
        {
            Runtime.setDefaultProperties();
            Runtime.setProperty(Runtime.TEMPLATE_PATH, TEST_TEMPLATE_PATH);
            Runtime.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot initialize Velocity Runtime!");
            System.exit(1);
        }            

        // Add test cases here.
        List templateTestCases = getTemplateTestCases();
        for (Iterator iter = templateTestCases.iterator(); iter.hasNext(); )
        {
            addTest(new TemplateTestCase((String)iter.next()));
        }
        addTest(new VelocityTest("Apache Velocity"));
    }

    /**
     * Returns a list of the template test cases to run.
     *
     * @return A <code>List</code> of <code>String</code> objects naming the 
     *         test cases.
     */
    private List getTemplateTestCases ()
    {
        List testCases = new ArrayList();
        // TODO: Parse the template test cases from the properties file.
        return testCases;
    }
}
