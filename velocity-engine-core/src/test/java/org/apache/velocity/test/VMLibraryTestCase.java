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

import junit.framework.TestSuite;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Macro library inclution via the Template.merge method is tested using this
 * class.
 */

public class VMLibraryTestCase extends BaseTestCase
{
    /**
     * This engine is used with local namespaces
     */
    private VelocityEngine ve1 = new VelocityEngine();

    /**
     * This engine is used with global namespaces
     */
    private VelocityEngine ve2 = new VelocityEngine();

    private static final String RESULT_DIR = TEST_RESULT_DIR + "/macrolibs";

    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/macrolibs/compare";

    public VMLibraryTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        /*
         *  setup local scope for templates
         */
        ve1.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve1.setProperty("velocimacro.permissions.allow.inline.to.replace.global",
                Boolean.FALSE);
        /**
         * Turn on the cache
         */
        ve1.setProperty("file.resource.loader.cache", Boolean.TRUE);

        ve1.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        ve1.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve1.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                TEST_COMPARE_DIR + "/macrolibs");
        ve1.init();

        /**
         * Set to global namespaces
         */
        ve2.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.FALSE);
        ve2.setProperty("velocimacro.permissions.allow.inline.to.replace.global",
                Boolean.TRUE);
        /**
         * Turn on the cache
         */
        ve2.setProperty("file.resource.loader.cache", Boolean.FALSE);

        ve2.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        ve2.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve2.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                TEST_COMPARE_DIR + "/macrolibs");
        ve2.init();
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(VMLibraryTestCase.class);
    }

    /**
     * Runs the tests with local namespace.
     */
    public void testVelociMacroLibWithLocalNamespace()
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);
        /**
         * Clear the file before proceeding
         */
        File file = new File(getFileName(
                RESULT_DIR, "vm_library_local", RESULT_FILE_EXT));
        if (file.exists())
        {
            file.delete();
        }

        /**
         * Create a file output stream for appending
         */
        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, "vm_library_local", RESULT_FILE_EXT), true);

        List templateList = new ArrayList();
        VelocityContext context = new VelocityContext();
        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        templateList.add("vm_library1.vm");

        Template template = ve1.getTemplate("vm_library_local.vm");
        template.merge(context, writer, templateList);

        /**
         * remove the first template library and includes a new library
         * with a new definition for macros
         */
        templateList.remove(0);
        templateList.add("vm_library2.vm");
        template = ve1.getTemplate("vm_library_local.vm");
        template.merge(context, writer, templateList);

        /*
         *Show that caching is working
         */
        Template t1 = ve1.getTemplate("vm_library_local.vm");
        Template t2 = ve1.getTemplate("vm_library_local.vm");

        assertEquals("Both templates refer to the same object", t1, t2);

        /**
         * Remove the libraries
         */
        template = ve1.getTemplate("vm_library_local.vm");
        template.merge(context, writer);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, "vm_library_local",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template did not match expected output");
        }
    }

    /**
     * Runs the tests with global namespace.
     */
    public void testVelociMacroLibWithGlobalNamespace()
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);
        /**
         * Clear the file before proceeding
         */
        File file = new File(getFileName(
                RESULT_DIR, "vm_library_global", RESULT_FILE_EXT));
        if (file.exists())
        {
            file.delete();
        }

        /**
         * Create a file output stream for appending
         */
        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, "vm_library_global", RESULT_FILE_EXT), true);

        List templateList = new ArrayList();
        VelocityContext context = new VelocityContext();
        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        templateList.add("vm_library1.vm");

        Template template = ve1.getTemplate("vm_library_global.vm");
        template.merge(context, writer, templateList);

        /**
         * remove the first template library and includes a new library
         * with a new definition for macros
         */
        templateList.remove(0);
        templateList.add("vm_library2.vm");
        template = ve1.getTemplate("vm_library_global.vm");
        template.merge(context, writer, templateList);

        /*
         *Show that caching is not working (We have turned off cache)
         */
        Template t1 = ve2.getTemplate("vm_library_global.vm");
        Template t2 = ve2.getTemplate("vm_library_global.vm");

        assertNotSame("Defferent objects", t1, t2);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, "vm_library_global",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template did not match expected output");
        }
    }

    /**
     * Runs the tests with global namespace.
     */
    public void testVelociMacroLibWithDuplicateDefinitions()
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);
        /**
         * Clear the file before proceeding
         */
        File file = new File(getFileName(
                RESULT_DIR, "vm_library_duplicate", RESULT_FILE_EXT));
        if (file.exists())
        {
            file.delete();
        }

        /**
         * Create a file output stream for appending
         */
        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, "vm_library_duplicate", RESULT_FILE_EXT), true);

        List templateList = new ArrayList();
        VelocityContext context = new VelocityContext();
        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        templateList.add("vm_library1.vm");
        templateList.add("vm_library2.vm");

        Template template = ve1.getTemplate("vm_library.vm");
        template.merge(context, writer, templateList);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, "vm_library_duplicate",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template did not match expected output");
        }
    }

    /**
     * Test whether the literal text is given if a definition cannot be
     * found for a macro.
     *
     * @throws Exception
     */
    public void testMacrosWithNoDefinition()
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);

        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, "vm_library", RESULT_FILE_EXT));

        VelocityContext context = new VelocityContext();
        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        Template template = ve1.getTemplate("vm_library.vm");
        template.merge(context, writer, null);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        /**
         * outputs the macro calls
         */
        if (!isMatch(RESULT_DIR, COMPARE_DIR, "vm_library",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template did not match expected output");
        }
    }


}
