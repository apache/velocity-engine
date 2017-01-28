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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.test.misc.TestLogger;

import java.io.StringWriter;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class MethodInvocationExceptionTestCase extends TestCase
{
    protected boolean DEBUG = false;

    /**
     * Default constructor.
     * @param name
     */
    public MethodInvocationExceptionTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        /*
         *  init() Runtime with defaults
         */

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.init();
    }

    public static Test suite ()
    {
        return new TestSuite(MethodInvocationExceptionTestCase.class);
    }

    protected void log(String out)
    {
        Velocity.getLog().debug(out);
        if (DEBUG)
        {
            System.out.println(out);
        }
    }

    /**
     * Runs the test :
     *
     *  uses the Velocity class to eval a string
     *  which accesses a method that throws an
     *  exception.
     *  @throws Exception
     */
    public void testNormalMethodInvocationException ()
            throws Exception
    {
        String template = "$woogie.doException() boing!";

        VelocityContext vc = new VelocityContext();

        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        try
        {
            Velocity.evaluate( vc,  w, "test", template );
            fail("No exception thrown");
        }
        catch( MethodInvocationException mie )
        {
            log("Caught MIE (good!) :" );
            log("  reference = " + mie.getReferenceName() );
            log("  method    = " + mie.getMethodName() );

            Throwable t = mie.getCause();
            log("  throwable = " + t );

            if( t instanceof Exception)
            {
                log("  exception = " + t.getMessage() );
            }
        }
    }


    public void testGetterMethodInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        /*
         *  second test - to ensure that methods accessed via get+ construction
         *  also work
         */

        String template = "$woogie.foo boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, second test.");
        }
        catch( MethodInvocationException mie )
        {
            log("Caught MIE (good!) :" );
            log("  reference = " + mie.getReferenceName() );
            log("  method    = " + mie.getMethodName() );

            Throwable t = mie.getCause();
            log("  throwable = " + t );

            if( t instanceof Exception)
            {
                log("  exception = " + t.getMessage() );
            }
        }
    }


    public void testCapitalizedGetterMethodInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        String template = "$woogie.Foo boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, third test.");
        }
        catch( MethodInvocationException mie )
        {
            log("Caught MIE (good!) :" );
            log("  reference = " + mie.getReferenceName() );
            log("  method    = " + mie.getMethodName() );

            Throwable t = mie.getCause();
            log("  throwable = " + t );

            if( t instanceof Exception)
            {
                log("  exception = " + t.getMessage() );
            }
        }
    }

    public void testSetterMethodInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        String template = "#set($woogie.foo = 'lala') boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, set test.");
        }
        catch( MethodInvocationException mie )
        {
            log("Caught MIE (good!) :" );
            log("  reference = " + mie.getReferenceName() );
            log("  method    = " + mie.getMethodName() );

            Throwable t = mie.getCause();
            log("  throwable = " + t );

            if( t instanceof Exception)
            {
                log("  exception = " + t.getMessage() );
            }
        }
    }


    /**
     * test that no exception is thrown when in parameter to macro.
     * This is the way we expect the system to work, but it would be better
     * to throw an exception.
     * @throws Exception
     */
    public void testMacroInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        String template = "#macro (macro1 $param) $param #end  #macro1($woogie.getFoo())";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, macro invocation test.");
        }
        catch( MethodInvocationException mie )
        {
            log("Caught MIE (good!) :" );
            log("  reference = " + mie.getReferenceName() );
            log("  method    = " + mie.getMethodName() );

            Throwable t = mie.getCause();
            log("  throwable = " + t );

            if( t instanceof Exception)
            {
                log("  exception = " + t.getMessage() );
            }
        }
        catch( Exception e)
        {
            fail("Wrong exception thrown, test of exception within macro parameter");
        }
    }

    public void doException()
        throws Exception
    {
        throw new NullPointerException();
    }

    public void getFoo()
        throws Exception
    {
        throw new Exception("Hello from getFoo()" );
    }

    public void  setFoo( String foo )
        throws Exception
    {
        throw new Exception("Hello from setFoo()");
    }
}
