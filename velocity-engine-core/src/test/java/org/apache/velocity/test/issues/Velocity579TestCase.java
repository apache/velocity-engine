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

import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-579 and with some related stuff
 * from VELOCITY-70 thrown in.
 */
public class Velocity579TestCase extends BaseTestCase
{
    public Velocity579TestCase(String name)
    {
       super(name);
    }

    public void testPublicMethodInPrivateImplOfPublicInterface()
    {
        context.put("foo", new Foobar());
        assertEvalEquals("bar", "$foo.foo('bar')");
        assertEvalEquals("$foo.bar()", "$foo.bar()");
    }

    public void testPublicMethodInheritedFromPrivateClass() throws Exception
    {
        context.put("bar", new MyBar());
        // ugly hack to avoid failed test when running JDK 1.5 or earlier
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.6"))
            assertEvalEquals("bar", "$bar.bar()");
    }

    public static interface Foo
    {
        String foo(String s);
    }

    private static abstract class FooImpl implements Foo
    {
        public String foo(String s)
        {
            return s == null ? "foo" : s;
        }
    }

    private static class Foobar extends FooImpl
    {
        public String bar()
        {
            return "bar";
        }
    }

    public static class MyBar extends Foobar
    {
        public String bar()
        {
            return super.bar();
        }
    }

}
