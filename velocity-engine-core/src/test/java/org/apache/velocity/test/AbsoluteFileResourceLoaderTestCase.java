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
 * Test use of an absolute path with the FileResourceLoader
 *
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id$
 */
public class AbsoluteFileResourceLoaderTestCase extends BaseTestCase
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
     * Path to template file.  This will get combined with the
     * application directory to form an absolute path
     */
    private final static String TEMPLATE_PATH = TEST_COMPARE_DIR + "/absolute/absolute";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/absolute/results";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/absolute/compare";

    /**
     * Default constructor.
     */
    AbsoluteFileResourceLoaderTestCase()
    {
        super("AbsoluteFileResourceLoaderTest");
    }

    public static Test suite ()
    {
        return new AbsoluteFileResourceLoaderTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {

        try
        {
            assureResultsDirectoryExists(RESULTS_DIR);

            Velocity.reset();

            // signify we want to use an absolute path
            Velocity.addProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, "");

            Velocity.setProperty(
                    Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

            Velocity.init();
        }
        catch (Exception e)
        {
            String msg = "Cannot setup AbsoluteFileResourceLoaderTest!";
            info(msg, e);
            fail(msg);
        }
        try
        {

            String curdir = System.getProperty("user.dir");
            String f = getFileName(curdir, TEMPLATE_PATH, TMPL_FILE_EXT);

            System.out.println("Retrieving template at absolute path: " + f);

            Template template1 = RuntimeSingleton.getTemplate(f);

            FileOutputStream fos1 =
                new FileOutputStream (
                    getFileName(RESULTS_DIR, "absolute", RESULT_FILE_EXT));

            Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));

            /*
             *  put the Vector into the context, and merge both
             */
            VelocityContext context = new VelocityContext();

            template1.merge(context, writer1);
            writer1.flush();
            writer1.close();

            if (!isMatch(RESULTS_DIR, COMPARE_DIR, "absolute",
                    RESULT_FILE_EXT, CMP_FILE_EXT))
            {
                fail("Output incorrect.");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
