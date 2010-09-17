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


/**
 * This class tests support for string concatenation.
 */
public class StringConcatenationTestCase extends BaseTestCase
{
    public StringConcatenationTestCase(String name)
    {
       super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        context.put("foo", "foo");
        context.put("baz", "baz");
    }

    public void testStringRefLeft()
    {
        assertEvalEquals("foobar", "#set( $o = $foo + 'bar' )$o");
        assertEvalEquals("foo$bar", "#set( $o = $foo + $bar )$o");
        assertEvalEquals("foo1", "#set( $o = $foo + 1 )$o");
        assertEvalEquals("foobaz", "#set( $o = $foo + $baz )$o");
    }

    public void testStringRefRight()
    {
        assertEvalEquals("barfoo", "#set( $o = 'bar' + $foo )$o");
        assertEvalEquals("$barfoo", "#set( $o = $bar + $foo )$o");
        assertEvalEquals("1foo", "#set( $o = 1 + $foo )$o");
    }

    public void testNoRef()
    {
        assertEvalEquals("bar1", "#set( $o = 'bar' + 1 )$o");
    }

    public void testAll()
    {
        assertEvalEquals("foobar$bar1baz", "#set( $o = $foo + 'bar' + $bar + 1 + $baz )$o");
    }

}
