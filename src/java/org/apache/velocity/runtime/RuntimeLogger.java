package org.apache.velocity.runtime;

/*
 * Copyright 2002,2004 The Apache Software Foundation.
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

/**
 * Interface for internal runtime logging services that are needed by the
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magusson Jr.</a>
 * @version $Id: RuntimeLogger.java,v 1.1.4.1 2004/03/03 23:22:55 geirm Exp $
 */
public interface RuntimeLogger
{
    /**
     * Log a warning message.
     *
     * @param Object message to log
     */
    public void warn(Object message);

    /**
     * Log an info message.
     *
     * @param Object message to log
     */
    public  void info(Object message);

    /**
     * Log an error message.
     *
     * @param Object message to log
     */
    public void error(Object message);

    /**
     * Log a debug message.
     *
     * @param Object message to log
     */
    public void debug(Object message);
}
