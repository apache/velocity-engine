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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used to check that method calls on Array references work properly
 * and that they produce the same results as the same methods would on
 * a fixed-size {@link List}.
 */
public class ArrayMethodsTestCase extends BaseTestCase
{
    public ArrayMethodsTestCase(final String name)
    {
        super(name);
    }

    /**
     * Runs the test.
     */
    public void testArrayMethods() throws Exception
    {
        // test an array of string objects
        Object array = new String[] { "foo", "bar", "baz" };
        checkResults(array, "woogie", true);

        // test an array of primitive ints
        array = new int[] { 1, 3, 7 };
        checkResults(array, new Integer(11), false);

        // test an array of mixed objects, including null
        array = new Object[] { new Double(2.2), null };
        checkResults(array, "whatever", true);
        // then set all the values to null
        checkResults(array, null, true);

        // then try an empty array
        array = new Object[] {};
        checkResults(array, null, true);

        // while we have an empty array and list in the context,
        // make sure $array.get(0) and $list.get(0) throw
        // the same type of exception (MethodInvocationException)
        Throwable lt = null;
        Throwable at = null;
        try
        {
            evaluate("$list.get(0)");
        }
        catch (Throwable t)
        {
            lt = t;
        }
        try
        {
            evaluate("$array.get(0)");
        }
        catch (Throwable t)
        {
            at = t;
        }
        assertEquals(lt.getClass(), at.getClass());
    }

    private void checkResults(Object array, Object setme,
                              boolean compareToList) throws Exception
    {
        context.put("array", array);
        if (compareToList)
        {
            // create a list to match...
            context.put("list", new ArrayList(Arrays.asList((Object[])array)));
        }

        // if the object to be set is null, then remove instead of put
        if (setme != null)
        {
            context.put("setme", setme);
        }
        else
        {
            context.remove("setme");
        }

        info("Changing to an array of: " + array.getClass().getComponentType());
        info("Changing setme to: " + setme);

        int size = Array.getLength(array);
        checkResult("size()", String.valueOf(size), compareToList);

        boolean isEmpty = (size == 0);
        checkResult("isEmpty()", String.valueOf(isEmpty), compareToList);

        checkPropertyResult("empty", String.valueOf(isEmpty), compareToList);

        // check that the wrapping doesn't apply to java.lang.Object methods
        // such as toString() (for backwards compatibility).
        assertFalse(evaluate("$array").equals(evaluate("$list")));

        for (int i=0; i < size; i++)
        {
            // put the index in the context, so we can try
            // both an explicit index and a reference index
            context.put("index", new Integer(i));

            Object value = Array.get(array, i);
            String get = "get($index)";
            String set = "set("+i+", $setme)";
            if (value == null)
            {
                checkEmptyResult(get, compareToList);
                // set should return null
                checkEmptyResult(set, compareToList);
            }
            else
            {
                checkResult(get, value.toString(), compareToList);
                // set should return the old get value
                checkResult(set, value.toString(), compareToList);
            }

            // check that set() actually changed the value
            assertEquals(setme, Array.get(array, i));

            // and check that get() now returns setme
            if (setme == null)
            {
                checkEmptyResult(get, compareToList);
            }
            else
            {
                checkResult(get, setme.toString(), compareToList);

                // now check that contains() properly finds the new value
                checkResult("contains($setme)", "true", compareToList);
            }
        }
    }

    private void checkEmptyResult(String method, boolean compareToList)
        throws Exception
    {
        checkResult(method, "", compareToList);
    }

    private void checkResult(String method, String expected,
                             boolean compareToList) throws Exception
    {
        String result = evaluate("$!array."+method);
        assertEquals(expected, result);

        String listResult = null;
        if (compareToList)
        {
            listResult = evaluate("$!list."+method);
            assertEquals(result, listResult);
        }

        info("    <$!array." + method + "> resolved to <" + result + ">");
        if (compareToList)
        {
            info("    <$!list."+method+"> resolved to "+listResult+">");
        }
    }

    private void checkPropertyResult(String property, String expected,
                             boolean compareToList) throws Exception
    {
        String result = evaluate("$!array."+property);
        assertEquals(expected, result);

        String listResult = null;
        if (compareToList)
        {
            listResult = evaluate("$!list."+property);
            assertEquals(result, listResult);
        }

        info("    <$!array."+property+"> resolved to <"+result+">");
        if (compareToList)
        {
            info("    <$!list."+property+"> resolved to "+listResult+">");
        }
    }

}


