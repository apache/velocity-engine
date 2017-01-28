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
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.test.misc.TestLogger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Multiple paths in the file resource loader.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class StringResourceLoaderTestCase extends BaseTestCase
{
    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/stringloader";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/stringloader/compare";

    /**
     * Default constructor.
     */
    public StringResourceLoaderTestCase(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(StringResourceLoaderTestCase.class);
    }

    public void setUp()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        Velocity.reset();

        Velocity.setProperty(Velocity.RESOURCE_LOADER, "string");
        Velocity.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        Velocity.addProperty("string.resource.loader.modificationCheckInterval", "1");

        // Silence the logger.
        Velocity.setProperty(Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.init();
    }

    public void  testSimpleTemplate()
            throws Exception
    {
        StringResourceLoader.getRepository().putStringResource("simpletemplate.vm", "This is a test for ${foo}");

        Template template = RuntimeSingleton.getTemplate(getFileName(null, "simpletemplate", TMPL_FILE_EXT));

        FileOutputStream fos =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "simpletemplate", RESULT_FILE_EXT));

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        VelocityContext context = new VelocityContext();
        context.put("foo", "a foo object");

        template.merge(context, writer);
        writer.flush();
        writer.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "simpletemplate",
                        RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }
    }

    public void  testMultipleTemplates()
            throws Exception
    {
        StringResourceLoader.getRepository().putStringResource("multi1.vm", "I am the $first template.");
        StringResourceLoader.getRepository().putStringResource("multi2.vm", "I am the $second template.");

        Template template1 = RuntimeSingleton.getTemplate(getFileName(null, "multi1", TMPL_FILE_EXT));

        FileOutputStream fos =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "multi1", RESULT_FILE_EXT));

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        VelocityContext context = new VelocityContext();
        context.put("first", 1);
        context.put("second", "two");

        template1.merge(context, writer);
        writer.flush();
        writer.close();

        Template template2 = RuntimeSingleton.getTemplate(getFileName(null, "multi2", TMPL_FILE_EXT));

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, "multi2", RESULT_FILE_EXT));

        writer = new BufferedWriter(new OutputStreamWriter(fos));

        template2.merge(context, writer);
        writer.flush();
        writer.close();



        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "multi1",
                        RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Template 1 incorrect.");
        }

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "multi2",
                        RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Template 2 incorrect.");
        }
    }

    public void  testContentChange()
            throws Exception
    {
        StringResourceLoader.getRepository().putStringResource("change.vm", "I am the $first template.");

        Template template = RuntimeSingleton.getTemplate(getFileName(null, "change", TMPL_FILE_EXT));

        FileOutputStream fos =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "change1", RESULT_FILE_EXT));

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        VelocityContext context = new VelocityContext();
        context.put("first", 1);
        context.put("second", "two");

        template.merge(context, writer);
        writer.flush();
        writer.close();

        StringResourceLoader.getRepository().putStringResource("change.vm", "I am the $second template.");
        Thread.sleep(2000L);
        template = RuntimeSingleton.getTemplate(getFileName(null, "change", TMPL_FILE_EXT));

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, "change2", RESULT_FILE_EXT));

        writer = new BufferedWriter(new OutputStreamWriter(fos));

        template.merge(context, writer);
        writer.flush();
        writer.close();



        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "change1",
                        RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Template 1 incorrect.");
        }

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "change2",
                        RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Template 2 incorrect.");
        }
    }

}
