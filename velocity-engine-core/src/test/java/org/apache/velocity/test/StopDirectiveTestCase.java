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

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogger;

/**
 *  Test the #stop directive
 */
public class StopDirectiveTestCase extends BaseTestCase
{
    public StopDirectiveTestCase(String name)
    {
        super(name);
        //DEBUG=true;
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, TemplateTestBase.TEST_COMPARE_DIR + "/stop");
        engine.setProperty(RuntimeConstants.VM_LIBRARY, "vmlib1.vm");
    }

    public void testStop()
    {
      // Make it work through the evaluate method call
      assertEvalEquals("Text1", "Text1#{stop}Text2");
      // Make sure stop works in a template
      assertTmplEquals("Text 1", "stop1.vm");
      // Make sure stop works when called from a velocity macro
      assertTmplEquals("Text123stuff1", "stop2.vm");
      // Make sure stop works when called located in another parsed file
      assertTmplEquals("text1blaa1", "stop3.vm");
    }

    public void testNestedStopAll()
    {
        addTemplate("ns", ",template"+
                          "#macro(vm),macro${bodyContent}macro#end"+
                          "#define($define),define"+
                            "#foreach($i in [1..2]),foreach"+
                              "#{stop}foreach"+
                            "#{end}define"+
                          "#{end}"+
                          "#@vm(),bodyContent"+
                            "${define}bodyContent"+
                          "#{end}template");
        String expected = "evaluate,template,macro,bodyContent,define,foreach";
        assertEvalEquals(expected, "#evaluate('evaluate#parse(\"ns\")evaluate')");
    }

    public void testStopMessage()
    {
        log.setEnabledLevel(TestLogger.LOG_LEVEL_DEBUG);
        context.put("log", log);

        assertEvalEquals("a", "a$!log.startCapture()#stop('woogie!')b");

        log.stopCapture();
        log.on();
        info("Log: "+log.getLog());
        assertTrue(log.getLog().contains("StopCommand: woogie!"));
    }

}
