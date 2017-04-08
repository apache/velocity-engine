package org.apache.velocity.test.util.introspection;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.test.misc.TestLogger;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;

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
 * Tests the default uberspector.
 */
public class UberspectImplTestCase extends BaseTestCase
{

    public UberspectImplTestCase(String name)
        throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(UberspectImplTestCase.class);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, new TestLogger());
        engine.addProperty(RuntimeConstants.UBERSPECT_CLASSNAME, "org.apache.velocity.util.introspection.UberspectImpl");
    }

    @Override
    protected void setUpContext(VelocityContext context)
    {
        context.put("privateClass", new PrivateClass());
        context.put("privateMethod", new PrivateMethod());
        context.put("publicMethod", new PublicMethod());
        context.put("iterable", new SomeIterable());
        context.put("over", new OverloadedMethods());
    }

    public void testPrivateIterator()
        throws Exception
    {
        assertEvalEquals("", "#foreach($i in $privateClass)$i#end");
        assertEvalEquals("", "#foreach($i in $privateMethod)$i#end");
        assertEvalEquals("123", "#foreach($i in $publicMethod)$i#end");
    }

    public void testIterableForeach()
    {
        assertEvalEquals("123", "#foreach($i in $iterable)$i#end");
    }

    private class PrivateClass
    {
        public Iterator iterator()
        {
            return Arrays.asList("X", "Y", "Z").iterator();
        }
    }

    public class PrivateMethod
    {
        private Iterator iterator()
        {
            return Arrays.asList("A", "B", "C").iterator();
        }
    }

    public class PublicMethod
    {
        public Iterator iterator()
        {
            return Arrays.asList("1", "2", "3").iterator();
        }
    }

    public class SomeIterable implements Iterable
    {
        public Iterator iterator()
        {
            return Arrays.asList("1", "2", "3").iterator();
        }
    }

    public class OverloadedMethods
    {
        public String foo() { return "foo0"; }
        public String foo(String arg1) { return "foo1"; }
        public String foo(String arg1, String arg2) { return "foo2"; }

        public String bar(Number n, int i) { return "bar1"; }
        public String bar(Number n, String s) { return "bar2"; }
    }

    public void testOverloadedMethods()
    {
        assertEvalEquals("foo0", "$over.foo()");
        assertEvalEquals("foo1", "$over.foo('a')");
        assertEvalEquals("foo1", "$over.foo($null)");
        assertEvalEquals("foo2", "$over.foo('a', 'b')");
        assertEvalEquals("foo2", "$over.foo('a', $null)");
        assertEvalEquals("bar1", "$over.bar(1,1)");
        assertEvalEquals("$over.bar(1,1.1)", "$over.bar(1,1.1)"); // this one is definitely ambiguous
        assertEvalEquals("bar2", "$over.bar(1,'1.1')");
    }
}
