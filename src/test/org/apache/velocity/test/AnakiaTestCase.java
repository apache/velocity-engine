package org.apache.velocity.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This is a test case for Anakia. Right now, it simply will compare
 * two index.html files together. These are produced as a result of
 * first running Anakia and then running this test.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id$
 */
public class AnakiaTestCase extends BaseTestCase
{
    private static final String COMPARE_DIR = "test/anakia/compare";
    private static final String RESULTS_DIR = "target/test/anakia";

    private static final String CONTEXT_FILE_EXT = "context.html";

    /**
     * Creates a new instance.
     *
     */
    public AnakiaTestCase(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(AnakiaTestCase.class);
    }

    /**
     * Runs the test. This is empty on purpose because the
     * code to do the Anakia output is in the .xml file that runs
     * this test.
     */
    public void testAnakiaResults ()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        /**
        if (!isMatch(RESULTS_DIR,COMPARE_DIR,"index",FILE_EXT,FILE_EXT))
        {
            fail("Output is incorrect!");
        }
        **/

        if (!isMatch(
                RESULTS_DIR,
                COMPARE_DIR,
                "index",
                CONTEXT_FILE_EXT,
                CONTEXT_FILE_EXT))
                {
                fail("Custom Context Output is incorrect");
                }
        else
        {
            System.out.println ("Passed!");
        }
    }
}
