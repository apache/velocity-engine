package org.apache.velocity.app.event;

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

import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.context.Context;

/**
 *  'Package' of event handlers...
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:j_a_fernandez@yahoo.com">Jose Alberto Fernandez</a>
 * @version $Id: EventCartridge.java,v 1.3.4.1 2004/03/03 23:22:53 geirm Exp $
 */
public class EventCartridge implements ReferenceInsertionEventHandler,
                                       NullSetEventHandler,
                                       MethodExceptionEventHandler
{
    private ReferenceInsertionEventHandler rieh = null;
    private NullSetEventHandler nseh = null;
    private MethodExceptionEventHandler meeh = null;

    /**
     *  Adds an event handler(s) to the Cartridge.  This method
     *  will find all possible event handler interfaces supported
     *  by the passed in object.
     *
     *  @param ev object impementing a valid EventHandler-derived interface
     *  @return true if a supported interface, false otherwise or if null
     */
    public boolean addEventHandler( EventHandler ev )
    {
        if (ev == null)
        {
            return false;
        }
        
        boolean found = false;

        if ( ev instanceof ReferenceInsertionEventHandler)
        {
            rieh = (ReferenceInsertionEventHandler) ev;
            found = true;
        }
       
        if ( ev instanceof NullSetEventHandler )
        {
            nseh = (NullSetEventHandler) ev;
            found = true;
        }

        if ( ev instanceof MethodExceptionEventHandler )
        {
            meeh = (MethodExceptionEventHandler) ev;
            found = true;
        }
 
        return found;
    }
    
    /**
     *  Removes an event handler(s) from the Cartridge.  This method
     *  will find all possible event handler interfaces supported
     *  by the passed in object and remove them.
     *
     *  @param ev object impementing a valid EventHandler-derived interface
     *  @return true if a supported interface, false otherwise or if null
     */
    public boolean removeEventHandler(EventHandler ev)
    {
        if ( ev == null )
        {
            return false;
        }

        boolean found = false;
        
        if (ev == rieh) 
        {
            rieh = null;
            found = true;
        }
	
        if (ev == nseh) 
        {
            nseh = null;
            found = true;
        }

        if (ev == meeh) 
        {
            meeh = null;
            found = true;
        }

        return found;
    }

    /**
     *  Implementation of ReferenceInsertionEventHandler method
     *  <code>referenceInsert()</code>.
     *
     *  Called during Velocity merge before a reference value will
     *  be inserted into the output stream.
     *
     *  @param reference reference from template about to be inserted
     *  @param value  value about to be inserted (after toString() )
     *  @return Object on which toString() should be called for output.
     */
    public Object referenceInsert( String reference, Object value  )
    {
        if (rieh == null)
        {
            return value;
        }

        return rieh.referenceInsert( reference, value );
    }

    /**
     *  Implementation of NullSetEventHandler method
     *  <code>shouldLogOnNullSet()</code>.
     *
     *  Called during Velocity merge to determine if when
     *  a #set() results in a null assignment, a warning
     *  is logged.
     *
     *  @param reference reference from template about to be inserted
     *  @return true if to be logged, false otherwise
     */
    public boolean shouldLogOnNullSet( String lhs, String rhs )
    {
        if ( nseh == null)
        {
            return true;
        }

        return nseh.shouldLogOnNullSet( lhs, rhs );
    }
    
    /**
     *  Implementation of MethodExceptionEventHandler  method
     *  <code>methodException()</code>.
     *
     *  Called during Velocity merge if a reference is null
     *
     *  @param claz  Class that is causing the exception
     *  @param method method called that causes the exception
     *  @param e Exception thrown by the method
     *  @return Object to return as method result
     *  @throws exception to be wrapped and propogated to app  
     */
    public Object methodException( Class claz, String method, Exception e )
        throws Exception
    {
        /*
         *  if we don't have a handler, just throw what we were handed
         */
        if (meeh == null)
        {
            throw e;
        }

        /*
         *  otherwise, call it..
         */
        return meeh.methodException( claz, method, e );
    }
    
    /**
     *  Attached the EventCartridge to the context
     *
     *  Final because not something one should mess with lightly :)
     *
     *  @param context context to attach to
     *  @return true if successful, false otherwise
     */
    public final boolean attachToContext( Context context )
    {
        if (  context instanceof InternalEventContext )
        {         
            InternalEventContext iec = (InternalEventContext) context;

            iec.attachEventCartridge( this );

            return true;
        }
        else
        {
            return false;
        }
    }
}
