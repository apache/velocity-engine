package org.apache.velocity;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
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

import java.util.HashMap;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

/**
 *  General purpose implemention of the application Context 
 *  interface for general application use.  This class should 
 *  be used in place of the original Context class.
 *
 *  This implementation uses a HashMap  (@see java.util.HashMap ) 
 *  for data storage.
 *
 *  This context implementation cannot be shared between threads
 *  without those threads synchronizing access between them, as 
 *  the HashMap is not synchronized, nor are some of the fundamentals
 *  of AbstractContext.  If you need to share a Context between 
 *  threads with simultaneous access for some reason, please create 
 *  your own and extend the interface Context 
 *  
 *  @see org.apache.velocity.context.Context
 *
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 *  @author <a href="mailto:fedor.karpelevitch@home.com">Fedor Karpelevitch</a>
 *  @version $Id: VelocityContext.java,v 1.4 2001/03/19 22:32:49 geirm Exp $
 */
public class VelocityContext extends AbstractContext implements Cloneable
{
    /**
     *  storage for key/value pairs 
     */
    private HashMap context = new HashMap();

    /** 
     * default contructor, does nothing 
     * interesting
     */
    public VelocityContext()
    {
        super();
    }

    /**
     *  Chaining constructor, used when you want to 
     *  wrap a context in another.  The inner context
     *  will be 'read only' - put() calls to the 
     *  wrapping context will only effect the outermost
     *  context
     *
     *  @param innerContext context impl to wrap
     */
    public VelocityContext( Context innerContext )
    {
        super( innerContext );
    }
 
    /**
     *  retrieves value for key from internal
     *  storage
     *
     *  @param key name of value to get
     *  @return value as object
     */
    public Object internalGet( String key )
    {
        return context.get( key );
    }        

    /**
     *  stores the value for key to internal
     *  storage
     *
     *  @param key name of value to store
     *  @param value value to store
     *  @return previous value of key as Object
     */
    public Object internalPut( String key, Object value )
    {
        return context.put( key, value );
    }

    /**
     *  determines if there is a value for the
     *  given key
     *
     *  @param key name of value to check
     *  @return true if non-null value in store
     */
    public  boolean internalContainsKey(Object key)
    {
        return context.containsKey( key );
    }

    /**
     *  returns array of keys
     *
     *  @return keys as []
     */
    public  Object[] internalGetKeys()
    {
        return context.keySet().toArray();
    }
    
    /**
     *  remove a key/value pair from the
     *  internal storage
     *
     *  @param key name of value to remove
     *  @return value removed
     */
    public  Object internalRemove(Object key)
    {
        return context.remove( key );
    }

    /**
     * Clones this context object
     * @return Object an instance of this Context
     */
    public Object clone()
    {
        VelocityContext clone = null;

        try
        {
            clone = (VelocityContext) super.clone();
            clone.context = (HashMap) context.clone();
        }
        catch (CloneNotSupportedException cnse)
        {
        }
        return clone;
    }

}

