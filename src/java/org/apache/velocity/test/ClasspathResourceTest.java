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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;

/**
 * Load templates from the Classpath.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: ClasspathResourceTest.java,v 1.9.4.1 2004/03/03 23:23:04 geirm Exp $
 */
public class ClasspathResourceTest extends BaseTestCase
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
    private static final String RESULTS_DIR = "../test/cpload/results";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = "../test/cpload/compare";

    /**
     * Default constructor.
     */
    public ClasspathResourceTest()
    {
        super("ClasspathResourceTest");

        try
        {
            assureResultsDirectoryExists(RESULTS_DIR);
            
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

            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup ClasspathResourceTest!");
            e.printStackTrace();
            System.exit(1);
        }            
    }

    public static junit.framework.Test suite ()
    {
        return new ClasspathResourceTest();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            /*
             *  lets ensure the results directory exists
             */
            assureResultsDirectoryExists(RESULTS_DIR);

            Template template1 = RuntimeSingleton.getTemplate(
                getFileName(null, "template/test1", TMPL_FILE_EXT));
            
            Template template2 = RuntimeSingleton.getTemplate(
                getFileName(null, "template/test2", TMPL_FILE_EXT));
           
            FileOutputStream fos1 = 
                new FileOutputStream (
                    getFileName(RESULTS_DIR, "test1", RESULT_FILE_EXT));

            FileOutputStream fos2 = 
                new FileOutputStream (
                    getFileName(RESULTS_DIR, "test2", RESULT_FILE_EXT));

            Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
            Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));
            
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

            if (!isMatch(RESULTS_DIR,COMPARE_DIR,"test1",RESULT_FILE_EXT,CMP_FILE_EXT) ||
                !isMatch(RESULTS_DIR,COMPARE_DIR,"test2",RESULT_FILE_EXT,CMP_FILE_EXT))
            {
                fail("Output is incorrect!");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
