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
 * This class tests VELOCITY-785.
 */
public class Velocity785TestCase extends BaseTestCase
{
    public Velocity785TestCase(String name)
    {
        super(name);
        // DEBUG = true;
    }

    public void testQuoteEscapes()
    {
        assertEvalEquals("\"", "#set($double_double = \"\"\"\")$double_double");
        assertEvalEquals("'", "#set($single_single = '''')$single_single");
        assertEvalEquals("''", "#set($double_single = \"''\")$double_single");
        assertEvalEquals("\"\"", "#set($single_double = '\"\"')$single_double");
    }
}
