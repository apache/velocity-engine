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
import org.apache.velocity.test.misc.TestLogChute;

/**
 * Base for test cases that use evaluate, instead of going
 * through the resource loaders.
 */
public class BaseEvalTestCase extends TestCase
{
    protected VelocityEngine engine;
    protected VelocityContext context;
    protected boolean DEBUG = false;
    protected TestLogChute log;

    public BaseEvalTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        engine = new VelocityEngine();

        //by default, make the engine's log output go to the test-report
        log = new TestLogChute(false, false);
        log.setEnabledLevel(TestLogChute.INFO_ID);
        log.setSystemErrLevel(TestLogChute.WARN_ID);
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

    protected void assertContextValue(String key, Object expected)
    {
        if (DEBUG)
        {
            engine.getLog().info("Expected value of '"+key+"': "+expected);
        }
        Object value = context.get(key);
        if (DEBUG)
        {
            engine.getLog().info("Result: "+value);
        }
        assertEquals(expected, value);
    }

    protected void assertEvalEquals(String expected, String template)
    {
        if (DEBUG)
        {
            engine.getLog().info("Expectation: "+expected);
        }
        assertEquals(expected, evaluate(template));
    }

    protected Exception assertEvalException(String evil)
    {
        return assertEvalException(evil, null);
    }

    protected Exception assertEvalException(String evil, Class exceptionType)
    {
        try
        {
            if (!DEBUG)
            {
                log.off();
            }
            evaluate(evil);
            fail("Template '"+evil+"' should have thrown an exception.");
        }
        catch (Exception e)
        {
            if (exceptionType != null && !exceptionType.isAssignableFrom(e.getClass()))
            {
                fail("Was expecting template '"+evil+"' to throw "+exceptionType+" not "+e);
            }
            return e;
        }
        finally
        {
            if (!DEBUG)
            {
                log.on();
            }
        }
        return null;
    }

    protected Exception assertEvalExceptionAt(String evil, String template,
                                              int line, int col)
    {
        String loc = template+"[line "+line+", column "+col+"]";
        if (DEBUG)
        {
            engine.getLog().info("Expectation: Exception at "+loc);
        }
        Exception e = assertEvalException(evil, null);
        if (e.getMessage().indexOf(loc) < 1)
        {
            fail("Was expecting exception at "+loc+" instead of "+e.getMessage());
        }
        else if (DEBUG)
        {
            engine.getLog().info("Result: "+e.getMessage());
        }
        return e;
    }

    protected Exception assertEvalExceptionAt(String evil, int line, int col)
    {
         return assertEvalExceptionAt(evil, "", line, col);
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
            if (DEBUG)
            {
                engine.getLog().info("RuntimeException!", re);
            }
            throw re;
        }
        catch (Exception e)
        {
            if (DEBUG)
            {
                engine.getLog().info("Exception!", e);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Compare an expected string with the given loaded template
     */
    protected void assertTmplEquals(String expected, String template)
    {        
        if (DEBUG)
        {
            engine.getLog().info("Expected:  '" + expected + "'");
        }

        StringWriter writer = new StringWriter();
        try
        {          
            engine.mergeTemplate(template, "utf-8", context, writer);
        }
        catch (RuntimeException re)
        {
            if (DEBUG)
            {
                engine.getLog().info("RuntimeException!", re);
            }
            throw re;
        }
        catch (Exception e)
        {
            if (DEBUG)
            {
                engine.getLog().info("Exception!", e);
            }
            throw new RuntimeException(e);
        }        

        if (DEBUG)
        {
            engine.getLog().info("Result:  '" + writer.toString() + "'");
        }
        assertEquals(expected, writer.toString());  
    }
}
