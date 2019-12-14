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
 * This class tests VELOCITY-701.
 */
public class Velocity701TestCase extends BaseTestCase
{
    public Velocity701TestCase(String name)
    {
        super(name);
        //DEBUG = true;
    }

    public void testAbstractClass()
    {
        context.put("foo", new Foo() {
            public String getBar() {
                return "bar";
            }
        });
        assertEvalEquals("bar", "$foo.bar");
    }

    public static abstract class Foo {

        public abstract String getBar();

    }

    public void testEnum()
    {
        context.put("bar", Bar.ONE);
        assertEvalEquals("foo", "$bar.foo");
    }

    public enum Bar {

        ONE(){
            public String getFoo() {
                return "foo";
            }
        };

       //This was an abstract method, but Velocity 1.6 quit working with it.
       public abstract String getFoo();

    }

}
