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
 * This class tests passing expressions as method arguments
 */

public class ExpressionAsMethodArgumentTestCase extends BaseTestCase
{
    public ExpressionAsMethodArgumentTestCase(final String name)
    {
        super(name);
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("tool",new MyAbsTool());
        context.put("foo",2);
        context.put("bar",-3);
    }

    public void testExpressionAsMethod()
    {
        assertEvalEquals("6","$tool.abs( $foo * $bar )");
        assertEvalEquals("12","$tool.abs( $foo * $tool.abs( $foo * $bar ) )");
    }

    public static class MyAbsTool
    {
        public int abs(int num)
        {
            return Math.abs(num);
        }
    }
}
