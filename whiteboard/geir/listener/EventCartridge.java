package org.apache.velocity.context;

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

import org.apache.velocity.context.InternalHousekeepingContext;
import org.apache.velocity.context.Context;

import java.util.ArrayList;

/**
 *  'Package' of event handlers...
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: EventCartridge.java,v 1.1 2001/04/06 11:25:52 geirm Exp $
 */
public class EventCartridge
{
    private ArrayList riehList = null;
    private Object[] riehArr = null;

    private ArrayList nsehList = null;
    private Object[] nsehArr = null;

    private ArrayList nrehList = null;
    private Object[] nrehArr = null;

    /**
     *  called by client to add an event handler.  For multiple
     *  calls with the same type, they stack and are called
     *  in sequence when invoked
     */
    public boolean addEventHandler( EventHandler ev )
        throws Exception
    {
        if (ev == null)
        {
            return false;
        }

        boolean found = false;

        if ( ev instanceof ReferenceInsertionEventHandler)
        {
            /*
             *  if we don't have one, make one
             */

            if ( riehList == null)
            {
                riehList = new ArrayList();
            }

            /*
             *  add to end
             */

            riehList.add( ev );
            riehArr = riehList.toArray();

            found = true;
        }
        
        if ( ev instanceof NullReferenceEventHandler )
        {
            /*
             *  if we don't have one, make one
             */

            if ( nrehList == null)
            {
                nrehList = new ArrayList();
            }

            /*
             *  add to end
             */

            nrehList.add( ev );
            nrehArr = nrehList.toArray();

            found = true;
        }


        if ( ev instanceof NullSetEventHandler )
        {
            /*
             *  if we don't have one, make one
             */

            if ( nsehList == null)
            {
                nsehList = new ArrayList();
            }

            /*
             *  add to end
             */

            nsehList.add( ev );
            nsehArr = nsehList.toArray();

            found = true;
        }
        
        if(!found)
        {
            /*
             *  guess we don't support that kind of event handler
             */

            throw new Exception("Unsupported handler class : " + ev.getClass());
        }

        return true;
    }

    public Object[] getReferenceInsertionHandlerArray()
    {
        return riehArr;
    }

    public Object[] getNullReferenceHandlerArray()
    {
        return nrehArr;
    }


    public Object[] getNullSetEventHandlerArray()
    {
        return nsehArr;
    }

    public boolean attachToContext( Context context )
    {
        
        if (  context instanceof InternalHousekeepingContext )
        {         
            InternalHousekeepingContext icb = (InternalHousekeepingContext) context;

            icb.attachEventCartridge( this );

            return true;
        }
        else
        {
            return false;
        }
    }
}
