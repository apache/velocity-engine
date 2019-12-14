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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.velocity.runtime.parser.node.MathUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Test arithmetic operations. Introduced after extending from Integer-only
 * to Number-handling.
 *
 * @author <a href="mailto:pero@antaramusic.de">Peter Romianowski</a>
 */
public class ArithmeticTestCase extends TestCase
{

    public ArithmeticTestCase(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
       return new TestSuite(ArithmeticTestCase.class);
    }

    public void testAdd()
    {
        addHelper (10, (short) 20, 30, Integer.class);
        addHelper ((byte) 10, (short) 20, 30, Short.class);
        addHelper (10f, (short) 20, 30, Float.class);
        addHelper ((byte) 10, 20d, 30, Double.class);
        addHelper (BigInteger.valueOf(10), 20, 30, BigInteger.class);
        addHelper (20, BigDecimal.valueOf(10),  30, BigDecimal.class);

        // Test overflow
        addHelper (Integer.MAX_VALUE, (short) 20, (double)Integer.MAX_VALUE+20, Long.class);
        addHelper (20, Long.MAX_VALUE, (double)Long.MAX_VALUE+20, BigInteger.class);
        addHelper (-20, Long.MIN_VALUE, (double)Long.MIN_VALUE-20, BigInteger.class);
    }

    private void addHelper (Number n1, Number n2, double expectedResult, Class expectedResultType)
    {
        Number result = MathUtils.add( n1, n2);
        assertEquals("The arithmetic operation produced an unexpected result.", expectedResult, result.doubleValue(), 0.01);
        assertEquals("ResultType does not match.", expectedResultType, result.getClass());
    }

    public void testSubtract()
    {
        subtractHelper (100, (short) 20, 80, Integer.class);
        subtractHelper ((byte) 100, (short) 20, 80, Short.class);
        subtractHelper (100f, (short) 20, 80, Float.class);
        subtractHelper ((byte) 100, 20d, 80, Double.class);
        subtractHelper (BigInteger.valueOf(100), 20, 80, BigInteger.class);
        subtractHelper (100, BigDecimal.valueOf(20),  80, BigDecimal.class);

        // Test overflow
        subtractHelper (Integer.MIN_VALUE, (short) 20, (double)Integer.MIN_VALUE-20, Long.class);
        subtractHelper(-20, Long.MAX_VALUE, -20d - (double) Long.MAX_VALUE, BigInteger.class);
        subtractHelper(Integer.MAX_VALUE, Long.MIN_VALUE, (double) Long.MAX_VALUE + (double) Integer.MAX_VALUE, BigInteger.class);
    }

    private void subtractHelper (Number n1, Number n2, double expectedResult, Class expectedResultType)
    {
        Number result = MathUtils.subtract( n1, n2);
        assertEquals("The arithmetic operation produced an unexpected result.", expectedResult, result.doubleValue(), 0.01);
        assertEquals("ResultType does not match.", expectedResultType, result.getClass());
    }

    public void testMultiply()
    {
        multiplyHelper (10, (short) 20, 200, Integer.class);
        multiplyHelper ((byte) 100, (short) 20, 2000, Short.class);
        multiplyHelper ((byte) 100, (short) 2000, 200000, Integer.class);
        multiplyHelper (100f, (short) 20, 2000, Float.class);
        multiplyHelper ((byte) 100, 20d, 2000, Double.class);
        multiplyHelper (BigInteger.valueOf(100), 20, 2000, BigInteger.class);
        multiplyHelper (100, BigDecimal.valueOf(20),  2000, BigDecimal.class);

        // Test overflow
        multiplyHelper (Integer.MAX_VALUE, (short) 10, (double)Integer.MAX_VALUE*10d, Long.class);
        multiplyHelper(Integer.MAX_VALUE, (short) -10, (double) Integer.MAX_VALUE * -10d, Long.class);
        multiplyHelper(20, Long.MAX_VALUE, 20d * (double) Long.MAX_VALUE, BigInteger.class);
    }

    private void multiplyHelper (Number n1, Number n2, double expectedResult, Class expectedResultType)
    {
        Number result = MathUtils.multiply( n1, n2);
        assertEquals("The arithmetic operation produced an unexpected result.", expectedResult, result.doubleValue(), 0.01);
        assertEquals("ResultType does not match.", expectedResultType, result.getClass());
    }

    public void testDivide()
    {
        divideHelper (10, (short) 2, 5, Integer.class);
        divideHelper ((byte) 10, (short) 2, 5, Short.class);
        divideHelper (BigInteger.valueOf(10), (short) 2, 5, BigInteger.class);
        divideHelper (10, (short) 4, 2, Integer.class);
        divideHelper (10, 2.5f, 4, Float.class);
        divideHelper(10, 2.5, 4, Double.class);
        divideHelper(10, new BigDecimal(2.5), 4, BigDecimal.class);
    }

    private void divideHelper (Number n1, Number n2, double expectedResult, Class expectedResultType)
    {
        Number result = MathUtils.divide( n1, n2);
        assertEquals("The arithmetic operation produced an unexpected result.", expectedResult, result.doubleValue(), 0.01);
        assertEquals("ResultType does not match.", expectedResultType, result.getClass());
    }

    public void testModulo()
    {
        moduloHelper (10, (short) 2, 0, Integer.class);
        moduloHelper ((byte) 10, (short) 3, 1, Short.class);
        moduloHelper(BigInteger.valueOf(10), (short) 4, 2, BigInteger.class);
        moduloHelper(10, 5.5f, 4.5, Float.class);

        try
        {
            moduloHelper (10, new BigDecimal( 2.5), 4, BigDecimal.class);
            fail ("Modulo with BigDecimal is not allowed! Should have thrown an ArithmeticException.");
        }
        catch( ArithmeticException e)
        {
            // do nothing
        }
    }

    private void moduloHelper (Number n1, Number n2, double expectedResult, Class expectedResultType)
    {
        Number result = MathUtils.modulo( n1, n2);
        assertEquals("The arithmetic operation produced an unexpected result.", expectedResult, result.doubleValue(), 0.01);
        assertEquals("ResultType does not match.", expectedResultType, result.getClass());
    }

    public void testCompare()
    {
        compareHelper (10, (short) 10, 0);
        compareHelper (10, (short) 11, -1);
        compareHelper (BigInteger.valueOf(10), (short) 11, -1);
        compareHelper ((byte) 10, (short) 3, 1);
        compareHelper(10f, (short) 11, -1);
        compareHelper(10d, (short) 11, -1);
    }

    private void compareHelper (Number n1, Number n2, int expectedResult)
    {
        int result = MathUtils.compare( n1, n2 );
        assertEquals("The arithmetic operation produced an unexpected result.", expectedResult, result);
    }

    public void testNegate()
    {
        negateHelper((byte) 1, -1, Byte.class);
        negateHelper((short) 1, -1, Short.class);
        negateHelper(1, -1, Integer.class);
        negateHelper(1L, -1, Long.class);
        negateHelper(BigInteger.valueOf(1), -1, BigInteger.class);
        negateHelper(BigDecimal.valueOf(1), -1, BigDecimal.class);
        negateHelper(Long.MIN_VALUE, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1)).doubleValue(), BigInteger.class);
    }

    private void negateHelper(Number n, double expectedResult, Class expectedResultType)
    {
        Number result = MathUtils.negate(n);
        assertEquals ("The arithmetic operation produced an unexpected result.", expectedResult, result.doubleValue(), 0.01);
        assertEquals ("ResultType does not match.", expectedResultType, result.getClass());
    }

/*
 *
 *    COMMENT OUT FOR PERFORMANCE-MEASSUREMENTS
 *
 *    public void testProfile()
 *    {
 *
 *        long start = System.currentTimeMillis();
 *
 *        Number v1 = new Long (1000);
 *        Number v2 = new Double (10.23);
 *        Number result = null;
 *        for (int a = 0; a < 10000; a++)
 *        {
 *
 *            result = MathUtils.typeConvert (
 *                new BigDecimal (v1.doubleValue()).add (
 *                new BigDecimal (v2.doubleValue())), v1, v2, false);
 *
 *        }
 *
 *        System.out.println ("took: "+(System.currentTimeMillis()-start));
 *
 *        start = System.currentTimeMillis();
 *        for (int a = 0; a < 10000; a++)
 *        {
 *
 *            result = MathUtils.divide( v1, v2);
 *        }
 *
 *        Number result2 = result;
 *        System.out.println ("took: "+(System.currentTimeMillis()-start));
 *    }
 *
 */

    /**
     * Test additional functions
     */
    public void testIsZero()
    {
        assertTrue (MathUtils.isZero (0));
        assertTrue (!MathUtils.isZero (1));
        assertTrue (!MathUtils.isZero (-1));

        assertTrue (MathUtils.isZero (0f));
        assertTrue (!MathUtils.isZero (0.00001f));
        assertTrue (!MathUtils.isZero (-0.00001f));

    }

}
