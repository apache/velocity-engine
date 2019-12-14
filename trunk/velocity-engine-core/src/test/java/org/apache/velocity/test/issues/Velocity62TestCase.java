package org.apache.velocity.test.issues;

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

import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-62.
 */
public class Velocity62TestCase extends BaseTestCase
{
    public Velocity62TestCase(String name)
    {
       super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        context.put("foo", "foo");
    }

    public void testNested()
    {
        String template = "#macro( outer )#set( $foo = 'bar' )#inner()#end"+
                          "#macro( inner )$foo#end"+
                          "#inner()#outer()#inner()";
        assertEvalEquals("foobarbar", template);
    }

    public void testRecursive()
    {
        context.put("i", 1);
        String template = "#macro(recurse $i)"+
                            "$i"+
                            "#if( $i < 5 )"+
                              "#set( $i = $i + 1 )"+
                              "#recurse($i)"+
                            "#end"+
                          "#end"+
                          "#recurse(1)";
        assertEvalEquals("12345", template);
    }

}
