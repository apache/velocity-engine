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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogChute;
import org.apache.velocity.test.provider.ForeachMethodCallHelper;

/**
 * This class tests the Foreach loop.
 *
 * @author Daniel Rall
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 */
public class ForeachTestCase extends TestCase
{
    private VelocityContext context;

    public ForeachTestCase(String name)
    {
        super(name);
    }

    public void setUp()
        throws Exception
    {
        // Limit the loop to three iterations.
        Velocity.setProperty(RuntimeConstants.MAX_NUMBER_LOOPS,
                             new Integer(3));

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, TestLogChute.class.getName());

        Velocity.init();

        context = new VelocityContext();
    }

    /**
     * Tests limiting of the number of loop iterations.
     */
    public void testMaxNbrLoopsConstraint()
        throws Exception
    {
        StringWriter writer = new StringWriter();
        String template = "#foreach ($item in [1..10])$item #end";
        Velocity.evaluate(context, writer, "test", template);
        assertEquals("Max number loops not enforced",
                     "1 2 3 ", writer.toString());
    }

    /**
     * Tests proper method execution during a Foreach loop over a Collection
     * with items of varying classes.
     */
    public void testCollectionAndMethodCall()
        throws Exception
    {
        List col = new ArrayList();
        col.add(new Integer(100));
        col.add("STRVALUE");
        context.put("helper", new ForeachMethodCallHelper());
        context.put("col", col);

        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "test",
                          "#foreach ( $item in $col )$helper.getFoo($item) " +
                          "#end");
        assertEquals("Method calls while looping over varying classes failed",
                     "int 100 str STRVALUE ", writer.toString());
    }

    /**
     * Tests that #foreach will be able to retrieve an iterator from
     * an arbitrary object that happens to have an iterator() method.
     * (With the side effect of supporting the new Java 5 Iterable interface)
     */
    public void testObjectWithIteratorMethod()
        throws Exception
    {
        context.put("iterable", new MyIterable());

        StringWriter writer = new StringWriter();
        String template = "#foreach ($i in $iterable)$i #end";
        Velocity.evaluate(context, writer, "test", template);
        assertEquals("Failed to call iterator() method",
                     "1 2 3 ", writer.toString());
    }

    public void testNotReallyIterableIteratorMethod()
        throws Exception
    {
        context.put("nri", new NotReallyIterable());

        StringWriter writer = new StringWriter();
        String template = "#foreach ($i in $nri)$i #end";
        Velocity.evaluate(context, writer, "test", template);
        assertEquals("", writer.toString());
    }


    public static class MyIterable
    {
        private List foo;

        public MyIterable()
        {
            foo = new ArrayList();
            foo.add(new Integer(1));
            foo.add(new Long(2));
            foo.add("3");
        }
        
        public Iterator iterator()
        {
            return foo.iterator();
        }
    }

    public static class NotReallyIterable
    {
        public Object iterator()
        {
            return new Object();
        }
    }

}
