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
 * This class tests VELOCITY-644.  Make sure the reported filename
 * is correct in exceptions when an error occurs in another template file.
 */
public class Velocity644TestCase extends BaseTestCase
{
    public Velocity644TestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        engine.setProperty(RuntimeConstants.VM_LIBRARY, "testCase644.vm");
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
        context.put("NULL", null);
    }

    public void test629()
    {
        // Calling a null method
        assertEvalExceptionAt("#nullMethod()", "testCase644.vm", 9, 8);
        // An invalid array
        assertEvalExceptionAt("#arrayError()", "testCase644.vm", 4, 8);
        // An invalid reference
        assertEvalExceptionAt("#badRef()", "testCase644.vm", 13, 3);
        // Non iterable object
        assertEvalExceptionAt("#forloop()", "testCase644.vm", 18, 18);
    }
}
