package org.apache.velocity.context;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

import java.util.Hashtable;
import java.util.Properties;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.File;
import java.util.Enumeration;

import org.apache.velocity.util.ArrayIterator;
import org.apache.velocity.context.InternalContextBase;

/**
 * This class is the abstract base class for all Velocity Context 
 * implementations.  Simply extend this class and implement the
 * abstract routines that access your preferred storage method.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: AbstractContext.java,v 1.4 2000/12/30 23:46:18 geirm Exp $
 */

public abstract class AbstractContext extends InternalContextBase  implements Context, Serializable
{
    /**
     *  we handle the context wrapping
     */
    private   Context  innerContext = null;
  
    /** implement to return a value from the context storage */
    public abstract Object internalGet( String key );

    /** implement to put a value into the context storage */
    public abstract Object internalPut( String key, Object value );

    /** implement to determine if a key is in the storage */
    public abstract boolean internalContainsKey(Object key);

    /** implement to return an object array of key strings from your storage */
    public abstract Object[] internalGetKeys();

    /** implement to remove an item from your storage */
    public abstract Object internalRemove(Object key);

    /**
     *  CTOR's
     */
    public AbstractContext()
    {
    }        

    public AbstractContext( Context inner )
    {
        innerContext = inner;
    }

    public boolean loadTools( String propsfilename, boolean doGlobal )
    {
        /*
         *  if we get a props file name, make a properties to call the 
         *  global toolsmith
         */

        try 
        {
            Properties p = null;

            if ( propsfilename != null)
            {
                p = new Properties();
                p.load(new FileInputStream( new File(propsfilename)));;
            }

            /*
             *  do it all in one place, the other loadTools()
             */

            return loadTools(p, doGlobal);

        }
        catch( Exception e)
        {
            return false;
        }
    }

    public boolean loadTools( Properties props, boolean doGlobal )
    {
        /*
         *  call the global toolsmith :)  and get the toolset
         *  comprised of globals if request, as well as locals
         *  if specified
         */

        /*
         *  NOTE : just to make it clear what I mean here - if we decide to implement
         *  tool management via the velocity.props file (for global tools), then that 
         *  tool management code should be used for both the global tools as well
         *  as dealing with tool sets the app wants to specify via the loadTool() method.
         *
         *  so an app can :
         *
         *  call loadTools() with props/filenam = null and doGlobal = true
         *    to get the global toolset into this context
         *
         *  call loadTools() with a valid props/filename and doGlobal = true
         *    to get the global toolset PLUS the toolset in the props/propsfile
         *    loaded into this context (by the toolmanager)
         *
         *  call loadTools() w/ valid props/filename and doGlobal = false
         *    to get the toolset in the props/propsfile loaded into this context
         *
         *  The following line is just to let it compile, until we resolve (maybe) how
         *  the global 'toolsmith' will work.
         */

        Hashtable h = new Hashtable(); // Runtime.toolsmith( props, doGlobal );

        /*
         *  iterate through the hash, adding tools to the Context
         */

        for( Enumeration e = h.keys(); e.hasMoreElements();  )
        {
            String reference = (String) e.nextElement();
            Object o = h.get( reference );

            internalPut( reference, o );
        }

        return true;
    }

    /**
     * Adds a name/value pair to the context.
     * 
     * @param key   The name to key the provided value with.
     * @param value The corresponding value.
     */
    public Object put(String key, Object value)
    {
        try
        {
            return internalPut(key, value);
        }
        catch (NullPointerException npe)
        {
            if (key == null)
            {
                org.apache.velocity.runtime.Runtime.error ("Context key was null! Value was: " + value);
            }
            else if (value == null)
            {
                org.apache.velocity.runtime.Runtime.error ("Context value was null! Key was: " + key);
            }

            return null;
        }
    }

    /**
     * Gets the value corresponding to the provided key from the context.
     *
     * @param key The name of the desired value.
     * @return    The value corresponding to the provided key.
     */
    public Object get(String key)
    {
        if (key == null)
        {
            org.apache.velocity.runtime.Runtime.debug ("Context key was null!");
        }

        Object o = internalGet( key );

        if (o == null && innerContext != null)
        {
            o = innerContext.get( key );
        }
            
        return o;
    }        

    /**
     * Indicates whether the specified key is in the context.
     *
     * @param key The key to look for.
     * @return    Whether the key is in the context.
     */
    public boolean containsKey(Object key)
    {
        if (key == null)
        {
            org.apache.velocity.runtime.Runtime.debug ("Context key was null!");
        }
        return internalContainsKey(key);
    }        

    /*
     * Get all the keys for the values in the context
     */
    public Object[] getKeys()
    {
        return internalGetKeys();
    }

    /**
     * Removes the value associated with the specified key from the context.
     *
     * @param key The name of the value to remove.
     * @return    The value that the key was mapped to, or <code>null</code> 
     *            if unmapped.
     */
    public Object remove(Object key)
    {
        if (key == null)
        {
            org.apache.velocity.runtime.Runtime.debug ("Context key was null!");
        }
        return internalRemove(key);
    }        

}
