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

import org.apache.velocity.runtime.RuntimeServices;

/**
 * Base interface that Logging systems need to implement.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: LogSystem.java,v 1.7.10.1 2004/03/03 23:22:56 geirm Exp $
 */
public interface LogSystem
{
    public final static boolean DEBUG_ON = true;

    /**
     * Prefix for debug messages.
     */
    public final static int DEBUG_ID = 0;

    /** 
     * Prefix for info messages.
     */
    public final static int INFO_ID = 1;
    
    /** 
     * Prefix for warning messages.
     */
    public final static int WARN_ID = 2;

    /** 
     * Prefix for error messages.
     */
    public final static int ERROR_ID = 3;

    /**
     *  init()
     */
    public void init( RuntimeServices rs ) throws Exception;

    /**
     * Send a log message from Velocity.
     */
    public void logVelocityMessage(int level, String message);
}
