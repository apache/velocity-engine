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
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-855.
 */
public class Velocity924TestCase extends BaseTestCase
{
    public Velocity924TestCase(String name)
    {
        super(name);
    }

    public static class Foo
    {
        public String getName() { return "foo"; }
    }

    @Override
    protected void setUpContext(VelocityContext context)
    {
        context.put("var", new Foo());
    }

    public void testVelocity924Getter()
    {
        assertEvalEquals("org.apache.velocity.test.issues.Velocity924TestCase$Foo foo", "$var.class.name $var.name");
    }

    public void testVelocity924Method()
    {
        assertEvalEquals("org.apache.velocity.test.issues.Velocity924TestCase$Foo foo", "$var.class.getName() $var.getName()");assertEvalEquals("org.apache.velocity.test.issues.Velocity924TestCase$Foo foo", "$var.class.name $var.name");
    }

    public void testVelocity924Getter2()
    {
        assertEvalEquals("foo org.apache.velocity.test.issues.Velocity924TestCase$Foo", "$var.name $var.class.name");
    }

    public void testVelocity924Method2()
    {
        assertEvalEquals("foo org.apache.velocity.test.issues.Velocity924TestCase$Foo", "$var.getName() $var.class.getName()");assertEvalEquals("org.apache.velocity.test.issues.Velocity924TestCase$Foo foo", "$var.class.name $var.name");
    }
}
