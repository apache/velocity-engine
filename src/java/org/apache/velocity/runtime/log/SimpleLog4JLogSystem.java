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

import org.apache.log4j.Category;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.Appender;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 *  Implementation of a simple log4j system that will either
 *  latch onto an existing category, or just do a simple
 *  rolling file log.  Derived from Jon's 'complicated'
 *  version :)
 *
 * @author <a href="mailto:geirm@apache.org>Geir Magnusson Jr.</a>
 * @version $Id: SimpleLog4JLogSystem.java,v 1.1 2001/11/17 12:02:35 geirm Exp $
 */
public class SimpleLog4JLogSystem implements LogSystem
{
    private RuntimeServices rsvc = null;

    /** log4java logging interface */
    protected Category logger = null;

    public SimpleLog4JLogSystem()
    {
    }

    public void init( RuntimeServices rs )
    {
        rsvc = rs;

        /*
         *  first see if there is a category specified and just use that - it allows
         *  the application to make us use an existing logger
         */

        String categoryname =  (String) rsvc.getProperty("runtime.log.logsystem.log4j.category");

        if ( categoryname != null )
        {
            logger = Category.getInstance( categoryname );
        
            logVelocityMessage( 0,
                                "SimpleLog4JLogSystem using category '" + categoryname + "'");

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

        logger = Category.getInstance(this.getClass().getName());
        logger.setAdditivity(false);

        /*
         * Priority is set for DEBUG becouse this implementation checks 
         * log level.
         */
        logger.setPriority(Priority.DEBUG);

        RollingFileAppender appender = new RollingFileAppender( new PatternLayout( "%d - %m%n"), logfile, true);
        
        appender.setMaxBackupIndex( 1 );
        
        appender.setMaximumFileSize( 100000 );
        
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
                logger.warn( message );
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
    protected void finalize() throws Throwable
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
