
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
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.runtime.log.LogSystem;

/**
 * Implementation of a Simple logger to output messages to STDERR.
 *
 * @author    <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @version   $Id: StderrLogSystem.java,v 1.1.8.1 2004/03/04 00:18:28 geirm Exp $
 */
public class StderrLogSystem implements LogSystem
{
    /**
     * Empty constructor.
     */
    public StderrLogSystem()
    {
        // nothing to do
    }
    
    /**
     * Do the initialization (this logger does not do anything here).
     */
    public void init( RuntimeServices rs ) throws Exception
    {
        // nothing to do
    }
    
    /**
     * Does the acutal message logging.
     *
     * @param level    severity level
     * @param message  complete error message
     */
    public void logVelocityMessage( int level, String message )
    {
        switch ( level )
        {
        case LogSystem.WARN_ID:
            System.err.println( RuntimeConstants.WARN_PREFIX + message );
            break;
        case LogSystem.INFO_ID:
            System.err.println( RuntimeConstants.INFO_PREFIX + message );
            break;
        case LogSystem.DEBUG_ID:
            System.err.println( RuntimeConstants.DEBUG_PREFIX + message );
            break;
        case LogSystem.ERROR_ID:
            System.err.println( RuntimeConstants.ERROR_PREFIX + message );
            break;
        default:
            System.err.println( message );
            break;
        }
    }
}
