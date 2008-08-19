package org.apache.velocity.test.misc;

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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * LogChute implementation that creates a String in memory.  Used to test
 * log information.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class TestLogChute implements LogChute
{
    public static final String TEST_LOGGER_LEVEL = "runtime.log.logsystem.test.level";

    private StringBuffer log = new StringBuffer();

    private int logLevel;
    private boolean on;
    
    public void init(RuntimeServices rs) throws Exception
    {
        String level = rs.getString(TEST_LOGGER_LEVEL, "debug");
        logLevel = getLevelNumber(level, LogChute.DEBUG_ID);
    }

    public void on()
    {
        on = true;
    }

    public void off()
    {
        on = false;
    }

    public void log(int level, String message)
    {
        if (on && level >= logLevel)
        {
            String levelName;
            levelName = getLevelName(level);
            log.append(" [").append(levelName).append("] ");
            log.append(message);
            log.append("\n");
        }
    }
    
    /**
     * Return the stored log messages to date.
     * @return log messages
     */
    public String getLog()
    {
        return log.toString();
    }

    /**
     * Return the name corresponding to each level
     * @param level integer level
     * @return String level name
     */
    private String getLevelName(int level)
    {
        String levelName;
        if (level == LogChute.DEBUG_ID)
        {
            levelName = "debug";
        }
        else if (level == LogChute.INFO_ID)
        {
            levelName = "info";
        }
        else if (level == LogChute.TRACE_ID)
        {
            levelName = "trace";
        }
        else if (level == LogChute.WARN_ID)
        {
            levelName = "warn";
        }
        else if (level == LogChute.ERROR_ID)
        {
            levelName = "error";
        }
        else 
        {
            levelName = "";
        }

        return levelName;
    }

    /**
     * Return the integer level correspoding to the string number, or use the default
     * @param level name
     * @param defaultLevel the default if the name does not exist
     * @return integer level
     */
    private int getLevelNumber(String level, int defaultLevel)
    {
        if (level == null)
        {
            return defaultLevel;
        }
        else if (level.equalsIgnoreCase("DEBUG"))
        {
            return LogChute.DEBUG_ID;
        }
        else if (level.equalsIgnoreCase("ERROR"))
        {
            return LogChute.ERROR_ID;
        }
        else if (level.equalsIgnoreCase("INFO"))
        {
            return LogChute.INFO_ID;
        }
        else if (level.equalsIgnoreCase("TRACE"))
        {
            return LogChute.TRACE_ID;
        }
        else if (level.equalsIgnoreCase("WARN"))
        {
            return LogChute.WARN_ID;
        }
        else 
        {
            return defaultLevel;
        }
    }
    
    public void log(int level, String message, Throwable t)
    {
        if (on && level >= logLevel)
        {
            String levelName;
            levelName = getLevelName(level);
            log.append(" [").append(levelName).append("] ");
            log.append(message);
            log.append("\n");
            log.append(t.toString());
            log.append("\n");
        }
    }

    public boolean isLevelEnabled(int level)
    {
        return level >= logLevel;
    }

}
