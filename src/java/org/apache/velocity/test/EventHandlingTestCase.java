package org.apache.velocity.test;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.log.LogSystem;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;

import org.apache.velocity.runtime.RuntimeServices;

/**
 *  Tests event handling
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: EventHandlingTestCase.java,v 1.5.10.1 2004/03/03 23:23:04 geirm Exp $
 */
public class EventHandlingTestCase extends TestCase implements ReferenceInsertionEventHandler, 
                                     NullSetEventHandler, MethodExceptionEventHandler,
                                     LogSystem
{
   
    private String logString = null;
    private boolean exceptionSwitch = true;
    private static String NO_REFERENCE_VALUE =  "<no reference value>";
    private static String REFERENCE_VALUE =  "<reference value>";

    /**
     * Default constructor.
     */
    public EventHandlingTestCase()
    {
        super("EventHandlingTestCase");

        try
        {
            /*
             *  use an alternative logger.  Set it up here and pass it in.
             */
            
            Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this );
            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup event handling test : " + e);
            System.exit(1);
        }            
    }

    public void init( RuntimeServices rs )
    {
        /* don't need it...*/
    }

    public static junit.framework.Test suite ()
    {
        return new EventHandlingTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {

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
        ec.addEventHandler(this);
        ec.attachToContext( inner );
  
        /*
         *  now wrap the event cartridge - we want to make sure that
         *  we can do this w/o harm
         */

        VelocityContext context = new VelocityContext( inner );

        context.put("name", "Velocity");

        try
        {
            /*
             *  First, the reference insertion handler
             */

            String s = "$name";
            
            StringWriter w = new StringWriter();
            Velocity.evaluate( context, w, "mystring", s );
            
            if ( !w.toString().equals( REFERENCE_VALUE ))
            {
                fail( "Reference insertion test 1");
            }

            /*
             *  using the same handler, we can deal with 
             *  null references as well
             */

            s = "$floobie";

            w = new StringWriter();
            Velocity.evaluate( context, w, "mystring", s );

            if ( !w.toString().equals( NO_REFERENCE_VALUE ))
            {
                fail( "Reference insertion test 2");
            }

            /*
             *  now lets test setting a null value - this test
             *  should result in *no* log output.
             */
                 
            s = "#set($settest = $NotAReference)";
            w = new StringWriter();
            logString = null;
            Velocity.evaluate( context, w, "mystring", s );
            
            if( logString != null)
            {
                fail( "NullSetEventHandler test 1");
            }
            
            /*
             *  now lets test setting a null value - this test
             *  should result in log output.
             */

            s = "#set($logthis = $NotAReference)";
            w = new StringWriter();
            logString = null;           
            Velocity.evaluate( context, w, "mystring", s );
           
            if( logString == null)
            {
                fail( "NullSetEventHandler test 1");
            }

            /*
             *  finally, we test a method exception event - we do this 
             *  by putting this class in the context, and calling 
             *  a method that does nothing but throw an exception.
             *  we use a little switch to turn the event handling
             *  on and off
             *
             *  Note also how the reference insertion process
             *  happens as well
             */
            
            exceptionSwitch = true;

            context.put("this", this );

            s = " $this.throwException()";
            w = new StringWriter();
            
            try
            {
                Velocity.evaluate( context, w, "mystring", s );
            }
            catch( MethodInvocationException mee )
            {
                fail("MethodExceptionEvent test 1");
            }
            catch( Exception e )
            {
                fail("MethodExceptionEvent test 1");
            }

            /*
             *  now, we turn the switch off, and we can see that the 
             *  exception will propgate all the way up here, and 
             *  wil be caught by the catch() block below
             */

            exceptionSwitch = false;

            s = " $this.throwException()";
            w = new StringWriter();

            try
            {
                Velocity.evaluate( context, w, "mystring", s );
                fail("MethodExceptionEvent test 2");
            }
            catch( MethodInvocationException mee )
            {
                /*
                 * correct - should land here...
                 */
            }
            catch( Exception e )
            {
                fail("MethodExceptionEvent test 2");
            }
        }
        catch( ParseErrorException pee )
        {
            fail("ParseErrorException" + pee);
        }
        catch( MethodInvocationException mee )
        {
            fail("MethodInvocationException" + mee);
        }
        catch( Exception e )
        {
            fail("Exception" + e);
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
     *  Event handler for when a reference is inserted into the output stream.
     */
    public Object referenceInsert( String reference, Object value  )
    {
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
        /*
         *  only do processing if the switch is on
         */

        if( exceptionSwitch && method.equals("throwException"))
        {
            return "handler";
        }

        throw e;
    } 

    /**
     *  handler for LogSystem interface
     */
    public void logVelocityMessage(int level, String message)
    {
        logString = message;
    }

}
