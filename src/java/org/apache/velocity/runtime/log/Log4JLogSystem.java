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

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.*;
import org.apache.log4j.net.*;
import org.apache.log4j.spi.*;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Implementation of a Log4J logger.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: Log4JLogSystem.java,v 1.5 2001/08/07 22:07:37 geirm Exp $
 */
public class Log4JLogSystem implements LogSystem
{
    private RuntimeServices rsvc = null;

    /** log4java logging interface */
    protected Category logger = null;

    /** logging layout */
    protected Layout layout = null;

    /** the runtime.log property value */
    private String logfile = "";
    
    /**
     *  default CTOR.  Initializes itself using the property RUNTIME_LOG
     *  from the Velocity properties
     */
    public Log4JLogSystem()
    {
    }

    public void init( RuntimeServices rs )
    {
        rsvc = rs;

        /*
         *  since this is a Velocity-provided logger, we will
         *  use the Runtime configuration
         */
        logfile = rsvc.getString( RuntimeConstants.RUNTIME_LOG );

        /*
         *  now init.  If we can't, panic!
         */
        try
        {
            internalInit();

            logVelocityMessage( 0, 
                "Log4JLogSystem initialized using logfile " + logfile );
        }
        catch( Exception e )
        {
            System.out.println( 
                "PANIC : error configuring Log4JLogSystem : " + e );
        }
    }

    /**
     *  initializes the log system using the logfile argument
     *
     *  @param logFile   file for log messages
     */
    private void internalInit()
        throws Exception
    {
        logger = Category.getInstance("");
        logger.setAdditivity(false);

        /*
         * Priority is set for DEBUG becouse this implementation checks 
         * log level.
         */
        logger.setPriority(Priority.DEBUG);

        String pattern = rsvc.getString( RuntimeConstants.LOGSYSTEM_LOG4J_PATTERN );
        
        if (pattern == null || pattern.length() == 0)
        {
            pattern = "%d - %m%n";
        }
        
        layout = new PatternLayout(pattern);
        
        configureFile();
        configureRemote();
        configureSyslog();
        configureEmail();
    }

    /**
     * Configures the logging to a file.
     */
    private void configureFile()
        throws Exception
    {
        int backupFiles = 
            rsvc.getInt(RuntimeConstants.LOGSYSTEM_LOG4J_FILE_BACKUPS, 1);
        int fileSize = 
            rsvc.getInt(RuntimeConstants.LOGSYSTEM_LOG4J_FILE_SIZE, 100000);
        
        Appender appender = new RollingFileAppender(layout,logfile,true);
        
        ((RollingFileAppender)appender).setMaxBackupIndex(backupFiles);
        
        /* finding file size */
        if (fileSize > -1)
        {
            ((RollingFileAppender)appender).setMaximumFileSize(fileSize);
        }
        logger.addAppender(appender);
    }

    /**
     * Configures the logging to a remote server
     */
    private void configureRemote()
        throws Exception
    {
        String remoteHost = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_REMOTE_HOST);
        int remotePort = 
            rsvc.getInt(RuntimeConstants.LOGSYSTEM_LOG4J_REMOTE_PORT, 1099);
        
        if (remoteHost == null || remoteHost.trim().equals("") || 
            remotePort <= 0)
        {
            return;
        }
        
        Appender appender=new SocketAppender(remoteHost,remotePort);
        
        logger.addAppender(appender);
    }

    /**
     * Configures the logging to syslogd
     */
    private void configureSyslog()
        throws Exception
    {
        String syslogHost = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_SYSLOGD_HOST);
        String syslogFacility = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_SYSLOGD_FACILITY);
        
        if (syslogHost == null || syslogHost.trim().equals("") || 
            syslogFacility == null )
        {
            return;
        }

        Appender appender = new SyslogAppender();
        
        ((SyslogAppender)appender).setLayout(layout);
        ((SyslogAppender)appender).setSyslogHost(syslogHost);
        ((SyslogAppender)appender).setFacility(syslogFacility);
        
        logger.addAppender(appender);
    }

    /**
     * Configures the logging to email
     */
    private void configureEmail()
        throws Exception
    {
        String smtpHost = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_EMAIL_SERVER);
        String emailFrom = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_EMAIL_FROM);
        String emailTo = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_EMAIL_TO);
        String emailSubject = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_EMAIL_SUBJECT);
        String bufferSize = 
            rsvc.getString(RuntimeConstants.LOGSYSTEM_LOG4J_EMAIL_BUFFER_SIZE);

        if (smtpHost == null || smtpHost.trim().equals("")
                || emailFrom == null || smtpHost.trim().equals("")
                || emailTo == null || emailTo.trim().equals("")
                || emailSubject == null || emailSubject.trim().equals("")
                || bufferSize == null || bufferSize.trim().equals("") )
        {
            return;
        }

        SMTPAppender appender = new SMTPAppender();
       
        appender.setSMTPHost( smtpHost );
        appender.setFrom( emailFrom );
        appender.setTo( emailTo );
        appender.setSubject( emailSubject );

        appender.setBufferSize( Integer.parseInt(bufferSize) );
        
        appender.setLayout(layout);
        appender.activateOptions();
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
