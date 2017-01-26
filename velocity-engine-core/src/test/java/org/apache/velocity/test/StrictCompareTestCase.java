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

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Make sure exceptions are thrown for strict comparisons that
 * cannot be compared.
 */
public class StrictCompareTestCase extends BaseTestCase
{
    public StrictCompareTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
        context.put("NULL", null);
        context.put("a", "abc");
        context.put("b", new Integer(3));
        context.put("TRUE", Boolean.TRUE);
    }

    public void testCompare()
    {
        assertVelocityEx("#if($a > $NULL)#end");
        assertVelocityEx("#if($a < $NULL)#end");
        assertVelocityEx("#if($a >= $NULL)#end");
        assertVelocityEx("#if($a <= $NULL)#end");

        assertVelocityEx("#if($NULL > $a)#end");
        assertVelocityEx("#if($NULL < $a)#end");
        assertVelocityEx("#if($NULL >= $a)#end");
        assertVelocityEx("#if($NULL <= $a)#end");

        assertVelocityEx("#if($NULL >= $NULL)#end");
        assertVelocityEx("#if($a >= $b)#end");
        assertVelocityEx("#if($a <= $b)#end");
        assertVelocityEx("#if($a > $b)#end");
        assertVelocityEx("#if($a < $b)#end");

        assertVelocityEx("#if($a < 5)#end");
        assertVelocityEx("#if($a > 5)#end");
    }

    /**
     * Assert that we get a VelocityException when calling evaluate
     */
    public void assertVelocityEx(String template)
    {
        assertEvalException(template, VelocityException.class);
    }
}
