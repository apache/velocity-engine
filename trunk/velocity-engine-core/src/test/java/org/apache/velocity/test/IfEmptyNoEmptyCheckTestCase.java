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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Used to check that empty values are properly handled in #if statements
 */
public class IfEmptyNoEmptyCheckTestCase extends BaseTestCase
{
    public IfEmptyNoEmptyCheckTestCase(final String name)
    {
        super(name);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty(RuntimeConstants.CHECK_EMPTY_OBJECTS, "false");
    }

    protected void assertEmpty(Object obj)
    {
        context.put("obj", obj);
        assertEvalEquals("", "#if( $obj )fail#end");
    }

    protected void assertNotEmpty(Object obj)
    {
        context.put("obj", obj);
        assertEvalEquals("", "#if( !$obj )fail#end");
    }

    public void testNull()
    {
        assertEmpty(null);
        assertNotEmpty(new NullAsString());
        assertNotEmpty(new NullAsNumber());
    }

    public void testDataStructures()
    {
        assertNotEmpty(Collections.emptyMap());
        assertNotEmpty(Collections.emptyList());
        assertNotEmpty(new Object[]{});
    }

    public void testString()
    {
        assertNotEmpty("");
        assertNotEmpty(new EmptyAsString());
    }

    public void testNumber()
    {
        assertNotEmpty(0);
    }

    public void testStringBuilder()
    {
        StringBuilder builder = new StringBuilder();
        assertNotEmpty(builder);
    }

    public void testLiterals()
    {
        assertEvalEquals("", "#if( !0 )fail#end");
        assertEvalEquals("", "#if( !0.0 )fail#end");
        assertEvalEquals("", "#if( !'' )fail#end");
        assertEvalEquals("", "#if( !\"\" )fail#end");
        assertEvalEquals("", "#if( ![] )fail#end");
        assertEvalEquals("", "#if( !{} )fail#end");
    }

    public static class NullAsString
    {
        public String getAsString()
        {
            return null;
        }
    }

    public static class EmptyAsString
    {
        public String getAsString()
        {
            return "";
        }
    }

    public static class NullAsNumber
    {
        public String getAsNumber()
        {
            return null;
        }
    }

}
