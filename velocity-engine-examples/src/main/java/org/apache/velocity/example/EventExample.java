package org.apache.velocity.example;
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
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.util.introspection.Info;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.io.StringWriter;

/**
 *   This class is a simple demonstration of how the event handling
 *   features of the Velocity Servlet Engine are used.  It uses a
 *   custom logger as well to check the log message stream
 *   when testing the NullSetEventHandler
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */

public class EventExample extends MarkerIgnoringBase
    implements ReferenceInsertionEventHandler, MethodExceptionEventHandler
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
             *  this class implements the Logger interface, so we
             *  can use it as a logger for Velocity
             */

            Velocity.setProperty(Velocity.RUNTIME_LOG_INSTANCE, this );
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
    public Object referenceInsert( Context context, String reference, Object value  )
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

    public Object methodException( Context context, Class claz, String method, Exception e, Info info )   {
        /*
         *  only do processing if the switch is on
         */

        if( exceptionSwitch && method.equals("throwException"))
        {
            return "Hello from the methodException() event handler method.";
        }

        throw new RuntimeException(e);
    }

    /**
     *  Our own logging towards System.out
     */

    /**
     * This just prints the message and level to System.out.
     */
    public void log(int level, String message)
    {
        if (logOutput)
        {
            System.out.println("level : " + level + " msg : " + message);
        }
    }


    /**
     * This prints the level, message, and the Throwable's message to
     * System.out.
     */
    public void log(int level, String message, Throwable t)
    {
        if (logOutput)
        {
            System.out.println("level : " + level + " msg : " + message + " t : "
                    + t.getMessage());
        }
    }

    /**
     * This prints the level and formatted message to
     * System.out.
     */
    public void formatAndLog(int level, String format, Object... arguments)
    {
        if (logOutput)
        {
            FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
            log(level, tp.getMessage());
        }
    }

    /**
     * This always returns true because logging levels can't be disabled in
     * this impl.
     */
    public boolean isLevelEnabled(int level)
    {
        return true;
    }

    public static final int LOG_LEVEL_TRACE = 1;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_WARN = 4;
    public static final int LOG_LEVEL_ERROR = 5;

    /**
     *  Required init methods for Logger interface
     */

    public String getName()
    {
        return "EventExample";
    }

    public boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    public void trace(String msg) {
        log(LOG_LEVEL_TRACE, msg);
    }

    public void trace(String format, Object param1)
    {
        formatAndLog(LOG_LEVEL_TRACE, format, param1);
    }

    public void trace(String format, Object param1, Object param2)
    {
        formatAndLog(LOG_LEVEL_TRACE, format, param1, param2);
    }

    public void trace(String format, Object... argArray)
    {
        formatAndLog(LOG_LEVEL_TRACE, format, argArray);
    }

    public void trace(String msg, Throwable t)
    {
        log(LOG_LEVEL_TRACE, msg, t);
    }

    public boolean isDebugEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    public void debug(String msg)
    {
        log(LOG_LEVEL_DEBUG, msg);
    }

    public void debug(String format, Object param1)
    {
        formatAndLog(LOG_LEVEL_DEBUG, format, param1);
    }

    public void debug(String format, Object param1, Object param2)
    {
        formatAndLog(LOG_LEVEL_DEBUG, format, param1, param2);
    }

    public void debug(String format, Object... argArray)
    {
        formatAndLog(LOG_LEVEL_DEBUG, format, argArray);
    }

    public void debug(String msg, Throwable t)
    {
        log(LOG_LEVEL_DEBUG, msg, t);
    }

    public boolean isInfoEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    public void info(String msg)
    {
        log(LOG_LEVEL_INFO, msg);
    }

    public void info(String format, Object arg)
    {
        formatAndLog(LOG_LEVEL_INFO, format, arg);
    }

    public void info(String format, Object arg1, Object arg2)
    {
        formatAndLog(LOG_LEVEL_INFO, format, arg1, arg2);
    }

    public void info(String format, Object... argArray)
    {
        formatAndLog(LOG_LEVEL_INFO, format, argArray);
    }

    public void info(String msg, Throwable t)
    {
        log(LOG_LEVEL_INFO, msg, t);
    }

    public boolean isWarnEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    public void warn(String msg)
    {
        log(LOG_LEVEL_WARN, msg);
    }

    public void warn(String format, Object arg)
    {
        formatAndLog(LOG_LEVEL_WARN, format, arg);
    }

    public void warn(String format, Object arg1, Object arg2)
    {
        formatAndLog(LOG_LEVEL_WARN, format, arg1, arg2);
    }

    public void warn(String format, Object... argArray)
    {
        formatAndLog(LOG_LEVEL_WARN, format, argArray);
    }

    public void warn(String msg, Throwable t)
    {
        log(LOG_LEVEL_WARN, msg, t);
    }

    public boolean isErrorEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

    public void error(String msg)
    {
        log(LOG_LEVEL_ERROR, msg);
    }

    public void error(String format, Object arg)
    {
        formatAndLog(LOG_LEVEL_ERROR, format, arg);
    }

    public void error(String format, Object arg1, Object arg2)
    {
        formatAndLog(LOG_LEVEL_ERROR, format, arg1, arg2);
    }

    public void error(String format, Object... argArray)
    {
        formatAndLog(LOG_LEVEL_ERROR, format, argArray);
    }

    public void error(String msg, Throwable t)
    {
        log(LOG_LEVEL_ERROR, msg, t);
    }
}
