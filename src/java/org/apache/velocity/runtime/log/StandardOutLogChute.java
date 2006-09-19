/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import org.apache.velocity.runtime.RuntimeServices;

/**
 * Logger used when no other is configured.
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 */
public class StandardOutLogChute implements LogChute
{
    /** */
    public static final String RUNTIME_LOG_LEVEL_KEY =
        "runtime.log.logsystem.stdout.level";

    private int enabled = TRACE_ID;

    /**
     * @see org.apache.velocity.runtime.log.LogChute#init(org.apache.velocity.runtime.RuntimeServices)
     */
    public void init(RuntimeServices rs) throws Exception
    {
        // look for a level config property
        String level = (String)rs.getProperty(RUNTIME_LOG_LEVEL_KEY);
        if (level != null)
        {
            // and set it accordingly
            if (level.equalsIgnoreCase("debug"))
            {
                setEnabledLevel(DEBUG_ID);
            }
            else if (level.equalsIgnoreCase("info"))
            {
                setEnabledLevel(INFO_ID);
            }
            else if (level.equalsIgnoreCase("warn"))
            {
                setEnabledLevel(WARN_ID);
            }
            else if (level.equalsIgnoreCase("error"))
            {
                setEnabledLevel(ERROR_ID);
            }
        }
    }

    /**
     * @param level
     * @return The prefix for the given level.
     */
    protected String getPrefix(int level)
    {
        switch (level)
        {
            case WARN_ID:
                return WARN_PREFIX;
            case INFO_ID:
                return INFO_PREFIX ;
            case DEBUG_ID:
                return DEBUG_PREFIX;
            case TRACE_ID:
                return TRACE_PREFIX;
            case ERROR_ID:
                return ERROR_PREFIX;
            default:
                return INFO_PREFIX;
        }
    }

    /**
     * Logs messages to either std.out or std.err
     * depending on their severity.
     *
     * @param level severity level
     * @param message complete error message
     */
    public void log(int level, String message)
    {
        // pass it off
        log(level, message, null);
    }

    /**
     * Logs messages to the system console so long as the specified level
     * is equal to or greater than the level this LogChute is enabled for.
     * If the level is equal to or greater than LogChute.ERROR_ID,
     * messages will be printed to System.err. Otherwise, they will be
     * printed to System.out. If a java.lang.Throwable accompanies the
     * message, it's stack trace will be printed to the same stream
     * as the message.
     *
     * @param level severity level
     * @param message complete error message
     * @param t the java.lang.Throwable
     */
    public void log(int level, String message, Throwable t)
    {
        if (!isLevelEnabled(level))
        {
            return;
        }

        String prefix = getPrefix(level);
        if (level > 2)
        {
            System.err.print(prefix);
            System.err.println(message);
            if (t != null)
            {
                System.err.println(t.getMessage());
                t.printStackTrace();
            }
        }
        else
        {
            System.out.print(prefix);
            System.out.println(message);
            if (t != null)
            {
                System.out.println(t.getMessage());
                t.printStackTrace(System.out);
            }
        }
    }

    /**
     * Set the minimum level at which messages will be printed.
     * @param level
     */
    public void setEnabledLevel(int level)
    {
        this.enabled = level;
    }

    /**
     * Returns the current minimum level at which messages will be printed.
     * @return The current minimum level at which messages will be printed.
     */
    public int getEnabledLevel()
    {
        return this.enabled;
    }

    /**
     * This will return true if the specified level
     * is equal to or higher than the level this
     * LogChute is enabled for.
     * @param level
     * @return True if logging is enabled for this level.
     */
    public boolean isLevelEnabled(int level)
    {
        return (level >= this.enabled);
    }

}
