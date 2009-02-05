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

import java.util.HashMap;
import java.util.Map;

/**
 * Context for Velocity macro arguments.
 * 
 * This special context combines ideas of earlier VMContext and VMProxyArgs
 * by implementing routing functionality internally. This significantly
 * reduces memory allocation upon macro invocations.
 * Since the macro AST is now shared and RuntimeMacro directive is used,
 * the earlier implementation of precalculating VMProxyArgs would not work.
 */
public class ProxyVMContext extends ChainedInternalContextAdapter
{
    /** container for any local or constant macro arguments. Size must be power of 2. */
    Map localcontext = new HashMap(8, 0.8f);
    
    /** If we are operating in global or localscope */
    boolean localscope = true;
    
    /**
     * @param inner Velocity context for processing
     * @param rsvc RuntimeServices provides logging reference
     * @param localContextScope if true, all references are set to be local
     */
    public ProxyVMContext(InternalContextAdapter global, boolean localScopeContext)
    {
        super(global instanceof ProxyVMContext ? ((ProxyVMContext)global).getGlobal() : global);        
        localscope = localScopeContext;
    }

    /**
     * Get the global context from this ProxyVMContext
     * @return
     */
    private InternalContextAdapter getGlobal()
    {
      return innerContext;
    }
    
    /**
     * Impl of the Context.put() method.
     * 
     * @param key name of item to set
     * @param value object to set to key
     * @return old stored object
     */
    public Object put(final String key, final Object value)
    {
        if (localscope)    
          return localcontext.put(key, value);
        else
          return super.put(key, value);
    }

    /**
     * Allows callers to explicitly put objects in the local context, no matter what the
     * velocimacro.context.local setting says. Needed e.g. for loop variables in foreach.
     * 
     * @param key name of item to set.
     * @param value object to set to key.
     * @return old stored object
     */
    public Object localPut(final String key, final Object value)
    {
        return put(key, value);
    }

    /**
     * Implementation of the Context.get() method.  First checks
     * localcontext, then global context.
     * 
     * @param key name of item to get
     * @return stored object or null
     */
    public Object get(String key)
    {
        Object o = localcontext.get(key);
        if (o == null)
        {
            // Make sure this isn't the case of the key existing, but the value is null
            if (!localcontext.containsKey(key))
            {
                o = super.get(key);  
            }
        }
        return o;
    }

    /**
     * @see org.apache.velocity.context.Context#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key)
    {
      return localcontext.containsKey(key)
          || super.containsKey(key);
    }

    /**
     * @see org.apache.velocity.context.Context#getKeys()
     */
    public Object[] getKeys()
    {
        return localcontext.keySet().toArray();
    }

    /**
     * @see org.apache.velocity.context.Context#remove(java.lang.Object)
     */
    public Object remove(Object key)
    {
        if (localscope)
            return localcontext.remove(key);
        else
            return super.remove(key);
    }

}
