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

import java.util.Vector;
import java.util.Enumeration;

import org.apache.velocity.runtime.RuntimeServices;

/**
 *  Pre-init logger.  I believe that this was suggested by
 *  Carsten Ziegeler <cziegeler@sundn.de> and 
 *  Jeroen C. van Gelderen.  If this isn't correct, let me
 *  know as this was a good idea... 
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: PrimordialLogSystem.java,v 1.4.4.1 2004/03/03 23:22:56 geirm Exp $
 */
public class PrimordialLogSystem implements LogSystem
{
    private Vector pendingMessages = new Vector();    
    private RuntimeServices rsvc = null;

    /**
     *  default CTOR.
     */
    public PrimordialLogSystem()
    {
    }

    public void init( RuntimeServices rs )
        throws Exception
    {
        rsvc = rs;
    }
    
    /**
     *  logs messages.  All we do is store them until
     *   'later'.
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void logVelocityMessage(int level, String message)
    {
        synchronized( this )
        {
            Object[] data = new Object[2];
            data[0] = new Integer(level);
            data[1] = message;
            pendingMessages.addElement(data);
        }
    }
    
    /**
     * dumps the log messages this logger is holding into a new logger
     */
    public void dumpLogMessages( LogSystem newLogger )
    {
        synchronized( this )
        {
            if ( !pendingMessages.isEmpty())
            {
                /*
                 *  iterate and log each individual message...
                 */
            
                for( Enumeration e = pendingMessages.elements(); e.hasMoreElements(); )
                {
                    Object[] data = (Object[]) e.nextElement();
                    newLogger.logVelocityMessage(((Integer) data[0]).intValue(), (String) data[1]);
                }
            }    
        }
    }    
}
