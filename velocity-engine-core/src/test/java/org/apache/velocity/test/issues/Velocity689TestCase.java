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
 * This class tests VELOCITY-689.
 */
public class Velocity689TestCase extends BaseTestCase
{
    public Velocity689TestCase(String name)
    {
        super(name);
        //DEBUG = true;
    }

    public void setUpContext(VelocityContext ctx)
    {
        ctx.put("foo", new Foo());
    }

    public void testIt()
    {
        String template = "$foo.baz, $foo.bar";
        assertEvalEquals("baz, bar", template);
    }

    public static interface HasMethod
    {
        String getBar();
    }

    public static interface HasOtherMethod extends HasMethod
    {
        String getBaz();
    }

    public static interface NoMethod extends HasOtherMethod
    {
        // nada!
    }

    private static class Foo implements NoMethod
    {
        public String getBar()
        {
            return "bar";
        }

        public String getBaz()
        {
            return "baz";
        }
    }

}
