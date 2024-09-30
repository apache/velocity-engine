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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.util.introspection.ClassMap;
import org.apache.velocity.util.introspection.MethodMap;

import java.lang.reflect.Method;
import java.util.TimeZone;

/**
 * This class tests the fix for VELOCITY-952.
 */
public class Velocity952TestCase extends BaseTestCase
{
    public Velocity952TestCase(String name)
    {
       super(name);
    }

    public void testMethodMap()
    {
        ClassMap classMap = new ClassMap(TimeZone.getDefault().getClass(), Velocity.getLog());
        Method getOffset = classMap.findMethod("getOffset", new Object[] { 1L });
        assertNotNull(getOffset);
        assertEquals(TimeZone.class, getOffset.getDeclaringClass());
    }

    public interface Foo
    {
        default String foo() { return "foo"; }
    }

    public static class Bar implements Foo
    {
        @Override
        public String foo()
        {
            return "bar";
        }

        public static String staticFoo()
        {
            return "static bar";
        }
    }

    public static class Baz extends Bar
    {
        @Override
        public String foo()
        {
            return "baz";
        }

        public static String staticFoo()
        {
            return "static baz";
        }
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("tz", TimeZone.getDefault());
        context.put("bar", new Bar());
        context.put("baz", new Baz());
    }

    public void testEnd2End()
    {
        assertEvalEquals("3600000", "$tz.getOffset(1)");
    }

    public void testBar()
    {
        assertEvalEquals("bar", "$bar.foo()");
        assertEvalEquals("static bar", "$bar.staticFoo()");
    }

    public void testBaz()
    {
        assertEvalEquals("baz", "$baz.foo()");
        assertEvalEquals("static baz", "$baz.staticFoo()");
    }
}
