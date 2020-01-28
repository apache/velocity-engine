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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-904.
 */
public class Velocity904TestCase extends BaseTestCase
{
    public Velocity904TestCase(String name)
    {
       super(name);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine)
    {
        // that will also test the deprecation of velocimacro.arguments.preserve_literals towards velocimacro.enable_bc_mode
        engine.setProperty("velocimacro.arguments.preserve_literals", getName().contains("NoPreserve") ? "false" : "true");
    }

    public void testNullArgNoPreserve()
    {
        assertEvalEquals("$parameter", "#macro(testmacro $parameter)$parameter#end#testmacro($return)");
    }

    public void testNullArgPreserve()
    {
        assertEvalEquals("$return", "#macro(testmacro $parameter)$parameter#end#testmacro($return)");
    }

    public void testArgSetToNullNoPreserve()
    {
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#set($variable = 'value')#mymacro($variable)");
    }

    public void testArgSetToNullPreserve()
    {
        assertEvalEquals("$variable", "#macro(mymacro $input)#set($input = $null)$input#end#set($variable = 'value')#mymacro($variable)");
    }

    public void testSubMacroNoPreserve()
    {
        assertEvalEquals("$return$return$return", "#macro(macro1 $return)$return#macro2($param2)$return#end#macro(macro2 $return)$return#end#macro1($param)");
    }

    public void testSubMacroPreserve()
    {
        assertEvalEquals("$param$param2$param", "#macro(macro1 $return)$return#macro2($param2)$return#end#macro(macro2 $return)$return#end#macro1($param)");
    }

    public void testNoArgNoPreserve()
    {
        assertEvalEquals("","#macro(testMacro $param)#end#testMacro()");
    }

    public void testNoArgPreserve()
    {
        assertEvalEquals("","#macro(testMacro $param)#end#testMacro()");
    }

    public void testConstantSetToNullNoPreserve()
    {
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro('string-value')");
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro(\"interpolated-$bar-value\")");
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro(true)");
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro(4.5)");
    }

    public void testConstantSetToNullPreserve()
    {
        assertEvalEquals("'string-value'", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro('string-value')");
        assertEvalEquals("\"interpolated-$bar-value\"", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro(\"interpolated-$bar-value\")");
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro(true)");
        assertEvalEquals("$input", "#macro(mymacro $input)#set($input = $null)$input#end#mymacro(4.5)");
    }

    public void testConstantNoPreserve()
    {
        assertEvalEquals("true", "#macro(mymacro $input)$input#end#mymacro(true)");
        assertEvalEquals("1.5", "#macro(mymacro $input)$input#end#mymacro(1.5)");
        assertEvalEquals("foo", "#macro(mymacro $input)$input#end#mymacro('foo')");
        assertEvalEquals("{}", "#macro(mymacro $input)$input#end#mymacro({})");
        assertEvalEquals("[]", "#macro(mymacro $input)$input#end#mymacro([])");
    }

    public void testConstantPreserve()
    {
        assertEvalEquals("true", "#macro(mymacro $input)$input#end#mymacro(true)");
        assertEvalEquals("1.5", "#macro(mymacro $input)$input#end#mymacro(1.5)");
        assertEvalEquals("foo", "#macro(mymacro $input)$input#end#mymacro('foo')");
        assertEvalEquals("{}", "#macro(mymacro $input)$input#end#mymacro({})");
        assertEvalEquals("[]", "#macro(mymacro $input)$input#end#mymacro([])");
    }

}
