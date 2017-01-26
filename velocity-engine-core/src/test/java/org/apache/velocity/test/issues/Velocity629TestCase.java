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
 * This class tests VELOCITY-629.  Make sure string literals
 * Error message reports correct line and column numbers.
 */
public class Velocity629TestCase extends BaseTestCase
{
    public Velocity629TestCase(String name)
    {
        super(name);
    }

    public void test629()
    {
        String template = "##\n"+
                          "##\n"+
                          "#set($list=[1])#set($x=\"\n"+
                          "$list.get(1)\n"+
                          "\")";
        // Make sure the error ouput contains "line 4, column 7" if not throw
        assertEvalExceptionAt(template, 4, 7);

        template = "##\n"+
                   "##\n"+
                   "#set($x=\"#if\")";
        assertEvalExceptionAt(template, 3, 9);

        template = "##\n"+
                   "##\n"+
                   "#macro(test $i)$i#end#set($list=[1])#test(\"$list.get(1)\")";
        assertEvalExceptionAt(template, 3, 50);
    }
}
