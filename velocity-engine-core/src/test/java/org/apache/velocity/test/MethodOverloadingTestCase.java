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
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Test a reported bug in which method overloading throws IllegalArgumentException
 * after a null return value.
 * (VELOCITY-132).
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class MethodOverloadingTestCase extends BaseTestCase
{
    String logData;

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
   private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/methodoverloading";

   /**
    * Results relative to the build directory.
    */
   private static final String RESULTS_DIR = TEST_RESULT_DIR + "/methodoverloading";

   /**
    * Results relative to the build directory.
    */
   private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/methodoverloading/compare";

    /**
     * Default constructor.
     */
    public MethodOverloadingTestCase(String name)
    {
        super(name);
    }

    public void setUp()
    {
        assureResultsDirectoryExists(RESULTS_DIR);
    }

    public static Test suite()
    {
       return new TestSuite(MethodOverloadingTestCase.class);
    }

    public void testMethodOverloading()
    throws Exception
    {
        /**
         * test overloading in a single template
         */
        testFile("single");

        assertTrue(!logData.contains("IllegalArgumentException"));
    }

    public void testParsedMethodOverloading()
    throws Exception
    {
        /**
         * test overloading in a file included with #parse
         */
        testFile("main");

        assertTrue(!logData.contains("IllegalArgumentException"));

    }

    public void testFile(String basefilename)
    throws Exception
    {
        TestLogger logger = new TestLogger();
        VelocityEngine ve = new VelocityEngine();
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        ve.setProperty(VelocityEngine.RUNTIME_LOG_INSTANCE, logger );
        ve.init();

        Template template;
        FileOutputStream fos;
        Writer fwriter;
        Context context;

        template = ve.getTemplate( getFileName(null, basefilename, TMPL_FILE_EXT) );

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, basefilename, RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        context = new VelocityContext();
        setupContext(context);
        logger.on();
        template.merge(context, fwriter);
        logger.off();
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, basefilename, RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }
        logData = logger.getLog();
    }

    public void setupContext(Context context)
    {
      context.put("test", this);
      context.put("nullValue", null);
    }


    public String overloadedMethod ( Integer s )
    {
        return "Integer";
    }

    public String overloadedMethod ( String s )
    {
        return "String";
    }


    public String overloadedMethod2 ( Integer s )
    {
        return "Integer";
    }

    public String overloadedMethod2 ( String i )
    {
        return "String";
    }
}
