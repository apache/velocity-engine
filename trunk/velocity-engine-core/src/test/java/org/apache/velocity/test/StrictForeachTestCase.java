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

import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class tests support for strict foreach mode.
 */
public class StrictForeachTestCase extends BaseTestCase
{
    public StrictForeachTestCase(String name)
    {
       super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.SKIP_INVALID_ITERATOR, Boolean.FALSE);
        context.put("good", new GoodIterable());
        context.put("bad", new BadIterable());
        context.put("ugly", new UglyIterable());
    }

    public void testGood()
    {
        try
        {
            evaluate("#foreach( $i in $good )$i#end");
        }
        catch (VelocityException ve)
        {
            fail("Doing #foreach on $good should not have exploded!");
        }
    }

    public void testBad()
    {
        try
        {
            evaluate("#foreach( $i in $bad )$i#end");
            fail("Doing #foreach on $bad should have exploded!");
        }
        catch (VelocityException ve)
        {
            // success!
        }
    }

    public void testUgly()
    {
        try
        {
            evaluate("#foreach( $i in $ugly )$i#end");
            fail("Doing #foreach on $ugly should have exploded!");
        }
        catch (VelocityException ve)
        {
            // success!
        }
    }


    public static class GoodIterable
    {
        public Iterator iterator()
        {
            return new ArrayList().iterator();
        }
    }

    public static class BadIterable
    {
        public Object iterator()
        {
            return new Object();
        }
    }

    public static class UglyIterable
    {
        public Iterator iterator()
        {
            return null;
        }
    }
}
