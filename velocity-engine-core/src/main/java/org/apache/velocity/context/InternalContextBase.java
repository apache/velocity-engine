package org.apache.velocity.context;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.velocity.Template;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
 * @version $Id$
 */
class InternalContextBase implements InternalHousekeepingContext, InternalEventContext
{
    /**
     * Version Id for serializable
     */
    private static final long serialVersionUID = -245905472770843470L;

    /**
     *  cache for node/context specific introspection information
     */
    private Map<Object, IntrospectionCacheData> introspectionCache = new HashMap<>(33);

    /**
     *  Template name stack. The stack top contains the current template name.
     */
    private Stack<String> templateNameStack = new Stack<>();

    /**
     *  Velocimacro name stack. The stack top contains the current macro name.
     */
    private Stack<String> macroNameStack = new Stack<>();

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
     *  List for holding the macro libraries. Contains the macro library
     *  template name as strings.
     */
    private List<Template> macroLibraries = null;

    /**
     *  set the current template name on top of stack
     *
     *  @param s current template name
     */
    @Override
    public void pushCurrentTemplateName(String s )
    {
        templateNameStack.push(s);
    }

    /**
     *  remove the current template name from stack
     */
    @Override
    public void popCurrentTemplateName()
    {
        templateNameStack.pop();
    }

    /**
     *  get the current template name
     *
     *  @return String current template name
     */
    @Override
    public String getCurrentTemplateName()
    {
        if ( templateNameStack.empty() )
            return "<undef>";
        else
            return templateNameStack.peek();
    }

    /**
     *  get the current template name stack
     *
     *  @return String[] with the template name stack contents.
     */
    @Override
    public String[] getTemplateNameStack()
    {
        return templateNameStack.toArray(new String[templateNameStack.size()]);
    }

    /**
     *  set the current macro name on top of stack
     *
     *  @param s current macro name
     */
    @Override
    public void pushCurrentMacroName(String s )
    {
        macroNameStack.push(s);
    }

    /**
     *  remove the current macro name from stack
     */
    @Override
    public void popCurrentMacroName()
    {
        macroNameStack.pop();
    }

    /**
     *  get the current macro name
     *
     *  @return String current macro name
     */
    @Override
    public String getCurrentMacroName()
    {
        if (macroNameStack.empty())
        {
            return "<undef>";
        }
        else
        {
            return macroNameStack.peek();
        }
    }

    /**
     *  get the current macro call depth
     *
     *  @return int current macro call depth
     */
    @Override
    public int getCurrentMacroCallDepth()
    {
        return macroNameStack.size();
    }

    /**
     *  get the current macro name stack
     *
     *  @return String[] with the macro name stack contents.
     */
    @Override
    public String[] getMacroNameStack()
    {
        return macroNameStack.toArray(new String[macroNameStack.size()]);
    }

    /**
     *  returns an IntrospectionCache Data (@see IntrospectionCacheData)
     *  object if exists for the key
     *
     *  @param key  key to find in cache
     *  @return cache object
     */
    @Override
    public IntrospectionCacheData icacheGet(Object key )
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
    @Override
    public void icachePut(Object key, IntrospectionCacheData o )
    {
        introspectionCache.put( key, o );
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#setCurrentResource(org.apache.velocity.runtime.resource.Resource)
     */
    @Override
    public void setCurrentResource(Resource r )
    {
        currentResource = r;
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getCurrentResource()
     */
    @Override
    public Resource getCurrentResource()
    {
        return currentResource;
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#setMacroLibraries(List)
     */
    @Override
    public void setMacroLibraries(List<Template> macroLibraries)
    {
        this.macroLibraries = macroLibraries;
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getMacroLibraries()
     */
    @Override
    public List<Template> getMacroLibraries()
    {
        return macroLibraries;
    }


    /**
     * @see org.apache.velocity.context.InternalEventContext#attachEventCartridge(org.apache.velocity.app.event.EventCartridge)
     */
    @Override
    public EventCartridge attachEventCartridge(EventCartridge ec )
    {
        EventCartridge temp = eventCartridge;

        eventCartridge = ec;

        return temp;
    }

    /**
     * @see org.apache.velocity.context.InternalEventContext#getEventCartridge()
     */
    @Override
    public EventCartridge getEventCartridge()
    {
        return eventCartridge;
    }
}
