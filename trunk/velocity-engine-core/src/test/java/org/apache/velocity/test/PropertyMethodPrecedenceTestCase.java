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

import org.apache.velocity.VelocityContext;

/**
 * Used to check that vararg method calls on references work properly
 */
public class PropertyMethodPrecedenceTestCase extends BaseTestCase
{
    public PropertyMethodPrecedenceTestCase(final String name)
    {
        super(name);
        // DEBUG = true;
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("geta", new getGetgetisTool());
        context.put("getA", new GetgetisTool());
        context.put("geta2", new get2getisTool());
        context.put("get_a", new getisTool());
        context.put("isA", new isTool());
    }

    public void testLowercasePropertyMethods()
    {
        assertEvalEquals("getfoo", "$geta.foo");
        assertEvalEquals("getFoo", "$getA.foo");
        assertEvalEquals("get(foo)", "$get_a.foo");
        assertEvalEquals("true", "$isA.foo");
    }

    public void testUppercasePropertyMethods()
    {
        assertEvalEquals("getFoo", "$geta.Foo");
        assertEvalEquals("getfoo", "$geta2.Foo");
        assertEvalEquals("getFoo", "$getA.Foo");
        assertEvalEquals("get(Foo)", "$get_a.Foo");
        assertEvalEquals("true", "$isA.Foo");
    }


    public static class isTool
    {
        public boolean isFoo()
        {
            return true;
        }
    }

    public static class getisTool extends isTool
    {
        public String get(String s)
        {
            return "get("+s+")";
        }
    }

    public static class GetgetisTool extends getisTool
    {
        public String getFoo()
        {
            return "getFoo";
        }
    }

    public static class getGetgetisTool extends GetgetisTool
    {
        public String getfoo()
        {
            return "getfoo";
        }
    }

    public static class get2getisTool extends getisTool
    {
        public String getfoo()
        {
            return "getfoo";
        }
    }

}
