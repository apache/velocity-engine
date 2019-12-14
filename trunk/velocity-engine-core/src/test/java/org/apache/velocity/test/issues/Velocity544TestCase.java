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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.velocity.test.BaseTestCase;

/**
 * @see https://issues.apache.org/jira/browse/VELOCITY-544
 */
public class Velocity544TestCase
        extends BaseTestCase
{
    public Velocity544TestCase(final String name)
            throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(Velocity544TestCase.class);
    }

    public void testBooleanPropertyExecutor()
        throws Exception
    {
        context.put("foobarTrue", new Foobar(true));
        context.put("foobarFalse", new Foobar(false));

        String template = "$foobarTrue.True $foobarFalse.True $foobarTrue.TrueObject $foobarFalse.TrueObject";

        String result = evaluate(template);

        assertEquals("true false true false", result);
    }

    public static class Foobar
    {
        private boolean value;

        public Foobar(boolean value)
        {
            this.value = value;
        }

        public boolean isTrue()
        {
            return(value);
        }

        public Boolean isTrueObject()
        {
            return(value);
        }
    }
}
