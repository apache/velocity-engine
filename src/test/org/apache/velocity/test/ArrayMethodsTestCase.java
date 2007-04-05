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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.SystemLogChute;

/**
 * Used to check that method calls on Array references work properly
 * and that they produce the same results as the same methods would on
 * a fixed-size {@link List}.
 */
public class ArrayMethodsTestCase extends TestCase
{
    private VelocityEngine engine;
    private VelocityContext context;

    private final static boolean PRINT_RESULTS = true;

    public ArrayMethodsTestCase(final String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        engine = new VelocityEngine();

        // make the engine's log output go to the test-report
        SystemLogChute log = new SystemLogChute();
        log.setEnabledLevel(SystemLogChute.INFO_ID);
        log.setSystemErrLevel(SystemLogChute.WARN_ID);
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, log);

        context = new VelocityContext();
    }

    public void tearDown()
    {
        engine = null;
        context = null;
    }

    public static Test suite ()
    {
        return new TestSuite(ArrayMethodsTestCase.class);
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

        if (PRINT_RESULTS)
        {
            System.out.println("Changing to an array of: " + array.getClass().getComponentType());
            System.out.println("Changing setme to: " + setme);
        }

        int size = Array.getLength(array);
        checkResult("size()", String.valueOf(size), compareToList);

        boolean isEmpty = (size == 0);
        checkResult("isEmpty()", String.valueOf(isEmpty), compareToList);

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

        if (PRINT_RESULTS)
        {
            System.out.println("    <$!array."+method+"> resolved to <"+result+">");
            if (compareToList)
            {
                System.out.println("    <$!list."+method+"> resolved to "+listResult+">");
            }
        }
    }

    private String evaluate(String template) throws Exception
    {
        StringWriter writer = new StringWriter();
        // use template as its own name, since our templates are short
        engine.evaluate(context, writer, template, template);
        return writer.toString();
    }

}


