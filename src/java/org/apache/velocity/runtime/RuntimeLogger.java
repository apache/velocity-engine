package org.apache.velocity.runtime;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

/**
 * Interface for internal runtime logging services. This will hopefully
 * be dissolved into the Log class at some point soon.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magusson Jr.</a>
 * @version $Id$
 * @deprecated This functionality has been taken over by the Log class
 */
public interface RuntimeLogger
{
    /**
     * @deprecated Use Log.warn(Object)
     */
    public void warn(Object message);

    /**
     * @deprecated Use Log.warn(Object)
     */
    public  void info(Object message);

    /**
     * @deprecated Use Log.warn(Object)
     */
    public void error(Object message);

    /**
     * @deprecated Use Log.warn(Object)
     */
    public void debug(Object message);
}
