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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.util.StringUtils;
import junit.framework.TestCase;

/**
 * This is a test case for Texen. Simply executes a simple
 * generative task and compares the output.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: TexenClasspathTestCase.java,v 1.3.10.1 2004/03/03 23:23:04 geirm Exp $
 */
public class TexenClasspathTestCase 
    extends BaseTestCase
{
    /**
     * Directory where results are generated.
     */
    private static final String RESULTS_DIR = "../test/texen-classpath/results";

    /**
     * Directory where comparison output is stored.
     */
    private static final String COMPARE_DIR = "../test/texen-classpath/compare";

    /**
     * Creates a new instance.
     *
     */
    public TexenClasspathTestCase()
    {
        super("TexenClasspathTestCase");
    }

    public static junit.framework.Test suite()
    {
        return new TexenClasspathTestCase();
    }

    /**
     * Sets up the test.
     */
    protected void setUp ()
    {
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
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
        catch(Exception e)
        {
            /*
             * do nothing.
             */
        }
    }
}
