package org.apache.velocity.runtime.log;

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

import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.Appender;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Implementation of a simple Log4J system that will either latch onto
 * an existing logger, or produce a simple rolling file log.
 *
 * @author <a href="mailto:geirm@apache.org>Geir Magnusson Jr.</a>
 * @author <a href="mailto:jon@apache.org>Jon Scott Stevens</a>
 * @version $Id: SimpleLog4JLogSystem.java,v 1.2 2003/10/22 01:08:39 dlr Exp $
 * @since Velocity 1.3
 */
public class SimpleLog4JLogSystem implements LogSystem
{
    private RuntimeServices rsvc = null;

    /**
     * <a href="http://jakarta.apache.org/log4j/">Log4J</a>
     * logging API.
     */
    protected Logger category = null;

    /**
     * Constructor used by {@link
     * org.apache.velocity.runtime.log.LogManager#createLogSystem()}.
     */
    public SimpleLog4JLogSystem()
    {
    }

    public void init( RuntimeServices rs )
    {
        rsvc = rs;

        /*
         *  first see if there is a logger specified and just use that - it allows
         *  the application to make us use an existing logger
         */

        String loggerName =
            (String) rsvc.getProperty("runtime.log.logsystem.log4j.category");

        if ( loggerName != null )
        {
            category = Logger.getLogger( loggerName );
        
            logVelocityMessage( 0,
                                "SimpleLog4JLogSystem using logger '" + loggerName + "'");

            return;
        }
        
        /*
         *  if not, use the file...
         */

        String logfile = rsvc.getString( RuntimeConstants.RUNTIME_LOG );

        /*
         *  now init.  If we can't, panic!
         */
        try
        {
            internalInit( logfile );

            logVelocityMessage( 0, 
                "SimpleLog4JLogSystem initialized using logfile '" + logfile + "'" );
        }
        catch( Exception e )
        {
            System.out.println( 
                "PANIC : error configuring SimpleLog4JLogSystem : " + e );
        }
    }

    /**
     *  initializes the log system using the logfile argument
     */
    private void internalInit( String logfile )
        throws Exception
    {
        /*
         *  do it by our classname to avoid conflicting with anything else 
         *  that might be used...
         */

        category = Logger.getLogger(this.getClass().getName());
        category.setAdditivity(false);

        /*
         * Priority is set for DEBUG becouse this implementation checks 
         * log level.
         */
        category.setPriority(Priority.DEBUG);

        RollingFileAppender appender = new RollingFileAppender( new PatternLayout( "%d - %m%n"), logfile, true);
        
        appender.setMaxBackupIndex( 1 );
        
        appender.setMaximumFileSize( 100000 );
        
        category.addAppender(appender);
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
                category.warn(message);
                break;
            case LogSystem.INFO_ID:
                category.info(message);
                break;
            case LogSystem.DEBUG_ID:
                category.debug(message);
                break;
            case LogSystem.ERROR_ID:
                category.error(message);
                break;
            default:
                category.debug(message);
                break;
        }
    }

    /**
     * Also do a shutdown if the object is destroy()'d.
     */
    protected void finalize() throws Throwable
    {
        shutdown();
    }

    /** Close all log destinations. */
    public void shutdown()
    {
        Enumeration appenders = category.getAllAppenders();
        while (appenders.hasMoreElements())
        {
            Appender appender = (Appender) appenders.nextElement();
            appender.close();
        }
    }
}
