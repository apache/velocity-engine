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

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.VelocimacroFactory;

import junit.framework.TestCase;

/**
 * Load templates from the Classpath.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: MultiLoaderTestCase.java,v 1.4.8.1 2004/03/03 23:23:04 geirm Exp $
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
    private static final String RESULTS_DIR = "../test/multiloader/results";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = "../test/multiloader";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = "../test/multiloader/compare";

    /**
     * Default constructor.
     */
    public MultiLoaderTestCase()
    {
        super("MultiLoaderTestCase");

        try
        {
            assureResultsDirectoryExists(RESULTS_DIR);
            
            /*
             * Set up the file loader.
             */
            
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

            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup MultiLoaderTestCase!");
            e.printStackTrace();
            System.exit(1);
        }            
    }

    public static junit.framework.Test suite ()
    {
        return new MultiLoaderTestCase();
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

            /*
             * Template to find with the file loader.
             */
            Template template1 = Velocity.getTemplate(
                getFileName(null, "path1", TMPL_FILE_EXT));
            
            /*
             * Template to find with the classpath loader.
             */
            Template template2 = Velocity.getTemplate(
                getFileName(null, "template/test1", TMPL_FILE_EXT));
           
            /*
             * Template to find with the jar loader
             */
            Template template3 = Velocity.getTemplate(
               getFileName(null, "template/test2", TMPL_FILE_EXT));

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
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
