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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.test.misc.TestLogger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * This class tests the velocimacro.library.autoreload functionality, and issue VELOCITY-
 */
public class MacroAutoReloadTestCase extends BaseTestCase
{
    private final String RELOAD_TEMPLATE_PATH = TEST_COMPARE_DIR + "/reload";

    public MacroAutoReloadTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        // always copy macros library before modifying it, to ensure successive tests will pass
        Files.copy(FileSystems.getDefault().getPath(RELOAD_TEMPLATE_PATH + "/macros.vtl"), FileSystems.getDefault().getPath(RELOAD_TEMPLATE_PATH + "/macros2.vtl"), StandardCopyOption.REPLACE_EXISTING);

        engine = new VelocityEngine();

        //by default, make the engine's log output go to the test-report
        log = new TestLogger(false, false);
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);

        // use file resource loader
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,string");
        engine.addProperty("file.resource.loader.path", RELOAD_TEMPLATE_PATH);
        engine.addProperty("velocimacro.library", "macros2.vtl");
        engine.addProperty("velocimacro.library.autoreload", "true");
        engine.addProperty("file.resource.loader.cache", "false");
        engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        engine.addProperty("string.resource.loader.repository.name", "stringRepo");
        engine.addProperty("string.resource.loader.repository.static", "false");
        context = new VelocityContext();
    }

    
    public void testChangedMacro() throws Exception
    {
        String template = "#foo('hip')";
        assertEvalEquals("hop_hip", template);

        FileWriter writer = new FileWriter(RELOAD_TEMPLATE_PATH + "/macros2.vtl");
        writer.write("#macro(foo $txt)hip_$txt#{end}");
        writer.close();
        // last modified timestamps resolution is one second before Java 8,
        // so force reloading by setting a file date in the future
        File macros2 = new File(TEST_COMPARE_DIR + "/reload/macros2.vtl");
        macros2.setLastModified(macros2.lastModified() + 1000);

        assertEvalEquals("hip_hip", template);
    }

    public void testNewMacro() throws Exception
    {
        String template = "#foo('hip')";
        assertEvalEquals("hop_hip", template);

        FileWriter writer = new FileWriter(TEST_COMPARE_DIR + "/reload/macros2.vtl", true);
        writer.write("\n#macro(bar $txt)hep_$txt#{end}");
        writer.close();
        // last modified timestamps resolution is one second before Java 8,
        // so force reloading by setting a file date in the future
        File macros2 = new File(TEST_COMPARE_DIR + "/reload/macros2.vtl");
        macros2.setLastModified(macros2.lastModified() + 1000);

        template = "#foo('hip') #bar('hip')";
        assertEvalEquals("hop_hip hep_hip", template);
    }
}
