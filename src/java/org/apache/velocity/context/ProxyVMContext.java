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
 * ProxyVMContext provides a context for a macro call frame and basically manages both
 * the local and global contexts. The local context is created for every call into a 
 * macro and destroyed when the macro exits.  The global context consists of the original 
 * users data and any variables defined in the global scope. The macro call
 * does not afffect the global context lifecycle.
 */
public class ProxyVMContext extends ChainedInternalContextAdapter
{
    /** container for any local or constant macro arguments. Size must be power of 2. */
    protected Map<String, Object> localcontext = new HashMap<String, Object>(8, 0.8f);
    
    /** If we are operating in global or localscope */
    boolean localscope = true;
        
    private Context globalContext = null;

    public ProxyVMContext(InternalContextAdapter context)
    {
        this(context, false);
    }

    /**
     * @param context the parent context
     * @param localScopeContext if true, all references are set to be local
     */
    public ProxyVMContext(InternalContextAdapter context, boolean localScopeContext)
    {
        super(context);
        localscope = localScopeContext;
        globalContext = context.getBaseContext();
    }
    
    /**
     * Impl of the Context.put() method.
     * 
     * @param key name of item to set
     * @param value object to set to key
     * @return old stored object
     */
    public Object put(String key, Object value)
    {
        if (localscope)    
          return localcontext.put(key, value);
        else
          return globalContext.put(key, value);
    }

    /**
     * Put a value into the appropriate context specified by 'scope'
     */
    public Object put(String key, Object value, Scope scope)
    {
        switch (scope)
        {
            case GLOBAL: return globalContext.put(key, value);
            case LOCAL: return localcontext.put(key, value);
            default: return put(key, value);  // DEFAULT scope
        }
    }

    /**
     * Returns a value associated with 'key' from the specified 'scope'
     */
    public Object get(String key, Scope scope)
    {
        switch (scope)
        {
            case GLOBAL: return globalContext.get(key);
            case LOCAL: return localcontext.get(key);
            default: return get(key);  // DEFAULT scope
        }      
    }
    
    /**
     * Returns true if the specified scope contains 'key'
     */
    public boolean containsKey(String key, Scope scope)
    {
        switch (scope)
        {
            case GLOBAL: return globalContext.containsKey(key);
            case LOCAL: return localcontext.containsKey(key);
            default: return containsKey(key);  // DEFAULT scope
        }            
    }
    
    /**
     * Implementation of the Context.get() method.  First checks
     * localcontext, then global context.
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
