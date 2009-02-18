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

import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 *  Test the #stop directive
 */
public class StopDirectiveTestCase extends BaseTestCase
{
    public StopDirectiveTestCase(String name)
    {
        super(name);
        DEBUG=true;
    }
  
    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, "test/stop/");
        engine.setProperty(RuntimeConstants.VM_LIBRARY, "vmlib1.vm");
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
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
      
      assertEvalEquals("123abcfoo", "123#parse(\"parse2.vm\")foo");
    }
}