/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.RuntimeServices;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;

import org.apache.velocity.context.Context;

/**
 *   This class is a simple demonstration of how the event handling
 *   features of the Velocity Servlet Engine are used.  It uses a
 *   custom logger as well to check the log message stream
 *   when testing the NullSetEventHandler
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: EventExample.java,v 1.3 2001/09/09 07:26:24 geirm Exp $
 */

public class EventExample implements ReferenceInsertionEventHandler, 
                                     NullSetEventHandler, MethodExceptionEventHandler,
                                     LogSystem
{

    private boolean logOutput = false;
    private boolean exceptionSwitch = false;

    public static void main( String args[] )
    {
        EventExample ee = new EventExample();
    }

    public EventExample()
    {
        try
        {
            /*
             *  this class implements the LogSystem interface, so we
             *  can use it as a logger for Velocity
             */
            
            Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this );
            Velocity.init();
        }
        catch(Exception e)
        {
            System.out.println("Problem initializing Velocity : " + e );
            return;
        }

        /* 
         *  lets make a Context and add some data
         */

        VelocityContext context = new VelocityContext();

        context.put("name", "Velocity");

        /*
         *  Now make an event cartridge, register all the 
         *  event handlers (at once) and attach it to the
         *  Context
         */

        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(this);
        ec.attachToContext( context );
  
        try
        {
            /* 
             *  lets test each type of event handler individually
             *  using 'dynamic' templates
             *
             *  First, the reference insertion handler
             */

            System.out.println("");
            System.out.println("Velocity Event Handling Demo");
            System.out.println("============================");
            System.out.println("");

            String s = "The word 'Velocity' should be bounded by emoticons :  $name.";
            
            StringWriter w = new StringWriter();
            Velocity.evaluate( context, w, "mystring", s );

            System.out.println("Reference Insertion Test : ");
            System.out.println("   " +  w.toString());
            System.out.println("");

            /*
             *  using the same handler, we can deal with 
             *  null references as well
             */

            s = "There is no reference $floobie, $nullvalue or anything in the brackets : >$!silentnull<";

            w = new StringWriter();
            Velocity.evaluate( context, w, "mystring", s );

            System.out.println("Reference Insertion Test with null references : ");
            System.out.println("   " + w.toString());
            System.out.println("");

            /*
             *  now lets test setting a null value - this test
             *  should result in *no* log output.
             *  Turn on the logger output.
             */

            logOutput = true;

            s = "#set($settest = $NotAReference)";
            w = new StringWriter();

            System.out.println("NullSetEventHandler test : " );
            System.out.print("      There should be nothing between >");
            Velocity.evaluate( context, w, "mystring", s );
            System.out.println("< the brackets.");
            System.out.println("");

            /*
             *  now lets test setting a null value - this test
             *  should result in log output.
             */

            s = "#set($logthis = $NotAReference)";
            w = new StringWriter();

            System.out.println("NullSetEventHandler test : " );
            System.out.print("     There should be a log message between >");
            Velocity.evaluate( context, w, "mystring", s );
            System.out.println("< the brackets.");
            System.out.println("");

            logOutput = false;

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

            System.out.println("MethodExceptionEventHandler test : " );
            System.out.print("    This exception will be controlled and converted into a string : ");
            Velocity.evaluate( context, w, "mystring", s );
            System.out.println("   " + w.toString());
            System.out.println("");

            /*
             *  now, we turn the switch off, and we can see that the 
             *  exception will propgate all the way up here, and 
             *  wil be caught by the catch() block below
             */

            exceptionSwitch = false;

            s = " $this.throwException()";
            w = new StringWriter();

            System.out.println("MethodExceptionEventHandler test : " );
            System.out.println("    This exception will NOT be controlled. "
                             + " The next thing you should see is the catch() output ");
            Velocity.evaluate( context, w, "mystring", s );
            System.out.println("If you see this, it didn't work!");

        }
        catch( ParseErrorException pee )
        {
            /*
             * thrown if something is wrong with the
             * syntax of our template string
             */
            System.out.println("ParseErrorException : " + pee );
        }
        catch( MethodInvocationException mee )
        {
            /*
             *  thrown if a method of a reference
             *  called by the template
             *  throws an exception. That won't happen here
             *  as we aren't calling any methods in this
             *  example, but we have to catch them anyway
             */
            System.out.println("   Catch Block : MethodInvocationException : " + mee );
        }
        catch( Exception e )
        {
            System.out.println("Exception : " + e );
        }
    }

    /**
     *  silly method to throw an exception to demonstrate
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
         *  lets decorate the reference with emoticons
         */

        String s = null;

        if( value != null )
        {
            s = " ;) " + value.toString() + " :-)";
        }
        else
        {
            /*
             * we only want to deal with $floobie - anything
             *  else we let go
             */
            if ( reference.equals("floobie") )
            {
                s = "<no floobie value>";
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

    public Object methodException( Class claz, String method, Exception e )
         throws Exception
    {
        /*
         *  only do processing if the switch is on
         */

        if( exceptionSwitch && method.equals("throwException"))
        {
            return "Hello from the methodException() event handler method.";
        }

        throw e;
    } 

	/**
	 *  Required init method for LogSystem
	 *  to get access to RuntimeServices
	 */ 
	 public void init( RuntimeServices rs )
	 {
	 	return;
	 }

    /**
     *  This is the key method needed to implement a logging interface
     *  for Velocity.
     */ 
    public void logVelocityMessage(int level, String message)
    {
        if (logOutput)
        {
            System.out.print("Velocity Log : level : " + level + " msg : " + message);
        }
    }

}
