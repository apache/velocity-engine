package org.apache.velocity.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.TestSuite;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Test suite for Templates.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id$
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
