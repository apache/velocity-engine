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
import org.apache.velocity.exception.MathException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * This class tests support for strict math mode.
 */
public class StrictMathTestCase extends BaseEvalTestCase
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

    public boolean nullmath(String operation)
    {
        try
        {
            evaluate("#set( $foo = $null "+operation+" $num )");
            fail("Doing "+operation+" with $null left side should have thrown a MathException");
        }
        catch (MathException me)
        {
            // success!
        }
        try
        {
            evaluate("#set( $foo = $num "+operation+" $null )");
            fail("Doing "+operation+" with $null right side should have thrown a MathException");
            return false;
        }
        catch (MathException me)
        {
            // success!
            return true;
        }
    }

    public boolean imaginarymath(String operation)
    {
        try
        {
            evaluate("#set( $foo = $num "+operation+" $zero )");
            fail("Doing "+operation+" with $zero right side should have thrown a MathException");
            return false;
        }
        catch (MathException me)
        {
            // success!
            return true;
        }
    }
   

    public void testAdd()
    {
        assertTrue(nullmath("+"));
    }

    public void testSub()
    {
        assertTrue(nullmath("-"));
    }

    public void testMul()
    {
        assertTrue(nullmath("*"));
    }

    public void testMod()
    {
        assertTrue(nullmath("%"));
        assertTrue(imaginarymath("%"));
    }

    public void testDiv()
    {
        assertTrue(nullmath("/"));
        assertTrue(imaginarymath("/"));
    }

}
