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
import java.io.File;

import java.util.Properties;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.VelocimacroFactory;

import junit.framework.TestCase;

/**
 * Multiple paths in the file resource loader.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: MultipleFileResourcePathTest.java,v 1.8.8.1 2004/03/03 23:23:04 geirm Exp $
 */
public class MultipleFileResourcePathTest extends BaseTestCase
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
    private final static String FILE_RESOURCE_LOADER_PATH1 = "../test/multi/path1";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH2 = "../test/multi/path2";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = "../test/multi/results";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = "../test/multi/compare";

    /**
     * Default constructor.
     */
    MultipleFileResourcePathTest()
    {
        super("MultipleFileResourcePathTest");

        try
        {
            assureResultsDirectoryExists(RESULTS_DIR);

            Velocity.addProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH1);

            Velocity.addProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH2);

            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup MultipleFileResourcePathTest!");
            e.printStackTrace();
            System.exit(1);
        }            
    }

    public static junit.framework.Test suite ()
    {
        return new MultipleFileResourcePathTest();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            Template template1 = RuntimeSingleton.getTemplate(
                getFileName(null, "path1", TMPL_FILE_EXT));
            
            Template template2 = RuntimeSingleton.getTemplate(
                getFileName(null, "path2", TMPL_FILE_EXT));
           
            FileOutputStream fos1 = 
                new FileOutputStream (
                    getFileName(RESULTS_DIR, "path1", RESULT_FILE_EXT));

            FileOutputStream fos2 = 
                new FileOutputStream (
                    getFileName(RESULTS_DIR, "path2", RESULT_FILE_EXT));

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

            if (!isMatch(RESULTS_DIR, COMPARE_DIR, "path1", 
                    RESULT_FILE_EXT, CMP_FILE_EXT) ||
                !isMatch(RESULTS_DIR, COMPARE_DIR, "path2", 
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
