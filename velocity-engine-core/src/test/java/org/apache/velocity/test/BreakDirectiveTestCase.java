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

import org.apache.velocity.app.VelocityEngine;

/**
 * This class tests the break directive.
 */
public class BreakDirectiveTestCase extends BaseTestCase
{
    public BreakDirectiveTestCase(String name)
    {
        super(name);
    }

    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty("a.provide.scope.control", "true");
        engine.setProperty("define.provide.scope.control", "true");
        engine.setProperty("evaluate.provide.scope.control", "true");
        engine.setProperty("macro.provide.scope.control", "true");
        engine.setProperty("template.provide.scope.control", "true");
    }

    public void testBadArgs()
    {
        context.put("foo","foo");
        assertEvalException("#break($null)");
        assertEvalException("#break($foo)");
        assertEvalException("#break(true)");
        assertEvalException("#break(1.2)");
        assertEvalException("#break([0..1])");
        assertEvalException("#break( $too $many )");
    }

    public void testStopForeach()
    {
        String template = "#foreach($i in [1..5])$i#if($i>2)#break($foreach)#end#end test";
        assertEvalEquals("123 test", template);

        // only inner should be stopped, not outer
        String t2 = "#foreach($j in [1..2])"+template+"#end";
        assertEvalEquals("123 test123 test", t2);

        // stop outer using #break($foreach.parent)
        String t3 = "#foreach($i in [1..2])#foreach($j in [2..3])$i$j#if($i+$j==5)#break($foreach.parent)#end#end test#end";
        assertEvalEquals("1213 test2223", t3);

        // without specifying scope...
        assertEvalEquals("1, 2, 3, 4, 5",
                         "#foreach($i in [1..10])$i#if($i > 4)#break#end, #end");
        assertEvalEquals("1", "#foreach($i in [1..5])$i#break #end");
        assertEvalEquals("~~~, ~~, ~, ",
            "#foreach($i in [1..3])#foreach($j in [2..4])#if($i*$j >= 8)#break#end~#end, #end");
    }

    public void testStopTemplate()
    {
        addTemplate("a", "a#break($template)b");
        assertTmplEquals("a", "a");
        assertEvalEquals("ac", "#parse('a')c");

        addTemplate("b", "b#{break}a");
        assertTmplEquals("b", "b");
    }

    public void testStopEvaluate()
    {
        assertEvalEquals("a", "a#break($evaluate)b");
        assertEvalEquals("a", "#evaluate('a#break($evaluate)b')");
        assertEvalEquals("a", "a#evaluate('#break($evaluate.topmost)')b");
        assertEvalEquals("a", "a#{break}b");
    }

    public void testStopDefineBlock()
    {
        assertEvalEquals("a", "#define($a)a#break($define)b#end$a");
        assertEvalEquals("aa", "#define($a)a#break($define.parent)b#end#define($b)a${a}b#end$b");
        assertEvalEquals("a", "#define($a)a#{break}b#end$a");
    }

    public void testStopMacro()
    {
        assertEvalEquals("a ", "#macro(a)a #break($macro) b#end#a");
        assertEvalEquals("b c ", "#macro(c)c #break($macro.parent) d#end"+
                               "#macro(b)b #c c#end"+
                               "#b");
        assertEvalEquals("d", "#macro(d)d#{break}e#end#d");
    }

    public void testStopMacroBodyBlock()
    {
        assertEvalEquals(" a ", "#macro(a) $bodyContent #end"+
                                "#@a()a#break($a)b#end");
        assertEvalEquals(" b ", "#macro(b) $bodyContent #end"+
                                "#@b()b#{break}c#end");
    }
}
