package org.apache.velocity.util.introspection;

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

import java.lang.reflect.Method;

import org.apache.velocity.runtime.log.Log;

/**
 * Lookup a a Method object for a particular class given the name of a method
 * and its parameters.
 *
 * The first time the Introspector sees a
 * class it creates a class method map for the
 * class in question. Basically the class method map
 * is a Hashtable where Method objects are keyed by a
 * concatenation of the method name and the names of
 * classes that make up the parameters.
 *
 * For example, a method with the following signature:
 *
 * public void method(String a, StringBuffer b)
 *
 * would be mapped by the key:
 *
 * "method" + "java.lang.String" + "java.lang.StringBuffer"
 *
 * This mapping is performed for all the methods in a class
 * and stored for.
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public abstract class IntrospectorBase
	implements IntrospectorCacheListener
{
    /** Class logger */
    protected final Log log;

    /** The Introspector Cache */
    private final IntrospectorCache introspectorCache;
    
    /**
     * C'tor.
     */
    protected IntrospectorBase(final Log log)
    {
	this.log = log;
	introspectorCache = new IntrospectorCacheImpl(log); // TODO: Load that from properties.
	introspectorCache.addListener(this);
    }
    
    /**
     * Gets the method defined by <code>name</code> and
     * <code>params</code> for the Class <code>c</code>.
     *
     * @param c Class in which the method search is taking place
     * @param name Name of the method being searched for
     * @param params An array of Objects (not Classes) that describe the
     *               the parameters
     *
     * @return The desired Method object.
     * @throws IllegalArgumentException When the parameters passed in can not be used for introspection.
     * @throws MethodMap.AmbiguousException When the method map contains more than one match for the requested signature.
     */
    public Method getMethod(final Class c, final String name, final Object[] params)
            throws IllegalArgumentException,MethodMap.AmbiguousException
    {
        if (c == null)
        {
            throw new IllegalArgumentException ("class object is null!");
        }
        
        if (params == null)
        {
            throw new IllegalArgumentException("params object is null!");
        }

        ClassMap classMap = null;

        IntrospectorCache ic = getIntrospectorCache();
        
        synchronized(ic)
        {
            classMap = ic.get(c);

            if (classMap == null)
            {
                classMap = ic.put(c);
            }
        }

        return classMap.findMethod(name, params);
    }

    /**
     * Return the internal IntrospectorCache object.
     * 
     * @return The internal IntrospectorCache object.
     */
    protected IntrospectorCache getIntrospectorCache()
    {
	return introspectorCache;
    }
    
    /**
     * Clears the internal cache.
     * 
     * @deprecated Use getIntrospectorCache().clear();
     */
    protected void clearCache()
    {
        getIntrospectorCache().clear();
    }

    /**
     * Creates a class map for specific class and registers it in the
     * cache.  Also adds the qualified name to the name-&gt;class map
     * for later Classloader change detection.
     *
     * @param c The class for which the class map gets generated.
     * @return A ClassMap object.
     * 
     * @deprecated Use getIntrospectorCache().put(c);
     */
    protected ClassMap createClassMap(final Class c)
    {
        return getIntrospectorCache().put(c);
    }

    /**
     * Lookup a given Class object in the cache. If it does not exist, 
     * check whether this is due to a class change and purge the caches
     * eventually.
     *
     * @param c The class to look up.
     * @return A ClassMap object or null if it does not exist in the cache.
     * 
     * @deprecated Use getIntrospectorCache().get(c);
     */
    protected ClassMap lookupClassMap(final Class c)
    {
        return getIntrospectorCache().get(c);
    }
    
    /**
     * @see IntrospectorCacheListener#triggerClear()
     */
    public void triggerClear()
    {
    }
    
    /**
     * @see IntrospectorCacheListener#triggerGet(Class, ClassMap)
     */
    public void triggerGet(Class c, ClassMap classMap)
    {
    }

    /**
     * @see IntrospectorCacheListener#triggerPut(Class, ClassMap)
     */
    public void triggerPut(Class c, ClassMap classMap)
    {
    }
}
