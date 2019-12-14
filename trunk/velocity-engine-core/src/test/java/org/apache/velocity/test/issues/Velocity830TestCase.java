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
 * This class tests the VELOCITY-830 issue.
 *
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 */
public class Velocity830TestCase extends BaseTestCase
{
    public Velocity830TestCase(String name)
    {
        super(name);
    }

    public static class UnderscoreMethodObject
    {
        public String check() { return "ok"; }
        public String _1() { return "gotit"; }
    }

    @Override
    protected void setUpContext(VelocityContext context)
    {
        context.put("obj", new UnderscoreMethodObject());
    }

    /**
     * Tests methods name beginning with _
     */
    public void testUnderscoreMethod()
        throws Exception
    {
        assertEvalEquals("ok", "$obj.check()");
        assertEvalEquals("gotit", "$obj._1()");
    }
}
