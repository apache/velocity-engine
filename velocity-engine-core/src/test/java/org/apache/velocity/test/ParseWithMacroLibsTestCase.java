/**
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
package org.apache.velocity.test;

import junit.framework.TestSuite;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Test case for including macro libraries via the #parse method.
 */
public class ParseWithMacroLibsTestCase extends BaseTestCase
{
    private static final String RESULT_DIR = TEST_RESULT_DIR + "/parsemacros";

    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/parsemacros/compare";

    public ParseWithMacroLibsTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        super.setUp();
    }

    /**
     * Test suite
     * @return test suite
     */
    public static junit.framework.Test suite()
    {
        return new TestSuite(ParseWithMacroLibsTestCase.class);
    }

    public void testParseMacroLocalCacheOn()
    throws Exception
    {
        /*
         *  local scope, cache on
         */
        VelocityEngine ve = createEngine(true, true);

        // render twice to make sure there is no difference with cached templates
        testParseMacro(ve, "vm_library1.vm", "parseMacro1_1", false);
        testParseMacro(ve, "vm_library1.vm", "parseMacro1_1", false);

        // run again with different macro library
        testParseMacro(ve, "vm_library2.vm", "parseMacro1_1b", false);
        testParseMacro(ve, "vm_library2.vm", "parseMacro1_1b", false);
    }

    /**
     * Runs the tests with global namespace.
     */
    public void testParseMacroLocalCacheOff()
    throws Exception
    {
        /*
         *  local scope, cache off
         */
        VelocityEngine ve = createEngine(false, true);

        testParseMacro(ve, "vm_library1.vm", "parseMacro1_2", true);

        // run again with different macro library
        testParseMacro(ve, "vm_library2.vm", "parseMacro1_2b", true);
    }

    public void testParseMacroGlobalCacheOn()
    throws Exception
    {
        /*
         *  global scope, cache on
         */
        VelocityEngine ve = createEngine(true, false);

        // render twice to make sure there is no difference with cached templates
        testParseMacro(ve, "vm_library1.vm", "parseMacro1_3", false);
        testParseMacro(ve, "vm_library1.vm", "parseMacro1_3", false);

        // run again with different macro library
        testParseMacro(ve, "vm_library2.vm", "parseMacro1_3b", false);
        testParseMacro(ve, "vm_library2.vm", "parseMacro1_3b", false);
    }

    public void testParseMacroGlobalCacheOff()
    throws Exception
    {
        /*
         *  global scope, cache off
         */
        VelocityEngine ve = createEngine(false, false);

        testParseMacro(ve, "vm_library1.vm", "parseMacro1_4", true);

        // run again with different macro library
        testParseMacro(ve, "vm_library2.vm", "parseMacro1_4b", true);

    }

    /**
     * Test #parse with macros.  Can be used to test different engine configurations
     * @param ve
     * @param outputBaseFileName
     * @param testCachingOff
     * @throws Exception
     */
    private void testParseMacro(VelocityEngine ve, String includeFile, String outputBaseFileName, boolean testCachingOff)
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);

        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, outputBaseFileName, RESULT_FILE_EXT));

        VelocityContext context = new VelocityContext();
        context.put("includefile", includeFile);

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        Template template = ve.getTemplate("parseMacro1.vm");
        template.merge(context, writer);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, outputBaseFileName,
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            String result = getFileContents(RESULT_DIR, outputBaseFileName, RESULT_FILE_EXT);
            String compare = getFileContents(COMPARE_DIR, outputBaseFileName, CMP_FILE_EXT);

            String msg = "Processed template did not match expected output\n"+
                "-----Result-----\n"+ result +
                "----Expected----\n"+ compare +
                "----------------";

            fail(msg);
        }

        /*
         * Show that caching is turned off
         */
        if (testCachingOff)
        {
            Template t1 = ve.getTemplate("parseMacro1.vm");
            Template t2 = ve.getTemplate("parseMacro1.vm");

            assertNotSame("Different objects", t1, t2);
        }
    }

    /**
     * Return and initialize engine
     * @return
     */
    private VelocityEngine createEngine(boolean cache, boolean local)
    throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve.setProperty("velocimacro.permissions.allow.inline.to.replace.global",
            local);
        ve.setProperty("file.resource.loader.cache", cache);
        ve.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                TEST_COMPARE_DIR + "/parsemacros");
        ve.init();

        return ve;
    }


    /**
     * Test whether the literal text is given if a definition cannot be
     * found for a macro.
     *
     * @throws Exception
     */
    public void testParseMacrosWithNoDefinition()
            throws Exception
    {
        /*
         *  ve1: local scope, cache on
         */
        VelocityEngine ve1 = new VelocityEngine();

        ve1.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve1.setProperty("velocimacro.permissions.allow.inline.to.replace.global",
                Boolean.FALSE);
        ve1.setProperty("file.resource.loader.cache", Boolean.TRUE);
        ve1.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());
        ve1.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve1.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                TEST_COMPARE_DIR + "/parsemacros");
        ve1.init();

        assureResultsDirectoryExists(RESULT_DIR);

        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, "parseMacro2", RESULT_FILE_EXT));

        VelocityContext context = new VelocityContext();

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        Template template = ve1.getTemplate("parseMacro2.vm");
        template.merge(context, writer);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, "parseMacro2",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template did not match expected output");
        }
    }


    /**
     * Test that if a macro is duplicated, the second one takes precendence
     *
     * @throws Exception
     */
    public void testDuplicateDefinitions()
            throws Exception
    {
        /*
         *  ve1: local scope, cache on
         */
        VelocityEngine ve1 = new VelocityEngine();

        ve1.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve1.setProperty("velocimacro.permissions.allow.inline.to.replace.global",
                Boolean.FALSE);
        ve1.setProperty("file.resource.loader.cache", Boolean.TRUE);
        ve1.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());
        ve1.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve1.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                TEST_COMPARE_DIR + "/parsemacros");
        ve1.init();

        assureResultsDirectoryExists(RESULT_DIR);

        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, "parseMacro3", RESULT_FILE_EXT));

        VelocityContext context = new VelocityContext();

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        Template template = ve1.getTemplate("parseMacro3.vm");
        template.merge(context, writer);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, "parseMacro3",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template did not match expected output");
        }
    }

}
