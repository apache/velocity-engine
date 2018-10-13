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

import junit.framework.TestCase;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.test.misc.TestLogger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Base test case that provides utility methods for
 * the rest of the tests.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 * @version $Id$
 */
public class AlternateValuesTestCase extends BaseTestCase
{
    public AlternateValuesTestCase(String name)
    {
        super(name);
    }

    public void testDefault()
    {
        assertEvalEquals("<foo>", "<${foo|'foo'}>");
        assertEvalEquals("bar", "#set($bar='bar')${foo|$bar}");
        assertEvalEquals("bar", "#set($bar='bar')${foo|${bar}}");
        assertEvalEquals("baz", "${foo|${bar|'baz'}}");
        assertEvalEquals("hop", "${foo.bar.baz()[5]|'hop'}");
        assertEvalEquals("{foo}", "{${foo|'foo'}}");
    }

}
