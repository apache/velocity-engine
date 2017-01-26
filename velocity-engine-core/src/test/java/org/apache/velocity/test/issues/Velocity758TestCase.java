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

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-758.
 */
public class Velocity758TestCase extends BaseTestCase
{
    public Velocity758TestCase(String name)
    {
        super(name);
    }

    public void testNullArgumentForParse()
    {
        assertEvalEquals("", "#parse($foo)");
    }

    public void testOverrideNullArgumentForParse()
    {
        String nullContent = "Parse arg was null";
        addTemplate("null.vm", nullContent);

        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new Handler());
        ec.attachToContext(context);

        assertEvalEquals(nullContent, "#parse($foo)");
    }

    public static class Handler implements IncludeEventHandler
    {
        public String includeEvent(Context context, String parsePath, String parentPath, String directive)
        {
            if (parsePath == null)
            {
                parsePath = "null.vm";
            }
            return parsePath;
        }
    }
}
