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

import java.lang.reflect.Field;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Implementation of a simple log4j system that will either latch onto
 * an existing category, or just do a simple rolling file log.
 *
 * Use this one rather than {@link SimpleLog4JLogSystem}; it uses the
 * modern <code>Logger</code> concept of Log4J, rather than the
 * deprecated <code>Categeory</code> concept.
 *
 * @author <a href="mailto:geirm@apache.org>Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@finemaltcoding.com>Daniel L. Rall</a>
 * @author <a href="mailto:nbubna@apache.org>Nathan Bubna</a>
 * @version $Id: Log4JLogChute.java 292075 2005-09-28 00:08:12Z dlr $
 * @since Velocity 1.5
 */
public class Log4JLogChute implements LogChute
{
    public static final String RUNTIME_LOG_LOG4J_LOGGER =
            "runtime.log.logsystem.log4j.logger";

    private RuntimeServices rsvc = null;
    private boolean hasTrace = false;
    private RollingFileAppender appender = null;

    /**
     * <a href="http://jakarta.apache.org/log4j/">Log4J</a> logging API.
     */
    protected Logger logger = null;

    public void init(RuntimeServices rs)
    {
        rsvc = rs;

        /* first see if there is a category specified and just use that - it allows
         * the application to make us use an existing logger
         */
        String loggerName = (String)rsvc.getProperty(RUNTIME_LOG_LOG4J_LOGGER);
        if (loggerName != null)
        {
            logger = Logger.getLogger(loggerName);
        
            log(0, "Log4JLogChute using logger '"
                               + loggerName + '\'');
        }
        else
        {
            // if not, use the file...
            String logfile = rsvc.getString(RuntimeConstants.RUNTIME_LOG);

            // now init.  If we can't, panic!
            try
            {
                internalInit(logfile);
                log(0, "Log4JLogChute initialized using logfile '" + logfile + "'");
            }
            catch(Exception e)
            {
                System.err.println("PANIC: error configuring Log4JLogChute - " + e);
            }
        }

        /* Ok, now let's see if this version of log4j supports the trace level. */
        try
        {
            Field traceLevel = Level.class.getField("TRACE");
            if (traceLevel != null)
            {
                hasTrace = true;
            }
        }
        catch (NoSuchFieldException e)
        {
            log(0, "The version of log4j being used does not support the \"trace\" level.");
        }
    }

    /*
     * initializes the log system using the logfile argument
     */
    private void internalInit(String logfile) throws Exception
    {
        /* do it by our classname to avoid conflicting with anything else 
         * that might be used...
         */
        logger = Logger.getLogger(this.getClass().getName());
        logger.setAdditivity(false);

        /* Priority is set for DEBUG becouse this implementation checks 
         * log level. */
        logger.setLevel(Level.DEBUG);

        this.appender = 
            new RollingFileAppender(new PatternLayout("%d - %m%n"), 
                                    logfile, true);
        
        appender.setMaxBackupIndex(1);
        appender.setMaximumFileSize(100000);
        logger.addAppender(appender);
    }

    /**
     *  logs messages
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void log(int level, String message)
    {
        switch (level) 
        {
            case LogChute.WARN_ID:
                logger.warn(message);
                break;
            case LogChute.INFO_ID:
                logger.info(message);
                break;
            case LogChute.DEBUG_ID:
                logger.debug(message);
                break;
            case LogChute.TRACE_ID:
                if (hasTrace)
                {
                    logger.trace(message);
                }
                else
                {
                    logger.debug(message);
                }
                break;
            case LogChute.ERROR_ID:
                logger.error(message);
                break;
            default:
                logger.debug(message);
                break;
        }
    }

    /**
     * Send a log message from Velocity along with an exception or error
     */
    public void log(int level, String message, Throwable t)
    {
        switch (level) 
        {
            case LogChute.WARN_ID:
                logger.warn(message, t);
                break;
            case LogChute.INFO_ID:
                logger.info(message, t);
                break;
            case LogChute.DEBUG_ID:
                logger.debug(message, t);
                break;
            case LogChute.TRACE_ID:
                if (hasTrace)
                {
                    logger.trace(message, t);
                }
                else
                {
                    logger.debug(message, t);
                }
                break;
            case LogChute.ERROR_ID:
                logger.error(message, t);
                break;
            default:
                logger.debug(message, t);
                break;
        }
    }

    /**
     * Checks whether the logger is enabled for the specified level
     */
    public boolean isLevelEnabled(int level)
    {
        switch (level)
        {
            case LogChute.DEBUG_ID:
                return logger.isDebugEnabled();
            case LogChute.INFO_ID:
                return logger.isInfoEnabled();
            case LogChute.TRACE_ID:
                if (hasTrace)
                {
                    return logger.isTraceEnabled();
                }
                else
                {
                    return logger.isDebugEnabled();
                }
            case LogChute.WARN_ID:
                return logger.isEnabledFor(Level.WARN);
            case LogChute.ERROR_ID:
                // can't be disabled in log4j
                return logger.isEnabledFor(Level.ERROR);
            default:
                return true;
        }
    }

    /**
     * Also do a shutdown if the object is destroy()'d.
     */
    protected void finalize() throws Throwable
    {
        shutdown();
    }

    /** Close all destinations*/
    public void shutdown()
    {
        if (appender != null)
        {
            logger.removeAppender(appender);
            appender.close();
        }
    }

}
