/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.velocity.runtime.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.runtime.RuntimeServices;

/**
 * Implementation of a simple java.util.logging LogChute.
 *
 * @author <a href="mailto:nbubna@apache.org>Nathan Bubna</a>
 * @version $Id: JdkLogChute.java 291585 2005-09-26 08:56:23Z henning $
 * @since Velocity 1.5
 */
public class JdkLogChute implements LogChute
{
    /** Property key for specifying the name for the logger instance */
    public static final String RUNTIME_LOG_JDK_LOGGER =
        "runtime.log.logsystem.jdk.logger";

    /** Default name for the JDK logger instance */
    public static final String DEFAULT_LOG_NAME = "org.apache.velocity";

    protected Logger logger = null;

    public void init(RuntimeServices rs)
    {
        String name = (String)rs.getProperty(RUNTIME_LOG_JDK_LOGGER);
        if (name == null)
        {
            name = DEFAULT_LOG_NAME;
        }
        logger = Logger.getLogger(name);
        log(LogChute.DEBUG_ID, "JdkLogChute will use logger '"+name+'\'');
    }

    /**
     * Returns the java.util.logging.Level that matches
     * to the specified LogChute level.
     */
    protected Level getJdkLevel(int level)
    {
        switch (level)
        {
            case LogChute.WARN_ID:
                return Level.WARNING;
            case LogChute.INFO_ID:
                return Level.INFO;
            case LogChute.DEBUG_ID:
                return Level.FINE;
            case LogChute.TRACE_ID:
                return Level.FINEST;
            case LogChute.ERROR_ID:
                return Level.SEVERE;
            default:
                return Level.FINER;
        }
    }

    /**
     * Logs messages
     *
     * @param level severity level
     * @param message complete error message
     */
    public void log(int level, String message)
    {
        log(level, message, null);
    }

    /**
     * Send a log message from Velocity along with an exception or error
     */
    public void log(int level, String message, Throwable t)
    {
        Level jdkLevel = getJdkLevel(level);
        if (t == null)
        {
            logger.log(jdkLevel, message);
        }
        else
        {
            logger.log(jdkLevel, message, t);
        }
    }

    /**
     * Checks whether the logger is enabled for the specified level
     */
    public boolean isLevelEnabled(int level)
    {
        Level jdkLevel = getJdkLevel(level);
        return logger.isLoggable(jdkLevel);
    }

}
