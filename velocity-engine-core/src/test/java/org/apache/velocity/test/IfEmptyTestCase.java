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

import java.util.Collections;

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
    }

    public void testString()
    {
        assertEmpty("");
        assertEmpty(new EmptyAsString());
        assertEmpty(new EmptyToString());
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

    public static class EmptyToString
    {
        public String toString()
        {
            return "";
        }
    }

}


