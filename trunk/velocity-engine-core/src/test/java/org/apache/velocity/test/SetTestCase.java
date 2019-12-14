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

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Test that an instance of a ResourceLoader can be successfully passed in.
 *
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id$
 */
public class SetTestCase extends BaseTestCase
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
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/set";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/set";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/set/compare";

    /**
     * Default constructor.
     */
    public SetTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        assureResultsDirectoryExists(RESULTS_DIR);
    }

    /**
     * Runs the test.
     */
    public void testSetNull()
            throws Exception
    {
        /**
         * Check that #set does accept nulls
         */
        checkTemplate("set1");

        /**
         * Check that #set can accept nulls, and has the correct behaviour for complex LHS
         */
        checkTemplate("set2");
    }

    public void checkTemplate(String templateName) throws Exception
    {
        Template template;
        FileOutputStream fos;
        Writer fwriter;

        template = engine.getTemplate(getFileName(null, templateName, TMPL_FILE_EXT));

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, templateName, RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, templateName, RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }
    }

    public void testInvalidSet() throws Exception
    {
        /* the purpose of this test is to check that in case of error, the calculation of the
         literal representation of the expression, which is displayed in the logs, does not raise a null exception
          */
        assertEvalEquals("", "#set($c = $a - $b - $c)");
        assertEvalEquals("", "#set($c = $a + $b + $c)");
        assertEvalEquals("", "#set($c = $a * $b * $c)");
        assertEvalEquals("", "#set($c = $a / $b / $c)");
        assertEvalEquals("", "#set($c = $a % $b % $c)");
        assertEvalEquals("", "#set($c = $a && $b && $c)");
        assertEvalEquals("", "#set($c = $a || $b || $c)");
        assertEvalEquals("", "#set($c = $a + $b + !$c)");
        assertEvalEquals("", "#set($c = $a + $b + (-$c))");
        assertEvalEquals("", "#set($c = $a && ($b < $c))");
        assertEvalEquals("", "#set($c = !$a)");
        assertEvalEquals("", "#set($c = ($a < $b) - ($c < $d))");
        assertEvalEquals("","#set($b = !$a)");
    }
}
