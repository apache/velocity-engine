package org.apache.velocity.runtime.log;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.runtime.RuntimeServices;

/**
 *  Pre-init logger.  I believe that this was suggested by
 *  Carsten Ziegeler <cziegeler@sundn.de> and
 *  Jeroen C. van Gelderen.  If this isn't correct, let me
 *  know as this was a good idea...
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id$
 * @since 1.5
 */
class HoldingLogChute
    implements LogChute
{
    private List<Object[]> pendingMessages = new ArrayList<Object[]>();

    private volatile boolean transferring = false;

    /**
     * @see org.apache.velocity.runtime.log.LogChute#init(org.apache.velocity.runtime.RuntimeServices)
     */
    public void init( RuntimeServices rs )
    {
    }

    /**
     * Logs messages. All we do is store them until 'later'.
     *
     * @param level severity level
     * @param message complete error message
     */
    public synchronized void log( int level, String message )
    {
        if ( !transferring )
        {
            Object[] data = new Object[2];
            data[0] = new Integer( level );
            data[1] = message;
            pendingMessages.add( data );
        }
    }

    /**
     * Logs messages and errors. All we do is store them until 'later'.
     *
     * @param level severity level
     * @param message complete error message
     * @param t the accompanying java.lang.Throwable
     */
    public synchronized void log( int level, String message, Throwable t )
    {
        if ( !transferring )
        {
            Object[] data = new Object[3];
            data[0] = new Integer( level );
            data[1] = message;
            data[2] = t;
            pendingMessages.add( data );
        }
    }

    /**
     * @see org.apache.velocity.runtime.log.LogChute#isLevelEnabled(int)
     */
    public boolean isLevelEnabled( int level )
    {
        return true;
    }

    /**
     * Dumps the log messages this chute is holding into a new chute
     * @param newChute
     */
    public synchronized void transferTo( LogChute newChute )
    {
        if ( !transferring && !pendingMessages.isEmpty() )
        {
            // let the other methods know what's up
            transferring = true;

            for ( Object[] data : pendingMessages )
            {
                int level = ( (Integer) data[0] ).intValue();
                String message = (String) data[1];
                if ( data.length == 2 )
                {
                    newChute.log( level, message );
                }
                else
                {
                    newChute.log( level, message, (Throwable) data[2] );
                }

            }
        }
    }

}
