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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class tests the mode where velocimacros do preserve arguments literals
 */

public class VelocimacroBCModeTestCase extends BaseTestCase
{
    private static final String BASE_DIR = TEST_COMPARE_DIR + "/bc_mode";
    private static final String CMP_DIR = BASE_DIR + "/compare";
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/bc_mode";

    public VelocimacroBCModeTestCase(final String name)
    {
        super(name);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine)
    {
        boolean bcMode = !getName().contains("NoPreserve");
        engine.setProperty(RuntimeConstants.VM_ENABLE_BC_MODE, bcMode);
        engine.setProperty("file.resource.loader.path", TEST_COMPARE_DIR + "/bc_mode");
    }

    public void testPreserveLiterals()
    {
        assertEvalEquals("$bar","#macro(m $foo)$foo#end#m($bar)");
    }

    public void testGlobalDefaults()
    {
        assertEvalEquals("foo","#macro(m $foo)$foo#end#set($foo='foo')#m()");
    }

    public void testVariousCasesPreserve() throws Exception
    {
        doTestVariousCases("bc_mode_enabled");
    }

    public void testVariousCasesNoPreserve() throws Exception
    {
        doTestVariousCases("bc_mode_disabled");
    }

    private void doTestVariousCases(String compare_ext) throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);
        String basefilename = "test_bc_mode";
        Template template = engine.getTemplate( getFileName(null, basefilename, "vtl") );
        context = new VelocityContext();
        FileOutputStream fos;
        Writer fwriter;

        fos = new FileOutputStream (getFileName(RESULTS_DIR, basefilename, RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, CMP_DIR, basefilename, RESULT_FILE_EXT, compare_ext))
        {
            String result = getFileContents(RESULTS_DIR, basefilename, RESULT_FILE_EXT);
            String compare = getFileContents(CMP_DIR, basefilename, compare_ext);

            assertEquals(compare, result);
        }
    }
}
