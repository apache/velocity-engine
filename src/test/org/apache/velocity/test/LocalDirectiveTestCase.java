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
 * This class tests the LocalDirective functionality.
 */
public class LocalDirectiveTestCase extends BaseTestCase
{
    public LocalDirectiveTestCase(String name)
    {
        super(name);
    }

    public void testSimple() throws Exception
    {
        String template = "#macro(foo $value) #local($counter = $value + 1)$counter#end #set($counter = 2) $counter #foo($counter) $counter";
        String result = " 2 3 2";

        assertEvalEquals(result, template);
    }

    public void testNestedCallsWithLocal() throws Exception
    {
        String template = "#set($a = 1) #macro(bar $n)#local($a = $n+ 1) $a #end #macro(foo $num)#local($a = $num + 1) $a #bar($a)#end #foo(1) $a";
        String result = "    2  3  1";

        assertEvalEquals(result, template);
    }

    public void testLocalWithMap() throws Exception
    {
        String template = "#macro(foo)#local($map.foo = 'bar')$map.foo#end #set($map = { 'foo': 'woogie' }) #foo() $map.foo";
        String result = " woogie woogie";

        assertEvalEquals(result, template);
    }
}
