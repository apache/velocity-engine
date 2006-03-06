package org.apache.velocity.test;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ExternalLoggerTestCase extends TestCase implements LogChute
{

    private String logString = null;
    private VelocityEngine ve = null;

    /**
     * Default constructor.
     */
    public ExternalLoggerTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        /*
         *  use an alternative logger.  Set it up here and pass it in.
         */

        ve = new VelocityEngine();
        ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this );
        ve.init();
    }

    public void init( RuntimeServices rs )
    {
        // do nothing with it
    }

    public static Test suite ()
    {
        return new TestSuite(ExternalLoggerTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testExternalLogger ()
    {
        /*
         *  simply log something and see if we get it.
         */

        logString = null;

        String testString = "This is a test.";

        ve.warn( testString );

        if (logString == null || !logString.equals(WARN_PREFIX +  testString ) )
        {
            fail("Didn't recieve log message.");
        }
    }

    public void log(int level, String message)
    {
        String out = "";

        /*
         * Start with the appropriate prefix
         */
        switch( level )
        {
            case DEBUG_ID :
                out = DEBUG_PREFIX;
                break;
            case INFO_ID :
                out = INFO_PREFIX;
                break;
            case TRACE_ID :
                out = TRACE_PREFIX;
                break;
            case WARN_ID :
                out = WARN_PREFIX;
                break;
            case ERROR_ID :
                out = ERROR_PREFIX;
                break;
            default :
                out = INFO_PREFIX;
                break;
        }

        logString = out + message;
    }

    public void log(int level, String message, Throwable t)
    {
        // ignore the Throwable, we're not testing this method here
        log(level, message);
    }

    public boolean isLevelEnabled(int level)
    {
        return true;
    }
}
