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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.ContextAware;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;

/**
 * Tests event handling for all event handlers except IncludeEventHandler.  This is tested
 * separately due to its complexity.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class EventHandlingTestCase extends BaseTestCase
{
    private static String NO_REFERENCE_VALUE =  "<no reference value>";
    private static String REFERENCE_VALUE =  "<reference value>";

    public EventHandlingTestCase(String name)
    {
        super(name);
    }

    public void testManualEventHandlers()
            throws Exception
    {
        TestEventCartridge te = new TestEventCartridge();
        /**
         * Test attaching the event cartridge to the context.
         *  Make an event cartridge, register all the
         *  event handlers (at once) and attach it to the
         *  Context
         */

        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(te);
        ec.attachToContext(context);

        /*
         *  now wrap the event cartridge - we want to make sure that
         *  we can do this w/o harm
         */
        doTestReferenceInsertionEventHandler1();
        doTestReferenceInsertionEventHandler2();
        doTestMethodExceptionEventHandler1();
        doTestMethodExceptionEventHandler2();
    }

    /**
     * Test assigning the event handlers via properties
     */
    public void testConfigurationEventHandlers()
            throws Exception
    {
        engine.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, TestEventCartridge.class.getName());
        engine.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, TestEventCartridge.class.getName());

        doTestReferenceInsertionEventHandler1();
        doTestReferenceInsertionEventHandler2();
        doTestMethodExceptionEventHandler1();
        doTestMethodExceptionEventHandler2();
    }

    /**
     * Test all the event handlers using the given engine.
     */
    private void doTestReferenceInsertionEventHandler1()
            throws Exception
    {
        VelocityContext outer = context;
        context = new VelocityContext(context);
        context.put("name", "Velocity");

        /*
         *  First, the reference insertion handler
         */
        String expected = REFERENCE_VALUE + REFERENCE_VALUE + REFERENCE_VALUE;
        assertEvalEquals(expected, "$name$name$name");

        context = outer;
    }

    private void doTestReferenceInsertionEventHandler2()
            throws Exception
    {
        VelocityContext outer = context;
        context = new VelocityContext(context);
        context.put("name", "Velocity");

        /*
         *  using the same handler, we can deal with
         *  null references as well
         */
        assertEvalEquals(NO_REFERENCE_VALUE, "$floobie");

        context = outer;
    }

    private void doTestMethodExceptionEventHandler1()
            throws Exception
    {
        VelocityContext outer = context;
        context = new VelocityContext(context);

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

        evaluate(" $this.throwException()");

        context = outer;
    }

    private void doTestMethodExceptionEventHandler2()
            throws Exception
    {
        VelocityContext outer = context;
        context = new VelocityContext(context);
        context.put("this", this );

        /*
         *  now, we remove the exception flag, and we can see that the
         *  exception will propgate all the way up here, and
         *  wil be caught by the catch() block below
         */
        assertEvalException("$this.throwException()", MethodInvocationException.class);

        context = outer;
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



    public static class TestEventCartridge
            implements ReferenceInsertionEventHandler,
                       MethodExceptionEventHandler,
                       RuntimeServicesAware,ContextAware
    {
        private RuntimeServices rs;

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
        public Object referenceInsert( Context context, String reference, Object value  )
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
         *  Handles exceptions thrown during in-template method access
         */
        public Object methodException( Context context, Class claz, String method, Exception e, Info info )
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
                    throw new RuntimeException(e);

            } else

                throw new RuntimeException(e);
        }

        Context context;


        public void setContext(Context context)
        {
            this.context = context;
        }
    }
}
