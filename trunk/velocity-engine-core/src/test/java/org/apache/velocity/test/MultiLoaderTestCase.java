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
public class MultiLoaderTestCase extends BaseTestCase
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
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/multiloader";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/multiloader";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/multiloader/compare";

    /**
     * Default constructor.
     */
    public MultiLoaderTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        /*
         * Set up the file loader.
         */

        Velocity.reset();

        Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");

        Velocity.setProperty(
            Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);

        Velocity.addProperty(Velocity.RESOURCE_LOADER, "classpath");

        Velocity.addProperty(Velocity.RESOURCE_LOADER, "jar");

        /*
         *  Set up the classpath loader.
         */

        Velocity.setProperty(
            "classpath." + Velocity.RESOURCE_LOADER + ".class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Velocity.setProperty(
            "classpath." + Velocity.RESOURCE_LOADER + ".cache", "false");

        Velocity.setProperty(
            "classpath." + Velocity.RESOURCE_LOADER + ".modificationCheckInterval",
                "2");

        /*
         *  setup the Jar loader
         */

        Velocity.setProperty(
                             "jar." + Velocity.RESOURCE_LOADER + ".class",
                             "org.apache.velocity.runtime.resource.loader.JarResourceLoader");

        Velocity.setProperty( "jar." + Velocity.RESOURCE_LOADER + ".path",
                              "jar:file:" + FILE_RESOURCE_LOADER_PATH + "/test2.jar" );


        Velocity.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.init();
    }

    public static Test suite ()
    {
        return new TestSuite(MultiLoaderTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testMultiLoader ()
            throws Exception
    {
        /*
         *  lets ensure the results directory exists
         */
        assureResultsDirectoryExists(RESULTS_DIR);

        /*
         * Template to find with the file loader.
         */
        Template template1 = Velocity.getTemplate(
            getFileName(null, "path1", TMPL_FILE_EXT));

        /*
         * Template to find with the classpath loader.
         */
        Template template2 = Velocity.getTemplate("includeevent/test1-cp." + TMPL_FILE_EXT);

        /*
         * Template to find with the jar loader
         */
        Template template3 = Velocity.getTemplate("template/test2." + TMPL_FILE_EXT);

        /*
         * and the results files
         */

        FileOutputStream fos1 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "path1", RESULT_FILE_EXT));

        FileOutputStream fos2 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "test2", RESULT_FILE_EXT));

        FileOutputStream fos3 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "test3", RESULT_FILE_EXT));

        Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
        Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));
        Writer writer3 = new BufferedWriter(new OutputStreamWriter(fos3));

        /*
         *  put the Vector into the context, and merge both
         */

        VelocityContext context = new VelocityContext();

        template1.merge(context, writer1);
        writer1.flush();
        writer1.close();

        template2.merge(context, writer2);
        writer2.flush();
        writer2.close();

        template3.merge(context, writer3);
        writer3.flush();
        writer3.close();

        if (!isMatch(RESULTS_DIR,COMPARE_DIR,"path1",RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Output incorrect for FileResourceLoader test.");
        }

        if (!isMatch(RESULTS_DIR,COMPARE_DIR,"test2",RESULT_FILE_EXT,CMP_FILE_EXT) )
        {
            fail("Output incorrect for ClasspathResourceLoader test.");
        }

        if( !isMatch(RESULTS_DIR,COMPARE_DIR,"test3",RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Output incorrect for JarResourceLoader test.");
        }
    }
}
