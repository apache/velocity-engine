package org.apache.velocity.runtime.log;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Vector;
import java.util.Enumeration;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 *  Pre-init logger.  I believe that this was suggested by
 *  Carsten Ziegeler <cziegeler@sundn.de> and 
 *  Jeroen C. van Gelderen.  If this isn't correct, let me
 *  know as this was a good idea... 
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: PrimordialLogSystem.java,v 1.2 2001/09/24 21:25:30 geirm Exp $
 */
public class PrimordialLogSystem implements LogSystem
{
    private  Vector pendingMessages = new Vector();    
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
