package org.apache.velocity.test.issues;

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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.ExtProperties;

import java.io.FileReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * This class tests VELOCITY-785.
 */
public class Velocity747TestCase extends BaseTestCase
{
    public Velocity747TestCase(String name)
    {
        super(name);
    }

    VelocityEngine engine1;
    VelocityEngine engine2;

    protected void setUp() throws Exception
    {
        Properties props = new Properties();
        /* The props file contains *spaces* at the end of the line:
         *   velocimacro.permissions.allow.inline.local.scope = true
         * which caused the initial problem
         */
        props.load(new FileReader(TEST_COMPARE_DIR + "/issues/velocity-747/vel.props"));
        props.setProperty("file.resource.loader.path", TEST_COMPARE_DIR + "/issues/velocity-747/");
        engine1 = new VelocityEngine(props);

        //by default, make the engine's log output go to the test-report
        log = new TestLogger(false, false);
        engine1.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);

        engine2 = new VelocityEngine();
        engine2.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,string");
        engine2.addProperty("file.resource.loader.path", TEST_COMPARE_DIR + "/issues/velocity-747/");
        engine2.addProperty("file.resource.loader.cache", "true");
        engine2.addProperty("file.resource.loader.modificationCheckInterval", "-1");
        engine2.addProperty("velocimacro.permissions.allow.inline.local.scope", "true");
        engine2.addProperty("directive.set.null.allowed", "true");
        engine2.addProperty("velocimacro.max.depth", "-1");
        engine2.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        engine2.addProperty("string.resource.loader.repository.name", "stringRepo");
        engine2.addProperty("string.resource.loader.repository.static", "false");
        log = new TestLogger(false, false);
        engine2.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
    }

    public void testMacroIsolation1()
    {
        StringWriter writer = new StringWriter();
        VelocityContext ctx = new VelocityContext();
        engine1.mergeTemplate("one.vm", "UTF-8", new VelocityContext(), writer);
        String result = writer.toString();
        assertEquals(result, "This is from Test1 macro of one.vm");
        writer = new StringWriter();
        engine1.mergeTemplate("two.vm", "UTF-8", ctx, writer);
        result = writer.toString();
        assertEquals(result, "This is from Test1 macro of two.vm");
    }

    public void testMacroIsolation2()
    {
        StringWriter writer = new StringWriter();
        VelocityContext ctx = new VelocityContext();
        engine2.mergeTemplate("one.vm", "UTF-8", new VelocityContext(), writer);
        String result = writer.toString();
        assertEquals(result, "This is from Test1 macro of one.vm");

        writer = new StringWriter();
        engine2.mergeTemplate("two.vm", "UTF-8", ctx, writer);
        result = writer.toString();
        assertEquals(result, "This is from Test1 macro of two.vm");
    }
}
