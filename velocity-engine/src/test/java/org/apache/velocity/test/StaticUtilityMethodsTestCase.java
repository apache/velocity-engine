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


/**
 * This class tests support for putting static utility classes
 * like java.lang.Math directly into the context in order to
 * use their methods.
 */
public class StaticUtilityMethodsTestCase extends BaseTestCase
{
    public StaticUtilityMethodsTestCase(String name)
    {
       super(name);
    }

    public void testMath()
    {
        context.put("Math", Math.class);
        assertEvalEquals("java.lang.Math", "$Math.name");
        assertEvalEquals("3.0", "$Math.ceil(2.5)");
    }

    public void testFoo()
    {
        context.put("Foo", Foo.class);
        assertEvalEquals("test", "$Foo.foo('test')");
    }


    public static class Foo
    {
        private Foo() {}
        public static String foo(String s)
        {
            return s == null ? "foo" : s;
        }
    }
}
