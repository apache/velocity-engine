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
        assertEvalEquals("bar", "#set($bar='bar')${bar|'foo'}");
        assertEvalEquals("bar", "#set($bar='bar')${foo|${bar}}");
        assertEvalEquals("baz", "${foo|${baz|'baz'}}");
        assertEvalEquals("hop", "${foo.bar.baz()[5]|'hop'}");
        assertEvalEquals("{foo}", "{${foo|'foo'}}");
        assertEvalEquals("<1>", "<${foo|1}>");
        assertEvalEquals("<1.1>", "<${foo|1.1}>");
    }

    public void testComplexEval()
    {
        assertEvalEquals("<no date tool>", "<${date.format('medium', $date.date)|'no date tool'}>");
        assertEvalEquals("true", "#set($val=false)${val.toString().replace(\"false\", \"true\")|'so what'}");
        assertEvalEquals("so what", "#set($foo='foo')${foo.contains('bar')|'so what'}");
        assertEvalEquals("so what", "#set($val=false)${val.toString().contains('bar')|'so what'}");
        assertEvalEquals("true", "#set($val=false)${val.toString().contains('false')|'so what'}");
        assertEvalEquals("", "$!{null|$null}");
        assertEvalEquals("null", "$!{null|'null'}");
        assertEvalEquals("so what", "#set($spaces='   ')${spaces.trim()|'so what'}");
    }
}
