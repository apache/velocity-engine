package org.apache.velocity.runtime.log;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Enumeration;

import org.apache.log4j.*;
import org.apache.log4j.net.*;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Implementation of a Log4J logger.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: Log4JLogSystem.java,v 1.7.4.1 2004/03/03 23:22:56 geirm Exp $
 *
 * @deprecated As of v1.3.  Use
 *  {@link SimpleLog4jLogSystem}
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
