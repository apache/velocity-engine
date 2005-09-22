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

import junit.framework.TestCase;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ExternalLoggerTestCase extends TestCase implements LogSystem
{

    private String logString = null;
    private VelocityEngine ve = null;

    /**
     * Default constructor.
     */
    public ExternalLoggerTestCase()
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
        return new ExternalLoggerTestCase();
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
