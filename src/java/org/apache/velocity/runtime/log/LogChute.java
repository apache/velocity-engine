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

import org.apache.velocity.runtime.RuntimeServices;

/**
 * Base interface that logging systems need to implement. This
 * is the blessed descendant of the old LogSystem interface.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 */
public interface LogChute
{
    /**
     * ID for trace messages.
     */
    public final static int TRACE_ID = -1;

    /**
     * ID for debug messages.
     */
    public final static int DEBUG_ID = 0;

    /** 
     * ID for info messages.
     */
    public final static int INFO_ID = 1;
    
    /** 
     * ID for warning messages.
     */
    public final static int WARN_ID = 2;

    /** 
     * ID for error messages.
     */
    public final static int ERROR_ID = 3;

    /**
     * Initializes this LogChute.
     */
    public void init(RuntimeServices rs) throws Exception;

    /**
     * Send a log message from Velocity.
     */
    public void log(int level, String message);

    /**
     * Send a log message from Velocity along with an exception or error
     */
    public void log(int level, String message, Throwable t);

    /**
     * Tell whether or not a log level is enabled.
     */
    public boolean isLevelEnabled(int level);

}
