package org.apache.velocity.test;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.ContextAware;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.RuntimeServicesAware;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Tests event handling for all event handlers except IncludeEventHandler.  This is tested
 * separately due to its complexity.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class EventHandlingTestCase
        extends TestCase
        implements LogSystem
{
    private static String NO_REFERENCE_VALUE =  "<no reference value>";
    private static String REFERENCE_VALUE =  "<reference value>";

    private static String logString = null;

    /**
     * Default constructor.
     */
    public EventHandlingTestCase(String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(EventHandlingTestCase.class);
    }

    public void testManualEventHandlers()
            throws Exception
    {
        TestEventCartridge te = new TestEventCartridge();
        /**
         * Test attaching the event cartridge to the context
         */
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this);
        ve.init();

        /*
         *  lets make a Context and add the event cartridge
         */

        VelocityContext inner = new VelocityContext();

        /*
         *  Now make an event cartridge, register all the
         *  event handlers (at once) and attach it to the
         *  Context
         */

        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(te);
        ec.attachToContext( inner );

        /*
         *  now wrap the event cartridge - we want to make sure that
         *  we can do this w/o harm
         */

        doTestReferenceInsertionEventHandler1(ve, inner);
        doTestReferenceInsertionEventHandler2(ve, inner);
        doTestNullValueEventHandler(ve, inner);
        doTestSetNullValueEventHandler(ve, inner);
        doTestMethodExceptionEventHandler1(ve, inner);
        doTestMethodExceptionEventHandler2(ve, inner);
    }

    /**
     * Test assigning the event handlers via properties
     */

    public void testConfigurationEventHandlers()
            throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this);
        ve.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, TestEventCartridge.class.getName());
        ve.setProperty(RuntimeConstants.EVENTHANDLER_NULLSET, TestEventCartridge.class.getName());
        ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, TestEventCartridge.class.getName());

        ve.init();

        doTestReferenceInsertionEventHandler1(ve, null);
        doTestReferenceInsertionEventHandler2(ve, null);
        doTestNullValueEventHandler(ve, null);
        doTestSetNullValueEventHandler(ve, null);
        doTestMethodExceptionEventHandler1(ve, null);
        doTestMethodExceptionEventHandler2(ve, null);
    }

    /**
     * Test all the event handlers using the given engine.
     * @param ve
     * @param vcontext
     */
    private void doTestReferenceInsertionEventHandler1(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        VelocityContext context = new VelocityContext(vc);

        context.put("name", "Velocity");

        /*
         *  First, the reference insertion handler
         */

        String s = "$name";

        StringWriter w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        if ( !w.toString().equals( REFERENCE_VALUE ))
        {
            fail( "Reference insertion test 1");
        }
    }

    private void doTestReferenceInsertionEventHandler2(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        VelocityContext context = new VelocityContext(vc);
        context.put("name", "Velocity");

        /*
         *  using the same handler, we can deal with
         *  null references as well
         */

        String s = "$floobie";

        Writer w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        if ( !w.toString().equals( NO_REFERENCE_VALUE ))
        {
            fail( "Reference insertion test 2");
        }
    }

    private void doTestNullValueEventHandler(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        VelocityContext context = new VelocityContext(vc);

        /*
         *  now lets test setting a null value - this test
         *  should result in *no* log output.
         */

        String s = "#set($settest = $NotAReference)";
        Writer w = new StringWriter();
        clearLogString();
        ve.evaluate( context, w, "mystring", s );

        if( getLogString() != null)
        {
            fail( "NullSetEventHandler test 1");
        }
    }

    private void doTestSetNullValueEventHandler(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        VelocityContext context = new VelocityContext(vc);

        /*
         *  now lets test setting a null value - this test
         *  should result in log output.
         */

        String s = "#set($logthis = $NotAReference)";
        Writer w = new StringWriter();
        clearLogString();
        ve.evaluate( context, w, "mystring", s );

        if( getLogString() == null)
        {
            fail( "NullSetEventHandler test 2");
        }
    }

    private void doTestMethodExceptionEventHandler1(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        VelocityContext context = new VelocityContext(vc);

        /*
         *  finally, we test a method exception event - we do this
         *  by putting this class in the context, and calling
         *  a method that does nothing but throw an exception.
         *  we use flag in the context to turn the event handling
         *  on and off
         *
         *  Note also how the reference insertion process
         *  happens as well
         */

        context.put("allow_exception",Boolean.TRUE);

        context.put("this", this );

        String s = " $this.throwException()";
        Writer w = new StringWriter();

        ve.evaluate( context, w, "mystring", s );
    }


    private void doTestMethodExceptionEventHandler2(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        VelocityContext context = new VelocityContext(vc);
        context.put("this", this );

        /*
         *  now, we remove the exception flag, and we can see that the
         *  exception will propgate all the way up here, and
         *  wil be caught by the catch() block below
         */

        String s = " $this.throwException()";
        Writer w = new StringWriter();

        try
        {
            ve.evaluate( context, w, "mystring", s );
            fail("No MethodExceptionEvent received!");
        }
        catch( MethodInvocationException mee )
        {
            // Do nothing
        }
    }

    /**
     *  silly method to throw an exception to test
     *  the method invocation exception event handling
     */
    public void throwException()
            throws Exception
    {
        throw new Exception("Hello from throwException()");
    }

    /**
     * Required by LogSystem
     */
    public void init( RuntimeServices rs )
    {
        /* don't need it...*/
    }

    /**
     *  handler for LogSystem interface
     */
    public void logVelocityMessage(int level, String message)
    {
        setLogString(message);
    }

    public static void clearLogString()
    {
        logString = null;
    }

    public static void setLogString(String message)
    {
        logString = message;
    }

    public static String getLogString()
    {
        return logString;
    }

    public static class TestEventCartridge
            implements ReferenceInsertionEventHandler,
                       NullSetEventHandler, MethodExceptionEventHandler,
                       RuntimeServicesAware,ContextAware
    {
        private RuntimeServices rs;
        
        public TestEventCartridge()
        {
        }

        /**
         * Required by EventHandler
         */
        public void setRuntimeServices( RuntimeServices rs )
        {
            // make sure this is only called once
            if (this.rs == null)
                this.rs = rs;

            else
                fail("initialize called more than once.");
        }

        /**
         *  Event handler for when a reference is inserted into the output stream.
         */
        public Object referenceInsert( String reference, Object value  )
        {
            // as a test, make sure this EventHandler is initialized
            if (rs == null)
                fail ("Event handler not initialized!");


            /*
             *  if we have a value
             *  return a known value
             */
            String s = null;

            if( value != null )
            {
                s = REFERENCE_VALUE;
            }
            else
            {
                /*
                 * we only want to deal with $floobie - anything
                 *  else we let go
                 */
                if ( reference.equals("$floobie") )
                {
                    s = NO_REFERENCE_VALUE;
                }
            }
            return s;
        }

        /**
         *  Event handler for when the right hand side of
         *  a #set() directive is null, which results in
         *  a log message.  This method gives the application
         *  a chance to 'vote' on msg generation
         */
        public boolean shouldLogOnNullSet( String lhs, String rhs )
        {
            // as a test, make sure this EventHandler is initialized
            if (rs == null)
                fail ("Event handler not initialized!");

            if (lhs.equals("$settest"))
                return false;

            return true;
        }

        /**
         *  Handles exceptions thrown during in-template method access
         */
        public Object methodException( Class claz, String method, Exception e )
                throws Exception
        {
            // as a test, make sure this EventHandler is initialized
            if (rs == null)
                fail ("Event handler not initialized!");

            // only do processing if the switch is on
            if (context != null)
            {
                boolean exceptionSwitch = context.containsKey("allow_exception");

                if( exceptionSwitch && method.equals("throwException"))
                {
                    return "handler";
                }
                else
                    throw e;

            } else

                throw e;
        }

        Context context;


        public void setContext(Context context)
        {
            this.context = context;
        }
    }
}
