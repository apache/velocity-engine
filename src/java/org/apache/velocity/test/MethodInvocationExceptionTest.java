package org.apache.velocity.test;

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

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.log.LogSystem;

import org.apache.velocity.exception.MethodInvocationException;

import junit.framework.TestCase;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: MethodInvocationExceptionTest.java,v 1.6 2001/08/07 22:20:28 geirm Exp $
 */
public class MethodInvocationExceptionTest extends TestCase 
{
   /**
     * Default constructor.
     */
    public MethodInvocationExceptionTest()
    {
        super("MethodInvocationExceptionTest");

        try
        {
            /*
             *  init() Runtime with defaults
             */
            Velocity.init();

        }
        catch (Exception e)
        {
            System.err.println("Cannot setup MethodInvocationExceptionTest : " + e);
            System.exit(1);
        }            
    }

    public static junit.framework.Test suite ()
    {
        return new MethodInvocationExceptionTest();
    }

    /**
     * Runs the test :
     *
     *  uses the Velocity class to eval a string
     *  which accesses a method that throws an 
     *  exception.
     */
    public void runTest ()
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
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
        catch( Exception e)
        {
            fail("Wrong exception thrown, first test." + e);
            e.printStackTrace();
        }

        /*
         *  second test - to ensure that methods accessed via get+ construction
         *  also work
         */

        template = "$woogie.foo boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, second test.");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
        catch( Exception e)
        {
            fail("Wrong exception thrown, second test");
        }

        template = "$woogie.Foo boing!";
 
        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, third test.");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
        catch( Exception e)
        {
            fail("Wrong exception thrown, third test");
        }

        template = "#set($woogie.foo = 'lala') boing!";
 
        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, set test.");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
        catch( Exception e)
        {
            fail("Wrong exception thrown, set test");
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
