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

import java.net.URL;

import org.apache.log.Priority;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.output.FileOutputLogTarget;

import org.apache.velocity.util.StringUtils;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Implementation of a Avalon logger.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: AvalonLogSystem.java,v 1.9 2001/08/20 11:44:49 geirm Exp $
 */
public class AvalonLogSystem implements LogSystem
{
    private Logger logger = null;
    private String logPath = "";
    
    private RuntimeServices rsvc = null;

    /**
     *  default CTOR.  Initializes itself using the property RUNTIME_LOG
     *  from the Velocity properties
     */

    public AvalonLogSystem()
    {
    }

    public void init( RuntimeServices rs )
        throws Exception
    {
        this.rsvc = rs;

        /*
         *  if a logger is specified, we will use this instead of
         *  the default
         */
        String loggerName = (String) rsvc.getProperty("runtime.log.logsystem.avalon.logger");
        
        if (loggerName != null)
        {
            this.logger = Hierarchy.getDefaultHierarchy().getLoggerFor(loggerName);
        } 
        else 
        {
            /*
             *  since this is a Velocity-provided logger, we will
             *  use the Runtime configuration
             */
            String logfile = (String) rsvc.getProperty( RuntimeConstants.RUNTIME_LOG );

            /*
             *  now init.  If we can't, panic!
             */
            try
            {
                init( logfile );

                logVelocityMessage( 0,
                    "AvalonLogSystem initialized using logfile " + logPath );
            }
            catch( Exception e )
            {
                System.out.println(
                    "PANIC : Error configuring AvalonLogSystem : " + e );
                System.err.println(
                    "PANIC : Error configuring AvalonLogSystem : " + e );

                throw new Exception("Unable to configure AvalonLogSystem : " + e );
            }
        }
    }

    /**
     *  initializes the log system using the logfile argument
     *
     *  @param logFile   file for log messages
     */
    public void init(String logFile)
        throws Exception
    {
        FileOutputLogTarget target = new FileOutputLogTarget();
        File logFileLocation = new File (logFile);
        
        logPath = logFileLocation.getAbsolutePath();

        target.setFilename( logPath );
        target.setFormatter(new VelocityFormatter());
        target.setFormat("%{time} %{message}\\n%{throwable}" );
                
        /*
         *  use the toString() of RuntimeServices to make a unique logger
         */

        logger = Hierarchy.getDefaultHierarchy().getLoggerFor( rsvc.toString() );
        logger.setPriority( Priority.DEBUG );
        logger.setLogTargets( new LogTarget[] { target } );
    }
    
    /**
     *  logs messages
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void logVelocityMessage(int level, String message)
    {
        /*
         *  based on level, call teh right logger method
         *  and prefix with the appropos prefix
         */

        switch (level) 
        {
            case LogSystem.WARN_ID:
                logger.warn( RuntimeConstants.WARN_PREFIX + message );
                break;
            case LogSystem.INFO_ID:
                logger.info( RuntimeConstants.INFO_PREFIX + message);
                break;
            case LogSystem.DEBUG_ID:
                logger.debug( RuntimeConstants.DEBUG_PREFIX + message);
                break;
            case LogSystem.ERROR_ID:
                logger.error(RuntimeConstants.ERROR_PREFIX + message);
                break;
            default:
                logger.info( message);
                break;
        }
    }
}
