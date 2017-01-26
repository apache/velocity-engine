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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.test.provider.NumberMethods;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Used to check that method calls with number parameters are executed correctly.
 *
 * @author <a href="mailto:wglass@forio.com">Peter Romianowski</a>
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 */
public class NumberMethodCallsTestCase extends TestCase
{
    private VelocityEngine ve = null;

    private final static boolean PRINT_RESULTS = false;

    /**
     * Default constructor.
     */
    public NumberMethodCallsTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        ve = new VelocityEngine();
        ve.init();
    }

    public void init( RuntimeServices rs )
    {
        // do nothing with it
    }

    public static Test suite ()
    {
        return new TestSuite(NumberMethodCallsTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testNumberMethodCalls ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();

        // context object with overloaded methods with number arguments
        vc.put("Test",new NumberMethods());

        // numbers for context
        vc.put("AByte",new Byte("10"));
        vc.put("AShort",new Short("10"));
        vc.put("AInteger",new Integer(10));
        vc.put("ALong",new Long(10));
        vc.put("ADouble",new Double(10));
        vc.put("AFloat",new Float(10));
        vc.put("ABigDecimal",new BigDecimal(10));
        vc.put("ABigInteger",new BigInteger("10"));

        // check context objects
        System.out.println("Testing: method calls with arguments as context objects");
        checkResults(vc,"$Test.numMethod($AByte)","byte (10)");
        checkResults(vc,"$Test.numMethod($AShort)","short (10)");
        checkResults(vc,"$Test.numMethod($AInteger)","int (10)");
        checkResults(vc,"$Test.numMethod($ADouble)","double (10.0)");
        checkResults(vc,"$Test.numMethod($AFloat)","double (10.0)");
        checkResults(vc,"$Test.numMethod($ALong)","long (10)");
        checkResults(vc,"$Test.numMethod($ABigDecimal)","BigDecimal (10)");
        checkResults(vc,"$Test.numMethod($ABigInteger)","BigInteger (10)");

        // check literals
        //    -- will cast floating point literal to smallest possible of Double, BigDecimal
        //    -- will cast integer literal to smallest possible of Integer, Long, BigInteger
        System.out.println("Testing: method calls with arguments as literals");
        checkResults(vc,"$Test.numMethod(10.0)","double (10.0)");
        checkResults(vc,"$Test.numMethod(10)","int (10)");
        checkResults(vc,"$Test.numMethod(10000000000)","long (10000000000)");
        checkResults(vc,"$Test.numMethod(10000000000000000000000)","BigInteger (10000000000000000000000)");

        // check calculated results
        // -- note calculated value is cast to smallest possible type
        // -- method invoked is smallest relevant method
        // -- it's an unusual case here of both byte and int methods, but this works as expected
        System.out.println("Testing: method calls with arguments as calculated values");
        checkResults(vc,"#set($val = 10.0 + 1.5)$Test.numMethod($val)","double (11.5)");
        checkResults(vc,"#set($val = 100 + 1)$Test.numMethod($val)","int (101)");
        checkResults(vc,"#set($val = 100 * 1000)$Test.numMethod($val)","int (100000)");
        checkResults(vc,"#set($val = 100 + 1.5)$Test.numMethod($val)","double (101.5)");
        checkResults(vc,"#set($val = $ALong + $AInteger)$Test.numMethod($val)","long (20)");
        checkResults(vc,"#set($val = $ABigInteger + $AInteger)$Test.numMethod($val)","BigInteger (20)");
    }


    private void checkResults(Context vc, String template, String compare) throws Exception
    {

        StringWriter writer = new StringWriter();
        ve.evaluate( vc, writer, "test", template);
        assertEquals("Incorrect results for template '" + template + "'.",compare,writer.toString());

        if (PRINT_RESULTS)
            System.out.println ("Method call successful: " + template);

    }


}
