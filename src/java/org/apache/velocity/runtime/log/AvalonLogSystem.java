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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import org.apache.log.Category;
import org.apache.log.Formatter;
import org.apache.log.Priority;
import org.apache.log.Logger;
import org.apache.log.LogKit;
import org.apache.log.LogTarget;
import org.apache.log.output.FileOutputLogTarget;

import org.apache.velocity.util.StringUtils;

/**
 * Implementation of a Avalon logger.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: AvalonLogSystem.java,v 1.1 2001/03/12 07:19:51 jon Exp $
 */
public class AvalonLogSystem implements LogSystem
{
    private Logger logger = null;
    private boolean stackTrace = false;
    
    public void init(String logFile)
        throws Exception
    {
        String targetName = "velocity";
        String priority = "DEBUG";
                
        Category category = LogKit.createCategory( 
            targetName, LogKit.getPriorityForName( priority ) );
        
        /*
         * Just create a FileOutputLogTarget, this is taken
         * from the SAR deployer in Avalon.
         */
        FileOutputLogTarget target = new FileOutputLogTarget();
        File logFileLocation = new File (logFile);
        
        target.setFilename(logFileLocation.getAbsolutePath());
        target.setFormatter(new VelocityFormatter());
        target.setFormat("%{time} %{message}\\n%{throwable}" );
        
        LogTarget logTargets[] = null;
                
        if ( null != target ) 
        {
            logTargets = new LogTarget[] { target };
        }            
                
        logger = LogKit.createLogger( category, logTargets );
    }
    
    public void log (int type, Object message)
    {
		switch (type)
		{
			case LogSystem.WARN_ID:
						 warn(message);
						 break;
			case LogSystem.INFO_ID:
						 info(message);
						 break;
			case LogSystem.DEBUG_ID:
						 debug(message);
						 break;
			case LogSystem.ERROR_ID:
						 error(message);
						 break;
			default:
				info(message);
				break;
		}
    }
    /**
     * Handle logging.
     *
     * @param Object message to log
     */
    public void warn(Object message)
    {
        String out = null;
        
        if ( getStackTrace() &&
            (message instanceof Throwable || message instanceof Exception) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        logger.warn(WARN + out);
    }

    /**
     * Handle logging.
     *
     * @param Object message to log
     */
    public void info(Object message)
    {
        String out = null;
        
        if ( getStackTrace() &&
            (message instanceof Throwable || message instanceof Exception) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        logger.info(INFO + out);
    }

    /**
     * Handle logging.
     *
     * @param Object message to log
     */
    public void error(Object message)
    {
        String out = null;
        
        if ( getStackTrace() &&
            ( message instanceof Throwable || message instanceof Exception ) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        logger.error(ERROR + out);
    }

    /**
     * Handle logging.
     *
     * @param Object message to log
     */
    public void debug(Object message)
    {
        if (!DEBUG_ON) return;
        
        String out = null;
        
        if ( getStackTrace() &&
            ( message instanceof Throwable || message instanceof Exception ) )
        {
            out = StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out = message.toString();    
        }
        logger.debug(DEBUG + out);
    }
    
    public boolean getStackTrace()
    {
        return stackTrace;
    }
    
    public void setStackTrace(boolean value)
    {
        stackTrace = value;
    }
}
