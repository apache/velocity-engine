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

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * This class tests the GlobalDirective functionality.
 */
public class GlobalTestCase extends BaseTestCase
{
    public GlobalTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
    }
    
    public void testSimple()
    {
        // define a macro for testing
        assertEvalEquals("","#macro(foo)#set($macro.bar = \"a\")#global($bar = \"b\")$macro.bar#end");
        assertEvalEquals("ab", "#foo()$bar");
        assertEvalEquals("ab", "#set($bar=\"c\")#foo()$bar");
        assertEvalEquals("ab", "#global($bar=\"c\")#foo()$bar");
    }
    
    public void testNested()
    {
        // define inner macro
        assertEvalEquals("","#macro(inner)#set($macro.bar = \"x\")$macro.bar#global($bar = \"y\")$macro.bar#end");
        // define outer macro
        assertEvalEquals("","#macro(outer)#set($macro.bar = \"a\")$macro.bar#inner()$macro.bar#global($bar = \"b\")$macro.bar#end");
        assertEvalEquals("axxaab","#outer()$bar");
        assertEvalEquals("axxaab","#set($bar = \"z\")#outer()$bar");
    }
    
    public void testExistance()
    {
        assertEvalEquals("yes","#macro(foo)#global($bar = \"b\")#if($bar)yes#{else}no#end#end#foo()");
    }

    public void testExistance2()
    {
        assertEvalEquals("no","#macro(foo)#set($macro.bar = \"b\")#end#foo()#if($bar)yes#{else}no#end");      
    }
    
    public void testProperties()
    {
        assertEvalEquals("1223", "#set($bar = {\"a\":1})$bar.a#macro(foo)#set($macro.bar = {\"a\":2})$macro.bar.a#global($bar.a = 3)$macro.bar.a#end#foo()$bar.a");
    }
    
    public void testProperties2()
    {
        assertEvalException("#macro(foo)#set($macro.bar = {\"a\":2})$macro.bar.a#global($bar.a = 3)$macro.bar.a#end#foo()");        
    }
}
