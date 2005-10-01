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

import org.apache.velocity.runtime.RuntimeLogger;

/**
 * A temporary RuntimeLogger wrapper to make the deprecation
 * of UberspectLoggable.setRuntimeLogger(RuntimeLogger) feasible.
 * This overrides all Log methods, either throwing 
 * UnsupportedOperationExceptions or passing things off to the
 * theoretical RuntimeLogger used to create it.  Oh, and all the
 * is<Level>Enabled() methods return true.  Of course, ideally
 * there is no one out there who actually created their own
 * RuntimeLogger instance to use with UberspectLoggable.setRuntimeLogger()
 * and this class will therefore never be used.  But it's here just in case.
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 * @deprecated This will be removed along with the RuntimeLogger interface.
 */
public class RuntimeLoggerLog extends Log
{

    private RuntimeLogger rlog;

    /**
     * Creates a new Log that wraps a PrimordialLogChute.
     */
    public RuntimeLoggerLog(RuntimeLogger rlog)
    {
        if (rlog == null)
        {
            throw new NullPointerException("RuntimeLogger cannot be null!");
        }
        this.rlog = rlog;
    }

    protected void setLogChute(LogChute newLogChute)
    {
        throw new UnsupportedOperationException("RuntimeLoggerLog does not support this method.");
    }

    protected LogChute getLogChute()
    {
        throw new UnsupportedOperationException("RuntimeLoggerLog does not support this method.");
    }

    protected void setShowStackTraces(boolean showStacks)
    {
        throw new UnsupportedOperationException("RuntimeLoggerLog does not support this method.");
    }

    public boolean getShowStackTraces()
    {
        throw new UnsupportedOperationException("RuntimeLoggerLog does not support this method.");
    }

    /**
     * Returns true.
     */
    public boolean isTraceEnabled()
    {
        return true;
    }

    public void trace(Object message)
    {
        debug(message);
    }

    public void trace(Object message, Throwable t)
    {
        debug(message, t);
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public void debug(Object message)
    {
        rlog.debug(message);
    }

    public void debug(Object message, Throwable t)
    {
        rlog.debug(message);
        rlog.debug(t);
    }

    public boolean isInfoEnabled()
    {
        return true;
    }

    public void info(Object message)
    {
        rlog.info(message);
    }

    public void info(Object message, Throwable t)
    {
        rlog.info(message);
        rlog.info(t);
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public void warn(Object message)
    {
        rlog.warn(message);
    }

    public void warn(Object message, Throwable t)
    {
        rlog.warn(message);
        rlog.warn(t);
    }

    public boolean isErrorEnabled()
    {
        return true;
    }

    public void error(Object message)
    {
        rlog.error(message);
    }

    public void error(Object message, Throwable t)
    {
        rlog.error(message);
        rlog.error(t);
    }

}
