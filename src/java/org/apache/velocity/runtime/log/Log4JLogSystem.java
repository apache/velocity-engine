package org.apache.velocity.runtime.log;

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

import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Appender;

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
 * @version $Id$
 * @since Velocity 1.5
 */
public class Log4JLogSystem implements LogSystem
{
    public static final String RUNTIME_LOG_LOG4J_LOGGER =
            "runtime.log.logsystem.log4j.logger";

    private RuntimeServices rsvc = null;

    /**
     * <a href="http://jakarta.apache.org/log4j/">Log4J</a>
     * logging API.
     */
    protected Logger logger = null;

    /**
     * <a href="http://jakarta.apache.org/log4j/">Log4J</a>
     * logging API.
     */
    public Log4JLogSystem()
    {
    }

    public void init(RuntimeServices rs)
    {
        rsvc = rs;

        /*
         *  first see if there is a category specified and just use that - it allows
         *  the application to make us use an existing logger
         */

        String loggerName =
            (String) rsvc.getProperty(RUNTIME_LOG_LOG4J_LOGGER);

        if (loggerName != null)
        {
            logger = Logger.getLogger(loggerName);
        
            logVelocityMessage(0,
                               "SimpleLog4JLogSystem using logger '"
                               + loggerName + '\'');

            return;
        }
        
        /*
         *  if not, use the file...
         */

        String logfile = rsvc.getString(RuntimeConstants.RUNTIME_LOG);

        /*
         *  now init.  If we can't, panic!
         */
        try
        {
            internalInit(logfile);

            logVelocityMessage(0,
                "Log4JLogSystem initialized using logfile '" + logfile + "'");
        }
        catch(Exception e)
        {
            System.err.println(
                "PANIC : error configuring Log4JLogSystem : " + e);
        }
    }

    /**
     *  initializes the log system using the logfile argument
     */
    private void internalInit(String logfile)
        throws Exception
    {
        /*
         *  do it by our classname to avoid conflicting with anything else 
         *  that might be used...
         */

        logger = Logger.getLogger(this.getClass().getName());
        logger.setAdditivity(false);

        /*
         * Priority is set for DEBUG becouse this implementation checks 
         * log level.
         */
        logger.setLevel(Level.DEBUG);

        RollingFileAppender appender = new RollingFileAppender(
                new PatternLayout( "%d - %m%n"), logfile, true);
        
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
    public void logVelocityMessage(int level, String message)
    {
        switch (level) 
        {
            case LogSystem.WARN_ID:
                logger.warn(message);
                break;
            case LogSystem.INFO_ID:
                logger.info(message);
                break;
            case LogSystem.DEBUG_ID:
                logger.debug(message);
                break;
            case LogSystem.ERROR_ID:
                logger.error(message);
                break;
            default:
                logger.debug(message);
                break;
        }
    }

    /**
     * Also do a shutdown if the object is destroy()'d.
     */
    protected void finalize()
            throws Throwable
    {
        shutdown();
    }

    /** Close all destinations*/
    public void shutdown()
    {
        Enumeration appenders = logger.getAllAppenders();
        while (appenders.hasMoreElements())
        {
            Appender appender = (Appender)appenders.nextElement();
            appender.close();
        }
    }
}
