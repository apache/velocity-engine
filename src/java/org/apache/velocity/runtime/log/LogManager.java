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

/**
 * LogManager.java
 *
 * Very rudimentary log manager. Lifted in part from the
 * SAR deployer in Avalon. This is how the logging system
 * is supposed to be used. The Velocity runtime uses
 * this class for logging.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: LogManager.java,v 1.2 2001/03/05 11:46:28 jvanzyl Exp $
 */
public class LogManager
{
    /*
     * This method was removed from Avalon, it was noted that this
     * method should be moved to a LogManager of some sort so that's
     * what I'm doing. This is the start of a LogManager because there
     * doesn't appear to be one in Avalon.
     */
    public static Logger createLogger(String logFile)
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
                
        return LogKit.createLogger( category, logTargets );
    }
}
