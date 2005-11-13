package org.apache.velocity.test;

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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

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

    public void setUp()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);
    }

    public static Test suite ()
    {
        return new TestSuite(SetTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testSetNull()
            throws Exception
    {
        /**
         * Check that #set does not accept nulls
         */
    
        VelocityEngine ve = new VelocityEngine();
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        ve.init();
    
        checkTemplate(ve,"set1");
        
        /**
         * Check that setting the property is the same as the default
         */
        ve = new VelocityEngine();
        ve.addProperty(RuntimeConstants.SET_NULL_ALLOWED, "false");
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        ve.init();
    
        checkTemplate(ve,"set1");

        /**
         * Check that #set can accept nulls
         */
        ve = new VelocityEngine();
        ve.addProperty(RuntimeConstants.SET_NULL_ALLOWED, "true");
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        ve.init();
    
        checkTemplate(ve,"set2");
    }

    public void checkTemplate(VelocityEngine ve, String templateName)
    throws Exception
    {
        Template template;
        FileOutputStream fos;
        Writer fwriter;
        Context context;

        template = ve.getTemplate( getFileName(null, templateName, TMPL_FILE_EXT) );

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, templateName, RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        context = new VelocityContext();
        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, templateName, RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }        
    }

}

