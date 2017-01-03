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

import java.io.StringWriter;
import java.io.Writer;

/**
 * Test resource caching related issues.
 *
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id$
 */
public class ResourceCachingTestCase extends BaseTestCase
{
    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = "/resourcecaching";


    /**
     * Default constructor.
     */
    public ResourceCachingTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {

    }

    public static Test suite ()
    {
        return new TestSuite(ResourceCachingTestCase.class);
    }

    /**
     * Tests for fix of bug VELOCITY-98 where a #include followed by #parse
     * of the same file throws ClassCastException when caching is on.
     * @throws Exception
     */
    public void testIncludeParseCaching ()
            throws Exception
    {

        VelocityEngine ve = new VelocityEngine();

        ve.setProperty("file.resource.loader.cache", "true");
        ve.setProperty("file.resource.loader.path", TemplateTestBase.TEST_COMPARE_DIR + FILE_RESOURCE_LOADER_PATH);
        ve.init();

        Template template = ve.getTemplate("testincludeparse.vm");

        Writer writer = new StringWriter();

        VelocityContext context = new VelocityContext();

        // will produce a ClassCastException if Velocity-98 is not solved
        template.merge(context, writer);
        writer.flush();
        writer.close();
    }


}
