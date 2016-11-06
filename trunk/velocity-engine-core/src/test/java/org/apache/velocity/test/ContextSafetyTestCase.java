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
import java.util.Vector;

/**
 * Tests if we are context safe : can we switch objects in the context
 * and re-merge the template safely.
 *
 * NOTE:
 * This class should not extend RuntimeTestCase because this test
 * is run from the VelocityTestSuite which in effect a runtime
 * test suite and the test suite initializes the Runtime. Extending
 * RuntimeTestCase causes the Runtime to be initialized twice.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ContextSafetyTestCase extends BaseTestCase implements TemplateTestBase
{
    public ContextSafetyTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        Velocity.reset();
        Velocity.setProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.init();
    }

    public static Test suite()
    {
        return new TestSuite(ContextSafetyTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testContextSafety ()
        throws Exception
    {
        /*
         *  make a Vector and String array because
         *  they are treated differently in Foreach()
         */
        Vector v = new Vector();

        v.addElement( new String("vector hello 1") );
        v.addElement( new String("vector hello 2") );
        v.addElement( new String("vector hello 3") );

        String strArray[] = new String[3];

        strArray[0] = "array hello 1";
        strArray[1] = "array hello 2";
        strArray[2] = "array hello 3";

        VelocityContext context = new VelocityContext();

        assureResultsDirectoryExists(RESULT_DIR);

        /*
         *  get the template and the output
         */

        Template template = RuntimeSingleton.getTemplate(
            getFileName(null, "context_safety", TMPL_FILE_EXT));

        FileOutputStream fos1 =
            new FileOutputStream (
                getFileName(RESULT_DIR, "context_safety1", RESULT_FILE_EXT));

        FileOutputStream fos2 =
            new FileOutputStream (
                getFileName(RESULT_DIR, "context_safety2", RESULT_FILE_EXT));

        Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
        Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));

        /*
         *  put the Vector into the context, and merge
         */

        context.put("vector", v);
        template.merge(context, writer1);
        writer1.flush();
        writer1.close();

        /*
         *  now put the string array into the context, and merge
         */

        context.put("vector", strArray);
        template.merge(context, writer2);
        writer2.flush();
        writer2.close();

        if (!isMatch(RESULT_DIR,COMPARE_DIR,"context_safety1",
                RESULT_FILE_EXT,CMP_FILE_EXT) ||
            !isMatch(RESULT_DIR,COMPARE_DIR,"context_safety2",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }
    }
}
