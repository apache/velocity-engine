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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Used to check that empty values are properly handled in #if statements
 */
public class IfEmptyTestCase extends BaseTestCase
{
    public IfEmptyTestCase(final String name)
    {
        super(name);
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
        assertEmpty(new NullAsString());
        assertEmpty(new NullAsNumber());
    }

    public void testDataStructures()
    {
        assertEmpty(Collections.emptyMap());
        assertEmpty(Collections.emptyList());
        assertEmpty(new Object[]{});
        List list = new ArrayList();
        list.add(1);
        assertNotEmpty(list);
        Map map = new TreeMap();
        map.put("foo", 1);
        assertNotEmpty(map);
    }

    public void testString()
    {
        assertEmpty("");
        assertEmpty(new EmptyAsString());
        assertNotEmpty("hello");
    }

    public void testNumber()
    {
        assertEmpty(0);
        assertEmpty(0L);
        assertEmpty(0.0f);
        assertEmpty(0.0);
        assertEmpty(BigInteger.ZERO);
        assertEmpty(BigDecimal.ZERO);
        assertNotEmpty(1);
        assertNotEmpty(1L);
        assertNotEmpty(1.0f);
        assertNotEmpty(1.0);
        assertNotEmpty(BigInteger.ONE);
        assertNotEmpty(BigDecimal.ONE);
    }

    public void testStringBuilder()
    {
        StringBuilder builder = new StringBuilder();
        assertEmpty(builder);
        builder.append("yo");
        assertNotEmpty(builder);
    }

    public void testLiterals()
    {
        assertEvalEquals("", "#if( 0 )fail#end");
        assertEvalEquals("", "#if( 0.0 )fail#end");
        assertEvalEquals("", "#if( '' )fail#end");
        assertEvalEquals("", "#if( \"\" )fail#end");
        assertEvalEquals("", "#if( [] )fail#end");
        assertEvalEquals("", "#if( {} )fail#end");

        assertEvalEquals("", "#if( !1 )fail#end");
        assertEvalEquals("", "#if( !1.0 )fail#end");
        assertEvalEquals("", "#if( !'foo' )fail#end");
        assertEvalEquals("", "#if( !\"foo\" )fail#end");
        assertEvalEquals("", "#if( ![ 'foo' ] )fail#end");
        assertEvalEquals("", "#if( !{ 'foo':'bar' } )fail#end");
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
