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

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

/**
 *  This is a special, internal-use-only context implementation to be
 *  used for the #evaluate directive.
 *
 *  We use this context to chain the existing context, preventing any changes
 *  from impacting the parent context.  By separating this context into a 
 *  separate class it also allows for the future possibility of changing
 *  the context behavior for the #evaluate directive.
 *  
 *  Note that the context used to store values local to #evaluate()
 *  is user defined but defaults to {@link VelocityContext}.
 *
 *  @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 *  @version $Id$
 *  @since 1.6
 */
public class EvaluateContext implements InternalContextAdapter
{
    /** container for any local items */
    Context localContext;

    /** the base context store.  This is the 'global' context */
    InternalContextAdapter innerContext = null;

    boolean allowRendering = true;
    
     /**
     *  CTOR, wraps an ICA
     * @param inner context for parent template
     * @param rsvc 
     */
    public EvaluateContext( InternalContextAdapter  inner, RuntimeServices rsvc )
    {
        innerContext = inner;
        initContext(rsvc);
    }

    /**
     * Initialize the context based on user-configured class 
     * @param rsvc
     */
    private void initContext( RuntimeServices rsvc )
    {
        String contextClass = rsvc.getString(RuntimeConstants.EVALUATE_CONTEXT_CLASS);

        if (contextClass != null && contextClass.length() > 0)
        {
            Object o = null;

            try
            {
                o = ClassUtils.getNewInstance( contextClass );
            }
            catch (ClassNotFoundException cnfe)
            {
                String err = "The specified class for #evaluate() context (" + contextClass
                + ") does not exist or is not accessible to the current classloader.";
                rsvc.getLog().error(err);
                throw new RuntimeException(err,cnfe);
            }
            catch (Exception e)
            {
                String err = "The specified class for #evaluate() context (" + contextClass
                + ") can not be loaded.";
                rsvc.getLog().error(err,e);
                throw new RuntimeException(err);
            }

            if (!(o instanceof Context))
            {                
                String err = "The specified class for #evaluate() context (" + contextClass
                + ") does not implement " + Context.class.getName() + ".";
                rsvc.getLog().error(err);
                throw new RuntimeException(err);
            }
            
            localContext = (Context) o; 

        }
        else
        {
            String err = "No class specified for #evaluate() context.";
            rsvc.getLog().error(err);
            throw new RuntimeException(err);
        }
        
    }
    
    /**
     *  Return the inner / user context.
     * @return The inner / user context.
     */
    public Context getInternalUserContext()
    {
        return innerContext.getInternalUserContext();
    }

    /**
     * @see org.apache.velocity.context.InternalWrapperContext#getBaseContext()
     */
    public InternalContextAdapter getBaseContext()
    {
        return innerContext.getBaseContext();
    }

    /**
     *  Put method also stores values in local scope 
     *
     *  @param key name of item to set
     *  @param value object to set to key
     *  @return old stored object
     */
    public Object put(String key, Object value)
    {
        /*
         *  just put in the local context
         */
        return localContext.put(key, value);

    }

    /**
     *  Retrieves from local or global context.
     *
     *  @param key name of item to get
     *  @return  stored object or null
     */
    public Object get( String key )
    {
        /*
         *  always try the local context then innerContext
         */

        Object o = localContext.get( key );

        if ( o == null)
        {
            o = innerContext.get( key );
        }

        return o;
    }

    /**
     * @see org.apache.velocity.context.Context#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
        return localContext.containsKey(key) || innerContext.containsKey(key);
    }

    /**
     * @see org.apache.velocity.context.Context#getKeys()
     */
    public Object[] getKeys()
    {
        Set keys = new HashSet();
        Object[] localKeys = localContext.getKeys();
        for (int i=0; i < localKeys.length; i++)
        {
            keys.add(localKeys[i]);
        }
        
        Object[] innerKeys = innerContext.getKeys();
        for (int i=0; i < innerKeys.length; i++)
        {
            keys.add(innerKeys[i]);
        }
        return keys.toArray();
    }

    /**
     * @see org.apache.velocity.context.Context#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        return localContext.remove( key );
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#pushCurrentTemplateName(java.lang.String)
     */
    public void pushCurrentTemplateName( String s )
    {
        innerContext.pushCurrentTemplateName( s );
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#popCurrentTemplateName()
     */
    public void popCurrentTemplateName()
    {
        innerContext.popCurrentTemplateName();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getCurrentTemplateName()
     */
    public String getCurrentTemplateName()
    {
        return innerContext.getCurrentTemplateName();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getTemplateNameStack()
     */
    public Object[] getTemplateNameStack()
    {
        return innerContext.getTemplateNameStack();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#pushCurrentMacroName(java.lang.String)
     */
    public void pushCurrentMacroName( String s )
    {
        innerContext.pushCurrentMacroName( s );
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#popCurrentMacroName()
     */
    public void popCurrentMacroName()
    {
        innerContext.popCurrentMacroName();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getCurrentMacroName()
     */
    public String getCurrentMacroName()
    {
        return innerContext.getCurrentMacroName();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getCurrentMacroCallDepth()
     */
    public int getCurrentMacroCallDepth()
    {
        return innerContext.getCurrentMacroCallDepth();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getMacroNameStack()
     */
    public Object[] getMacroNameStack()
    {
        return innerContext.getMacroNameStack();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#icacheGet(java.lang.Object)
     */
    public IntrospectionCacheData icacheGet( Object key )
    {
        return innerContext.icacheGet( key );
    }

    /**
     * Allows callers to explicitly put objects in the local context.
     * Objects added to the context through this method always end up
     * in the top-level context of possible wrapped contexts.
     *
     *  @param key name of item to set.
     *  @param value object to set to key.
     *  @return old stored object
     */
    public Object localPut(final String key, final Object value)
    {
        return localContext.put(key, value);
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#icachePut(java.lang.Object, org.apache.velocity.util.introspection.IntrospectionCacheData)
     */
    public void icachePut( Object key, IntrospectionCacheData o )
    {
        innerContext.icachePut( key, o );
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getAllowRendering()
     */
    public boolean getAllowRendering()
    {
       return allowRendering && innerContext.getAllowRendering();
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#setAllowRendering(boolean)
     */
    public void setAllowRendering(boolean v)
    {
        allowRendering = false;
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#setMacroLibraries(List)
     */
    public void setMacroLibraries(List macroLibraries)
    {
        innerContext.setMacroLibraries(macroLibraries);
    }
    
    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getMacroLibraries()
     */
    public List getMacroLibraries()
    {
        return innerContext.getMacroLibraries();
    }

    /**
     * @see org.apache.velocity.context.InternalEventContext#attachEventCartridge(org.apache.velocity.app.event.EventCartridge)
     */
    public EventCartridge attachEventCartridge( EventCartridge ec )
    {
        EventCartridge cartridge = innerContext.attachEventCartridge( ec );
        return cartridge;
    }

    /**
     * @see org.apache.velocity.context.InternalEventContext#getEventCartridge()
     */
    public EventCartridge getEventCartridge()
    {
        return innerContext.getEventCartridge();
    }


    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#setCurrentResource(org.apache.velocity.runtime.resource.Resource)
     */
    public void setCurrentResource( Resource r )
    {
        innerContext.setCurrentResource( r );
    }

    /**
     * @see org.apache.velocity.context.InternalHousekeepingContext#getCurrentResource()
     */
    public Resource getCurrentResource()
    {
        return innerContext.getCurrentResource();
    }
}




