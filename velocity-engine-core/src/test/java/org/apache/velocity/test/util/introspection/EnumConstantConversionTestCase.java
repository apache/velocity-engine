package org.apache.velocity.test.util.introspection;

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
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.test.BaseTestCase;

/**
 * Tests DeprecatedCheckUberspector
 */
public class EnumConstantConversionTestCase extends BaseTestCase {

    public EnumConstantConversionTestCase(String name)
    	throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EnumConstantConversionTestCase.class);
    }

    public static class Obj
    {
        public enum Color { RED, GREEN };
        public String getAction(Color color)
        {
            switch (color)
            {
                case RED: return "Stop";
                case GREEN: return "Go";
                default: return "???";
            }
        }
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("obj", new Obj());
    }

    public void testStringToEnumConversion()
    	throws Exception
    {
        assertEvalEquals("Stop", "$obj.getAction('RED')");
        assertEvalEquals("Go", "$obj.getAction('GREEN')");
        try
        {
            String result = evaluate("$obj.getAction('BLUE')");
            fail();
        }
        catch(MethodInvocationException mie) {}
    }
}
