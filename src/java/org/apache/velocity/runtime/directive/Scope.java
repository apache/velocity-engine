package org.apache.velocity.runtime.directive;

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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This handles context scoping and metadata for directives.
 *
 * @author Nathan Bubna
 * @version $Id$
 */
public class Scope extends AbstractMap
{
    private Map storage;
    private Object replaced;
    private Scope parent;
    protected final Object owner;

    public Scope(Object owner, Object previous)
    {
        this.owner = owner;
        if (previous != null)
        {
            try
            {
                this.parent = (Scope)previous;
            }
            catch (ClassCastException cce)
            {
                this.replaced = previous;
            }
        }
    }

    private Map getStorage()
    {
        if (storage == null)
        {
            storage = new HashMap();
        }
        return storage;
    }

    public Set entrySet()
    {
        return getStorage().entrySet();
    }

    public Object get(Object key)
    {
        Object o = super.get(key);
        if (o == null && parent != null && !containsKey(key))
        {
            return parent.get(key);
        }
        return o;
    }

    public Object put(Object key, Object value)
    {
        return getStorage().put(key, value);
    }

    /**
     * Allows #stop to easily trigger the proper StopCommand for this scope.
     */
    protected void stop()
    {
        throw new StopCommand(owner);
    }

    /**
     * Returns the number of control arguments of this type
     * that are stacked up.  This is the distance between this
     * instance and the topmost instance, plus one. This value
     * will never be negative or zero.
     */
    public int getDepth()
    {
        if (parent == null)
        {
            return 1;
        }
        return parent.getDepth() + 1;
    }

    /**
     * Returns the topmost parent control reference, retrieved
     * by simple recursion on {@link #getParent}.
     */
    public Scope getTopmost()
    {
        if (parent == null)
        {
            return this;
        }
        return parent.getTopmost();
    }

    /**
     * Returns the parent control reference overridden by the placement
     * of this instance in the context.
     */
    public Scope getParent()
    {
        return parent;
    }

    /**
     * Returns the user's context reference overridden by the placement
     * of this instance in the context.  If there was none (as is hoped),
     * then this will return null.  This never returns parent controls;
     * those are returned by {@link #getParent}.
     */
    public Object getReplaced()
    {
        if (replaced == null && parent != null)
        {
            return parent.getReplaced();
        }
        return replaced;
    }

}
