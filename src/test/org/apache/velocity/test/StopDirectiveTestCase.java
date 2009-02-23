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
    }
  
    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, "test/stop/");
        engine.setProperty(RuntimeConstants.VM_LIBRARY, "vmlib1.vm");
    }

    public void testStop()
    {
      // Make it works through the evaluate method call
      assertEvalEquals("Text1", "Text1#{stop}Text2");
      // Make sure stop works in a template
      assertTmplEquals("Text 1", "stop1.vm");
      // Make sure stop works when called from a velocity macro
      assertTmplEquals("Text123stuff1", "stop2.vm");
      // Make sure stop works when called located in another parsed file
      assertTmplEquals("text1blaa1", "stop3.vm");
    }

    public void testBadStopArgs()
    {
        context.put("foo","foo");
        assertEvalException("#stop($null)");
        assertEvalException("#stop($foo)");
        assertEvalException("#stop(true)");
        assertEvalException("#stop(1.2)");
        assertEvalException("#stop([0..1])");
        assertEvalException("#stop( $too $many )");
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

    public void testStopForeach()
    {
        String template = "#foreach($i in [1..5])$i#if($i>2)#stop($foreach)#end#end test";
        assertEvalEquals("123 test", template);

        // only inner should be stopped, not outer
        String t2 = "#foreach($j in [1..2])"+template+"#end";
        assertEvalEquals("123 test123 test", t2);

        // stop outer using #stop($foreach.parent)
        String t3 = "#foreach($i in [1..2])#foreach($j in [2..3])$i$j#if($i+$j==5)#stop($foreach.parent)#end#end test#end";
        assertEvalEquals("1213 test2223", t3);
    }

    public void testStopTemplate()
    {
        addTemplate("a", "a#stop($template)b");
        assertTmplEquals("a", "a");
        assertEvalEquals("ac", "#parse('a')c");
    }

    public void testStopEvaluate()
    {
        assertEvalEquals("a", "a#stop($evaluate)b");
        assertEvalEquals("a", "#evaluate('a#stop($evaluate)b')");
        assertEvalEquals("a", "a#evaluate('#stop($evaluate.topmost)')b");
    }

    public void testStopDefineBlock()
    {
        assertEvalEquals("a", "#define($a)a#stop($define)b#end$a");
        assertEvalEquals("aa", "#define($a)a#stop($define.parent)b#end#define($b)a${a}b#end$b");
    }

    public void testStopMacro()
    {
        assertEvalEquals("a ", "#macro(a)a #stop($macro) b#end#a");
        assertEvalEquals("b c ", "#macro(c)c #stop($macro.parent) d#end"+
                               "#macro(b)b #c c#end"+
                               "#b");
    }

    public void testStopMacroBodyBlock()
    {
        assertEvalEquals(" a ", "#macro(a) $bodyContent #end"+
                                "#@a()a#stop($a)b#end");
    }
}