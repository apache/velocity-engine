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

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.provider.ForeachMethodCallHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class tests the Foreach loop.
 *
 * @author Daniel Rall
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 */
public class ForeachTestCase extends BaseTestCase
{
    public ForeachTestCase(String name)
    {
        super(name);
    }

    /**
     * Tests limiting of the number of loop iterations.
     */
    public void testMaxNbrLoopsConstraint()
        throws Exception
    {
        // Limit the loop to three iterations.
        engine.setProperty(RuntimeConstants.MAX_NUMBER_LOOPS,
            3);

        assertEvalEquals("1 2 3 ", "#foreach ($item in [1..10])$item #end");
    }

    /**
     * Tests proper method execution during a Foreach loop over a Collection
     * with items of varying classes.
     */
    public void testCollectionAndMethodCall()
        throws Exception
    {
        List col = new ArrayList();
        col.add(100);
        col.add("STRVALUE");
        context.put("helper", new ForeachMethodCallHelper());
        context.put("col", col);

        assertEvalEquals("int 100 str STRVALUE ", "#foreach ( $item in $col )$helper.getFoo($item) #end");
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

        assertEvalEquals("1 2 3 ", "#foreach ($i in $iterable)$i #end");
    }

    public void testNotReallyIterableIteratorMethod()
        throws Exception
    {
        context.put("nri", new NotReallyIterable());

        assertEvalEquals("", "#foreach ($i in $nri)$i #end");
    }

    public void testVelocityHasNextProperty()
        throws Exception
    {
        List list = new ArrayList();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        context.put("list", list);
        assertEvalEquals("test1 SEPARATOR test2 SEPARATOR test3 ", "#foreach ($value in $list)$value #if( $foreach.hasNext )SEPARATOR #end#end");
    }

    public void testNestedVelocityHasNextProperty()
        throws Exception
    {
        List list = new ArrayList();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        list.add("test4");
        context.put("list", list);
        List list2 = new ArrayList();
        list2.add("a1");
        list2.add("a2");
        list2.add("a3");
        context.put("list2", list2);

        assertEvalEquals("test1 (a1;a2;a3)-test2 (a1;a2;a3)-test3 (a1;a2;a3)-test4 (a1;a2;a3)", "#foreach ($value in $list)$value (#foreach ($val in $list2)$val#if( $foreach.hasNext );#end#end)#if( $foreach.hasNext )-#end#end");
    }

    public static class MyIterable
    {
        private List foo;

        public MyIterable()
        {
            foo = new ArrayList();
            foo.add(1);
            foo.add(2L);
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
