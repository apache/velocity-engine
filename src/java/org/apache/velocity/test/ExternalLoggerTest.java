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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;

import org.apache.velocity.runtime.log.LogSystem;

import junit.framework.TestCase;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ExternalLoggerTest.java,v 1.4 2001/08/20 11:08:14 geirm Exp $
 */
public class ExternalLoggerTest extends TestCase implements LogSystem
{
   
    private String logString = null;
    private VelocityEngine ve = null;

    /**
     * Default constructor.
     */
    public ExternalLoggerTest()
    {
        super("LoggerTest");

        try
        {
            /*
             *  use an alternative logger.  Set it up here and pass it in.
             */
            
            ve = new VelocityEngine();
            ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this );
            ve.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup LoggerTest : " + e);
            System.exit(1);
        }            
    }

    public void init( RuntimeServices rs )
    {
        // do nothing with it
    }

    public static junit.framework.Test suite ()
    {
        return new ExternalLoggerTest();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        /*
         *  simply log something and see if we get it.
         */

        logString = null;

        String testString = "This is a test.";

        ve.warn( testString );

        if (logString == null || !logString.equals( VelocityEngine.WARN_PREFIX +  testString ) )
        {
            fail("Didn't recieve log message.");
        }
    }

    public void logVelocityMessage(int level, String message)
    {
        String out = "";

        /*
         * Start with the appropriate prefix
         */
        switch( level ) 
        {
            case LogSystem.DEBUG_ID :
                out = VelocityEngine.DEBUG_PREFIX;
                break;
            case LogSystem.INFO_ID :
                out = VelocityEngine.INFO_PREFIX;
                break;
            case LogSystem.WARN_ID :
                out = VelocityEngine.WARN_PREFIX;
                break;
            case LogSystem.ERROR_ID : 
                out = VelocityEngine.ERROR_PREFIX;
                break;
            default :
                out = VelocityEngine.UNKNOWN_PREFIX;
                break;
        }

        logString =  out + message;
    }
}
