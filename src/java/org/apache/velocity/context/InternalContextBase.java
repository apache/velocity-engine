package org.apache.velocity.context;

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
 * @version $Id: InternalContextBase.java,v 1.8.12.1 2004/03/03 23:22:54 geirm Exp $
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

