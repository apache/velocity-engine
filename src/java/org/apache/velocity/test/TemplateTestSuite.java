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

import java.io.FileInputStream;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.velocity.app.Velocity;

import junit.framework.TestSuite;

/**
 * Test suite for Templates.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: TemplateTestSuite.java,v 1.5 2001/03/14 22:05:22 jvanzyl Exp $
 */
public class TemplateTestSuite extends TestSuite implements TemplateTestBase
{
    private Properties testProperties;

    /**
     * Creates an instace of the Apache Velocity test suite.
     */
    public TemplateTestSuite()
    {
        try
        {
            Velocity.setProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
            
            Velocity.setProperty(Velocity.RUNTIME_LOG_ERROR_STACKTRACE, "true");
            Velocity.setProperty(Velocity.RUNTIME_LOG_WARN_STACKTRACE, "true");
            Velocity.setProperty(Velocity.RUNTIME_LOG_INFO_STACKTRACE, "true");

            Velocity.init();
            
            testProperties = new Properties();
            testProperties.load(new FileInputStream(TEST_CASE_PROPERTIES));
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup TemplateTestSuite!");
            e.printStackTrace();
            System.exit(1);
        }            

        addTemplateTestCases();
    }

    /**
     * Adds the template test cases to run to this test suite.  Template test
     * cases are listed in the <code>TEST_CASE_PROPERTIES</code> file.
     */
    private void addTemplateTestCases()
    {
        String template;
        for (int i = 1 ;; i++)
        {
            template = testProperties.getProperty(getTemplateTestKey(i));

            if (template != null)
            {
                System.out.println("Adding TemplateTestCase : " + template);
                addTest(new TemplateTestCase(template));
            }
            else
            {
                // Assume we're done adding template test cases.
                break;
            }
        }
    }

    /**
     * Macro which returns the properties file key for the specified template
     * test number.
     *
     * @param nbr The template test number to return a property key for.
     * @return    The property key.
     */
    private static final String getTemplateTestKey(int nbr)
    {
        return ("test.template." + nbr);
    }
}
