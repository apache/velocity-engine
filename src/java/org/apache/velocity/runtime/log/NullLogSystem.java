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
 *  Logger used in case of failure. Does nothing.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: NullLogSystem.java,v 1.1.10.1 2004/03/03 23:22:56 geirm Exp $
 */
public class NullLogSystem implements LogSystem
{
    public NullLogSystem()
    {
    }

    public void init( RuntimeServices rs )
        throws Exception
    {
    }
    
    /**
     *  logs messages to the great Garbage Collector
     *  in the sky
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void logVelocityMessage(int level, String message)
    {
    }
}
