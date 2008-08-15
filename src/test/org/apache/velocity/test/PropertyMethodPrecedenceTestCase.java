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
public class PropertyMethodPrecedenceTestCase extends TestCase
{
    private VelocityEngine engine;
    private VelocityContext context;

    public PropertyMethodPrecedenceTestCase(final String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(PropertyMethodPrecedenceTestCase.class);
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
        context.put("geta", new getGetgetisTool());
        context.put("getA", new GetgetisTool());
        context.put("get_a", new getisTool());
        context.put("isA", new isTool());
    }

    public void tearDown()
    {
        engine = null;
        context = null;
    }

    public void testPropertyMethods()
    {
        assertEvalEquals("getfoo", "$geta.foo");
        assertEvalEquals("getFoo", "$getA.foo");
        assertEvalEquals("get(foo)", "$get_a.foo");
        assertEvalEquals("true", "$isA.foo");
    }


    protected void assertEvalEquals(String expected, String template)
    {
        try
        {
            String result = evaluate(template);
System.out.println("expected "+expected+" and got "+result);
            assertEquals(expected, result);
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


    public static class isTool
    {
        public boolean isFoo()
        {
            return true;
        }
    }

    public static class getisTool extends isTool
    {
        public String get(String s)
        {
            return "get(foo)";
        }
    }

    public static class GetgetisTool extends getisTool
    {
        public String getFoo()
        {
            return "getFoo";
        }
    }

    public static class getGetgetisTool extends GetgetisTool
    {
        public String getfoo()
        {
            return "getfoo";
        }
    }

}


