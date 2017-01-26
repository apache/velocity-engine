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

import org.apache.velocity.exception.MathException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * This class tests support for strict math mode.
 */
public class StrictMathTestCase extends BaseTestCase
{
    public StrictMathTestCase(String name)
    {
       super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.STRICT_MATH, Boolean.TRUE);
        context.put("num", new Integer(5));
        context.put("zero", new Integer(0));
    }

    protected void assertNullMathEx(String operation)
    {
        String leftnull = "#set( $foo = $null "+operation+" $num )";
        assertEvalException(leftnull, MathException.class);
        String rightnull = "#set( $foo = $num "+operation+" $null )";
        assertEvalException(rightnull, MathException.class);
    }

    protected void assertImaginaryMathEx(String operation)
    {
        String infinity = "#set( $foo = $num "+operation+" $zero )";
        assertEvalException(infinity, MathException.class);
    }


    public void testAdd()
    {
        assertNullMathEx("+");
    }

    public void testSub()
    {
        assertNullMathEx("-");
    }

    public void testMul()
    {
        assertNullMathEx("*");
    }

    public void testMod()
    {
        assertNullMathEx("%");
        assertImaginaryMathEx("%");
    }

    public void testDiv()
    {
        assertNullMathEx("/");
        assertImaginaryMathEx("/");
    }

}
