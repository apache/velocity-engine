package org.apache.velocity.runtime.log;

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

/**
 * Convenient wrapper for LogChute functions. This implements
 * the RuntimeLogger methods (and then some).  It is hoped that
 * use of this will fully replace use of the RuntimeLogger.
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 */
public class Log
{

    private LogChute chute;

    /**
     * Creates a new Log that wraps a HoldingLogChute.
     */
    public Log()
    {
        setLogChute(new HoldingLogChute());
    }

    /**
     * Creates a new Log that wraps the specified LogChute.
     * @param chute
     */
    public Log(final LogChute chute)
    {
        setLogChute(chute);
    }

    /**
     * Updates the LogChute wrapped by this Log instance.
     * @param chute The new value for the log chute.
     */
    protected void setLogChute(final LogChute chute)
    {
        if (chute == null)
        {
            throw new NullPointerException("The LogChute cannot be set to null!");
        }
        this.chute = chute;
    }

    /**
     * Returns the LogChute wrapped by this Log instance.
     * @return The LogChute wrapped by this Log instance.
     */
    protected LogChute getLogChute()
    {
        return this.chute;
    }

    protected void log(int level, Object message)
    {
        getLogChute().log(level, String.valueOf(message));
    }

    protected void log(int level, Object message, Throwable t)
    {
        getLogChute().log(level, String.valueOf(message), t);
    }

    /**
     * Returns true if trace level messages will be printed by the LogChute.
     * @return If trace level messages will be printed by the LogChute.
     */
    public boolean isTraceEnabled()
    {
        return getLogChute().isLevelEnabled(LogChute.TRACE_ID);
    }

    /**
     * Log a trace message.
     * @param message
     */
    public void trace(Object message)
    {
        log(LogChute.TRACE_ID, message);
    }

    /**
     * Log a trace message and accompanying Throwable.
     * @param message
     * @param t
     */
    public void trace(Object message, Throwable t)
    {
        log(LogChute.TRACE_ID, message, t);
    }

    /**
     * Returns true if debug level messages will be printed by the LogChute.
     * @return True if debug level messages will be printed by the LogChute.
     */
    public boolean isDebugEnabled()
    {
        return getLogChute().isLevelEnabled(LogChute.DEBUG_ID);
    }

    /**
     * Log a debug message.
     * @param message
     */
    public void debug(Object message)
    {
        log(LogChute.DEBUG_ID, message);
    }

    /**
     * Log a debug message and accompanying Throwable.
     * @param message
     * @param t
     */
    public void debug(Object message, Throwable t)
    {
        log(LogChute.DEBUG_ID, message, t);
    }

    /**
     * Returns true if info level messages will be printed by the LogChute.
     * @return True if info level messages will be printed by the LogChute.
     */
    public boolean isInfoEnabled()
    {
        return getLogChute().isLevelEnabled(LogChute.INFO_ID);
    }

    /**
     * Log an info message.
     * @param message
     */
    public void info(Object message)
    {
        log(LogChute.INFO_ID, message);
    }

    /**
     * Log an info message and accompanying Throwable.
     * @param message
     * @param t
     */
    public void info(Object message, Throwable t)
    {
        log(LogChute.INFO_ID, message, t);
    }

    /**
     * Returns true if warn level messages will be printed by the LogChute.
     * @return True if warn level messages will be printed by the LogChute.
     */
    public boolean isWarnEnabled()
    {
        return getLogChute().isLevelEnabled(LogChute.WARN_ID);
    }

    /**
     * Log a warning message.
     * @param message
     */
    public void warn(Object message)
    {
        log(LogChute.WARN_ID, message);
    }

    /**
     * Log a warning message and accompanying Throwable.
     * @param message
     * @param t
     */
    public void warn(Object message, Throwable t)
    {
        log(LogChute.WARN_ID, message, t);
    }

    /**
     * Returns true if error level messages will be printed by the LogChute.
     * @return True if error level messages will be printed by the LogChute.
     */
    public boolean isErrorEnabled()
    {
        return getLogChute().isLevelEnabled(LogChute.ERROR_ID);
    }

    /**
     * Log an error message.
     * @param message
     */
    public void error(Object message)
    {
        log(LogChute.ERROR_ID, message);
    }

    /**
     * Log an error message and accompanying Throwable.
     * @param message
     * @param t
     */
    public void error(Object message, Throwable t)
    {
        log(LogChute.ERROR_ID, message, t);
    }

}
