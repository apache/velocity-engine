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

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests the fix for VELOCITY-961.
 */
public class Velocity961TestCase extends BaseTestCase
{
    public Velocity961TestCase(String name)
    {
       super(name);
    }

    public void test1()
    {
        String template = "$child.typeName()#if($child.isRepeated())[]#end";
        assertEvalEquals("$child.typeName()", template);
    }

    public void test2()
    {
        String template = "$child.typeName()#if(1)[]#end";
        assertEvalEquals("$child.typeName()[]", template);
    }
}
