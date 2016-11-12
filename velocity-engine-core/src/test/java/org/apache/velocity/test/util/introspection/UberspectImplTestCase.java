package org.apache.velocity.test.util.introspection;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
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
    public void setUp()
        throws Exception
    {
        Velocity.reset();
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, new TestLogger());
        Velocity.addProperty(RuntimeConstants.UBERSPECT_CLASSNAME,
            "org.apache.velocity.util.introspection.UberspectImpl");
        Velocity.init();
    }

    @Override
    public void tearDown()
    {
    }

    public void testPrivateIterator()
        throws Exception
    {
        VelocityContext context = new VelocityContext();
        context.put("privateClass", new PrivateClass());
        context.put("privateMethod", new PrivateMethod());
        context.put("publicMethod", new PublicMethod());
        StringWriter writer = new StringWriter();

        Velocity.evaluate(context, writer, "test", "#foreach($i in $privateClass)$i#end");
        assertEquals(writer.toString(), "");

        writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "#foreach($i in $privateMethod)$i#end");
        assertEquals(writer.toString(), "");

        writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "#foreach($i in $publicMethod)$i#end");
        assertEquals(writer.toString(), "123");
    }

    public void testIterableForeach()
    {
        VelocityContext context = new VelocityContext();
        context.put("iterable", new SomeIterable());
        StringWriter writer = new StringWriter();

        Velocity.evaluate(context, writer, "test", "#foreach($i in $iterable)$i#end");
        assertEquals(writer.toString(), "123");
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
    }

    public void testOverloadedMethods()
    {
        VelocityContext context = new VelocityContext();
        context.put("over", new OverloadedMethods());
        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "$over.foo()");
        assertEquals(writer.toString(), "foo0");
        writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "$over.foo('a')");
        assertEquals(writer.toString(), "foo1");
        writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "$over.foo($null)");
        assertEquals(writer.toString(), "foo1");
        writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "$over.foo('a', 'b')");
        assertEquals(writer.toString(), "foo2");
        writer = new StringWriter();
        Velocity.evaluate(context, writer, "test", "$over.foo('a', $null)");
        assertEquals(writer.toString(), "foo2");
    }
}
