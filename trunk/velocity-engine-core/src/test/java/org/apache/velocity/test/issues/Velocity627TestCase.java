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

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-627.  Make sure Foreach
 * Error message reports correct line numbers.
 */

public class Velocity627TestCase extends BaseTestCase
{
    public Velocity627TestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.SKIP_INVALID_ITERATOR, Boolean.FALSE);
    }

    public void test627()
    {
        // Make sure the error ouput contains "line 3, column 16"
        assertEvalExceptionAt("##\n##\n#foreach($i in \"junk\")blaa#end", 3, 16);
    }
}
