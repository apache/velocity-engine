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
 * Used to check that vararg method calls on references work properly
 */
public class VarargMethodsTestCase extends TestCase
{
    private VelocityEngine engine;
    private VelocityContext context;

    public VarargMethodsTestCase(final String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(VarargMethodsTestCase.class);
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
        context.put("nice", new NiceTool());
        context.put("nasty", new NastyTool());
        context.put("objects", new Object[] { this, Test.class });
        context.put("strings", new String[] { "one", "two" });
        context.put("doubles", new double[] { 1.5, 2.5 });
        context.put("float", Float.valueOf(1f));
        context.put("ints", new int[] { 1, 2 });
    }

    public void tearDown()
    {
        engine = null;
        context = null;
    }

    public void testStrings()
    {
        assertEvalEquals("onetwo", "$nice.var($strings)");
        assertEvalEquals("onetwo", "$nice.var('one','two')");
        assertEvalEquals("one", "$nice.var('one')");
        assertEvalEquals("", "$nice.var()");
    }

    public void testDoubles()
    {
        assertEvalEquals("4.0", "$nice.add($doubles)");
        assertEvalEquals("3.0", "$nice.add(1,2)");
        assertEvalEquals("1.0", "$nice.add(1)");
        assertEvalEquals("0.0", "$nice.add()");
    }

    public void testFloatToDoubleVarArg()
    {
        assertEvalEquals("1.0", "$nice.add($float)");
    }

    public void testStringVsStrings()
    {
        assertEvalEquals("onlyone", "$nasty.var('one')");
        assertEvalEquals("onlynull", "$nasty.var($null)");
        assertEvalEquals("", "$nasty.var()");
    }

    public void testIntVsDoubles()
    {
        assertEvalEquals("1", "$nasty.add(1)");
        assertEvalEquals("1.0", "$nasty.add(1.0)");
        assertEvalEquals("3.0", "$nasty.add(1.0,2)");
    }

    public void testInts()
    {
        assertEvalEquals("3", "$nasty.add($ints)");
        assertEvalEquals("3", "$nasty.add(1,2)");
        assertEvalEquals("1", "$nasty.add(1)");
        // add(int[]) wins because it is "more specific"
        assertEvalEquals("0", "$nasty.add()");
    }

    public void testStringsVsObjectsAKASubclassVararg()
    {
        assertEvalEquals("objects", "$nice.test($objects)");
        assertEvalEquals("objects", "$nice.test($nice,$nasty,$ints)");
        assertEvalEquals("strings", "$nice.test('foo')");
    }

    public void testObjectVarArgVsObjectEtc()
    {
        assertEvalEquals("object,string", "$nasty.test($nice,'foo')");
    }


    protected void assertEvalEquals(String expected, String template)
    {
        try
        {
            assertEquals(expected, evaluate(template));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String evaluate(String template) throws Exception
    {
        StringWriter writer = new StringWriter();
        // use template as its own name, since our templates are short
        engine.evaluate(context, writer, template, template);
        return writer.toString();
    }



    public static class NiceTool
    {
        public String var(String[] ss)
        {
            StringBuffer out = new StringBuffer();
            for (int i=0; i < ss.length; i++)
            {
                out.append(ss[i]);
            }
            return out.toString();
        }

        public double add(double[] dd)
        {
            double total = 0;
            for (int i=0; i < dd.length; i++)
            {
                total += dd[i];
            }
            return total;
        }

        public String test(Object[] oo)
        {
            return "objects";
        }

        public String test(String[] oo)
        {
            return "strings";
        }

    }

    public static class NastyTool extends NiceTool
    {
        public String var(String s)
        {
            return "only"+s;
        }

        public int add(int[] ii)
        {
            int total = 0;
            for (int i=0; i < ii.length; i++)
            {
                total += ii[i];
            }
            return total;
        }

        public int add(int i)
        {
            return i;
        }

        public String test(Object[] array)
        {
            return "object[]";
        }

        public String test(Object object, String property)
        {
            return "object,string";
        }

    }

}


