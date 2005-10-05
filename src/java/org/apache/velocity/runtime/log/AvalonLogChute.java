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

import java.io.File;
import java.io.IOException;
import org.apache.log.Priority;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.output.io.FileTarget;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Implementation of a Avalon logger.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 */
public class AvalonLogChute implements LogChute
{
    private Logger logger = null;
    private RuntimeServices rsvc = null;

    public void init(RuntimeServices rs) throws Exception
    {
        this.rsvc = rs;

        // if a logger is specified, we will use this instead of the default
        String name = (String)rsvc.getProperty("runtime.log.logsystem.avalon.logger");
        if (name != null)
        {
            this.logger = Hierarchy.getDefaultHierarchy().getLoggerFor(name);
        } 
        else 
        {
            // use the toString() of RuntimeServices to make a unique logger
            logger = Hierarchy.getDefaultHierarchy().getLoggerFor(rsvc.toString());

            // if we have a file property, use it to create a FileTarget
            String file = (String)rsvc.getProperty(RuntimeConstants.RUNTIME_LOG);
            if (file != null && file.trim().length() > 0)
            {
                initTarget(file);
            }
        }
    }

    // creates a file target using the specified file name
    private void initTarget(String file) throws Exception
    {
        try
        {
            VelocityFormatter vf = 
                new VelocityFormatter("%{time} %{message}\\n%{throwable}");

            // make the target and keep the default behavior of not appending
            FileTarget target = new FileTarget(new File(file), false, vf);

            logger.setPriority(Priority.DEBUG);
            logger.setLogTargets(new LogTarget[] { target });
            log(DEBUG_ID, "AvalonLogChute initialized using file '"+file+'\'');
        }
        catch (IOException ioe)
        {
            rsvc.getLog().warn("Unable to create log file for AvalonLogChute", ioe);
            throw new Exception("Error configuring AvalonLogChute : " + ioe);
        }
    }

    /**
     * @deprecated This method should not be used. It is here only to provide
     *             backwards compatibility for the deprecated AvalonLogSystem
     *             class, in case anyone used it and this method directly.
     */
    public void init(String file) throws Exception
    {
        logger = Hierarchy.getDefaultHierarchy().getLoggerFor(rsvc.toString());
        initTarget(file);
        // nag the theoretical user
        log(WARN_ID, "You shouldn't be using the init(String file) method!");
    }
    
    /**
     *  logs messages
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void log(int level, String message)
    {
        /*
         * based on level, call the right logger method
         * and prefix with the appropos prefix
         */
        switch (level) 
        {
            case LogChute.WARN_ID:
                logger.warn( RuntimeConstants.WARN_PREFIX + message );
                break;
            case LogChute.INFO_ID:
                logger.info( RuntimeConstants.INFO_PREFIX + message);
                break;
            case LogChute.DEBUG_ID:
                logger.debug( RuntimeConstants.DEBUG_PREFIX + message);
                break;
            case LogChute.TRACE_ID:
                logger.debug(RuntimeConstants.TRACE_PREFIX + message);
                break;
            case LogChute.ERROR_ID:
                logger.error(RuntimeConstants.ERROR_PREFIX + message);
                break;
            default:
                logger.info( message);
                break;
        }
    }

    /**
     *  logs messages and error
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void log(int level, String message, Throwable t)
    {
        switch (level) 
        {
            case LogChute.WARN_ID:
                logger.warn(RuntimeConstants.WARN_PREFIX + message, t);
                break;
            case LogChute.INFO_ID:
                logger.info(RuntimeConstants.INFO_PREFIX + message, t);
                break;
            case LogChute.DEBUG_ID:
                logger.debug(RuntimeConstants.DEBUG_PREFIX + message, t);
                break;
            case LogChute.TRACE_ID:
                logger.debug(RuntimeConstants.TRACE_PREFIX + message, t);
                break;
            case LogChute.ERROR_ID:
                logger.error(RuntimeConstants.ERROR_PREFIX + message, t);
                break;
            default:
                logger.info(message, t);
                break;
        }
    }

    /**
     * Checks to see whether the specified level is enabled.
     */
    public boolean isLevelEnabled(int level)
    {
        switch (level)
        {
            // For Avalon, no Trace exists. Log at debug level.
            case LogChute.TRACE_ID:
            case LogChute.DEBUG_ID:
                return logger.isDebugEnabled();
            case LogChute.INFO_ID:
                return logger.isInfoEnabled();
            case LogChute.WARN_ID:
                return logger.isWarnEnabled();
            case LogChute.ERROR_ID:
                return logger.isErrorEnabled();
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
        logger.unsetLogTargets();
    }

}
