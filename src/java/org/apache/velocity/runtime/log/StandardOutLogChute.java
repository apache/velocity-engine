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

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Logger used when no other is configured.
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 */
public class StandardOutLogChute implements LogChute
{

    private int enabled = -1;

    public void init(RuntimeServices rs) throws Exception
    {
        // does nothing
    }

    protected String getPrefix(int level)
    {
        switch (level)
        {
            case LogChute.WARN_ID:
                return RuntimeConstants.WARN_PREFIX;
            case LogChute.INFO_ID:
                return RuntimeConstants.INFO_PREFIX ;
            case LogChute.DEBUG_ID:
                return RuntimeConstants.DEBUG_PREFIX;
            case LogChute.TRACE_ID:
                return RuntimeConstants.TRACE_PREFIX;
            case LogChute.ERROR_ID:
                return RuntimeConstants.ERROR_PREFIX;
            default:
                return RuntimeConstants.UNKNOWN_PREFIX;
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
     */
    public void setEnabledLevel(int level)
    {
        this.enabled = level;
    }

    /**
     * Returns the current minimum level at which messages will be printed.
     */
    public int getEnabledLevel()
    {
        return this.enabled;
    }

    /**
     * This will return true if the specified level
     * is equal to or higher than the level this
     * LogChute is enabled for.
     */
    public boolean isLevelEnabled(int level)
    {
        return (level >= this.enabled);
    }

}
