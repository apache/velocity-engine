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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.misc.TestLogger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Load templates from the Classpath.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id$
 */
public class ClasspathResourceTestCase extends BaseTestCase
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
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/cpload";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/cpload/compare";

    /**
     * Default constructor.
     */
    public ClasspathResourceTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        Velocity.reset();
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "classpath");

        /*
         * I don't think I should have to do this, these should
         * be in the default config file.
         */

        Velocity.addProperty(
                "classpath." + Velocity.RESOURCE_LOADER + ".class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Velocity.setProperty(
                "classpath." + Velocity.RESOURCE_LOADER + ".cache", "false");

        Velocity.setProperty(
                "classpath." + Velocity.RESOURCE_LOADER + ".modificationCheckInterval",
                "2");

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.init();
    }

    public static Test suite ()
    {
        return new TestSuite(ClasspathResourceTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testClasspathResource ()
            throws Exception
    {
        /*
         *  lets ensure the results directory exists
         */
        assureResultsDirectoryExists(RESULTS_DIR);

        Template template1 = RuntimeSingleton.getTemplate("/includeevent/test1-cp." + TMPL_FILE_EXT);

        // Uncomment when http://jira.codehaus.org/browse/MPTEST-57 has been resolved
        //            Template template2 = RuntimeSingleton.getTemplate(
        //                getFileName(null, "template/test2", TMPL_FILE_EXT));

        FileOutputStream fos1 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "test1", RESULT_FILE_EXT));

        // Uncomment when http://jira.codehaus.org/browse/MPTEST-57 has been resolved
        //            FileOutputStream fos2 =
        //                new FileOutputStream (
        //                    getFileName(RESULTS_DIR, "test2", RESULT_FILE_EXT));

        Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
        // Uncomment when http://jira.codehaus.org/browse/MPTEST-57 has been resolved
        //            Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));

        /*
         *  put the Vector into the context, and merge both
         */

        VelocityContext context = new VelocityContext();

        template1.merge(context, writer1);
        writer1.flush();
        writer1.close();

        // Uncomment when http://jira.codehaus.org/browse/MPTEST-57 has been resolved
        //            template2.merge(context, writer2);
        //            writer2.flush();
        //            writer2.close();

        if (!isMatch(RESULTS_DIR,COMPARE_DIR,"test1",RESULT_FILE_EXT,CMP_FILE_EXT)
                // Uncomment when http://jira.codehaus.org/browse/MPTEST-57 has been resolved
                //                || !isMatch(RESULTS_DIR,COMPARE_DIR,"test2",RESULT_FILE_EXT,CMP_FILE_EXT)
            )
        {
            fail("Output is incorrect!");
        }
    }
}
