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

import java.util.HashMap;
import java.util.Stack;
import java.io.Serializable;

import org.apache.velocity.util.introspection.IntrospectionCacheData;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.runtime.resource.Resource;

/**
 *  class to encapsulate the 'stuff' for internal operation of velocity.  
 *  We use the context as a thread-safe storage : we take advantage of the
 *  fact that it's a visitor  of sorts  to all nodes (that matter) of the 
 *  AST during init() and render().
 *  Currently, it carries the template name for namespace
 *  support, as well as node-local context data introspection caching.
 *
 *  Note that this is not a public class.  It is for package access only to
 *  keep application code from accessing the internals, as AbstractContext
 *  is derived from this.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: InternalContextBase.java,v 1.8 2001/05/20 19:44:34 geirm Exp $
 */
class InternalContextBase implements InternalHousekeepingContext, InternalEventContext,  Serializable
{
    /**
     *  cache for node/context specific introspection information
     */
    private HashMap introspectionCache = new HashMap(33);
    
    /**
     *  Template name stack. The stack top contains the current template name.
     */
    private Stack templateNameStack = new Stack();

    /**
     *  EventCartridge we are to carry.  Set by application
     */
    private EventCartridge eventCartridge = null;

    /**
     *  Current resource - used for carrying encoding and other
     *  information down into the rendering process
     */
    private Resource currentResource = null;

    /**
     *  set the current template name on top of stack
     *
     *  @param s current template name
     */
    public void pushCurrentTemplateName( String s )
    {
        templateNameStack.push(s);
        return;
    }

    /**
     *  remove the current template name from stack
     */
    public void popCurrentTemplateName()
    {
        templateNameStack.pop();
        return;
    }
     
    /**
     *  get the current template name
     *
     *  @return String current template name
     */
    public String getCurrentTemplateName()
    {
        if ( templateNameStack.empty() )
            return "<undef>";
        else
            return (String) templateNameStack.peek();
    }

    /**
     *  get the current template name stack
     *
     *  @return Object[] with the template name stack contents.
     */
    public Object[] getTemplateNameStack()
    {
        return templateNameStack.toArray();
    }

    /**
     *  returns an IntrospectionCache Data (@see IntrospectionCacheData)
     *  object if exists for the key
     *
     *  @param key  key to find in cache
     *  @return cache object
     */
    public IntrospectionCacheData icacheGet( Object key )
    {
        return ( IntrospectionCacheData ) introspectionCache.get( key );
    }
     
    /**
     *  places an IntrospectionCache Data (@see IntrospectionCacheData)
     *  element in the cache for specified key
     *
     *  @param key  key 
     *  @param o  IntrospectionCacheData object to place in cache
     */
    public void icachePut( Object key, IntrospectionCacheData o )
    {
        introspectionCache.put( key, o );
    }

    public void setCurrentResource( Resource r )
    {
        currentResource = r;
    }

    public Resource getCurrentResource()
    {
        return currentResource;
    }

    public EventCartridge attachEventCartridge( EventCartridge ec )
    {
        EventCartridge temp = eventCartridge;

        eventCartridge = ec;
        
        return temp;
    }

    public EventCartridge getEventCartridge()
    {
        return eventCartridge;
    }
}

