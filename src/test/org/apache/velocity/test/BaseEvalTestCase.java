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
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.SystemLogChute;

/**
 * Base for test cases that use evaluate, instead of going
 * through the resource loaders.
 */
public class BaseEvalTestCase extends TestCase
{
    protected VelocityEngine engine;
    protected VelocityContext context;
    protected boolean DEBUG = false;

    public BaseEvalTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        engine = new VelocityEngine();

        //by default, make the engine's log output go to the test-report
        SystemLogChute log = new SystemLogChute();
        log.setEnabledLevel(SystemLogChute.INFO_ID);
        log.setSystemErrLevel(SystemLogChute.WARN_ID);
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, log);

        context = new VelocityContext();
        setContext(context);
    }

    public void tearDown()
    {
        engine = null;
        context = null;
    }

    public void testBase()
    {
        assertEvalEquals("","");
        assertEvalEquals("abc\n123","abc\n123");
    }

    protected void setProperties(VelocityEngine engine)
    {
        // extension hook
    }

    protected void setContext(VelocityContext context)
    {
        // extension hook
    }

    protected void assertEvalEquals(String expected, String template)
    {
        assertEquals(expected, evaluate(template));
    }

    protected String evaluate(String template)
    {
        StringWriter writer = new StringWriter();
        try
        {
            if (DEBUG)
            {
                engine.getLog().info("Template: "+template);
            }

            // use template as its own name, since our templates are short
            engine.evaluate(context, writer, template, template);

            String result = writer.toString();
            if (DEBUG)
            {
                engine.getLog().info("Result: "+result);
            }
            return result;
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
