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
 * This is a test case for Texen. Simply executes a simple
 * generative task and compares the output.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class TexenClasspathTestCase
    extends BaseTestCase
{
    /**
     * Directory where results are generated.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/texen-classpath";

    /**
     * Directory where comparison output is stored.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/texen-classpath/compare";

    /**
     * Creates a new instance.
     *
     */
    public TexenClasspathTestCase(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(TexenClasspathTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testTexenClasspath ()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        if (!isMatch(RESULTS_DIR,COMPARE_DIR,"TurbineWeather","java","java") ||
            !isMatch(RESULTS_DIR,COMPARE_DIR,"TurbineWeatherService","java","java") ||
            !isMatch(RESULTS_DIR,COMPARE_DIR,"WeatherService","java","java") ||
            !isMatch(RESULTS_DIR,COMPARE_DIR,"book","txt","txt") ||
            !isMatch(RESULTS_DIR,COMPARE_DIR,"Test","txt","txt"))
        {
            fail("Output is incorrect!");
        }
    }
}
