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

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import java.lang.reflect.Method;

/**
 * This basic function of this class is to return a Method
 * object for a particular class given the name of a method
 * and the parameters to the method in the form of an Object[]
 *
 * The first time the Introspector sees a
 * class it creates a class method map for the
 * class in question. Basically the class method map
 * is a Hastable where Method objects are keyed by a
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
 * and stored for
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class IntrospectorBase
{
    /**
     * Holds the method maps for the classes we know about, keyed by
     * Class object.
     */
    private final Map classMethodMaps = new HashMap();

    /**
     * Holds the qualified class names for the classes
     * we hold in the classMethodMaps hash
     */
    private final Set cachedClassNames = new HashSet();

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

        synchronized(classMethodMaps)
        {
            classMap = (ClassMap)classMethodMaps.get(c);

            /*
             *  if we don't have this, check to see if we have it
             *  by name.  if so, then we have a classloader change
             *  so dump our caches.
             */

            if (classMap == null)
            {
                if (cachedClassNames.contains(c.getName()))
                {
                    /*
                     * we have a map for a class with same name, but not
                     * this class we are looking at.  This implies a
                     * classloader change, so dump
                     */
                    clearCache();
                }

                classMap = createClassMap(c);
            }
        }

        return classMap.findMethod(name, params);
    }

    /**
     * Creates a class map for specific class and registers it in the
     * cache.  Also adds the qualified name to the name-&gt;class map
     * for later Classloader change detection.
     * @param c The class for which the class map gets generated.
     * @return A ClassMap object.
     */
    protected ClassMap createClassMap(final Class c)
    {
        ClassMap classMap = new ClassMap(c);
        classMethodMaps.put(c, classMap);
        cachedClassNames.add(c.getName());

        return classMap;
    }

    /**
     * Clears the classmap and classname caches.
     */
    protected void clearCache()
    {
	/*
	 * classes extending IntrospectorBase can request these objects through the
	 * protected getters. If we swap them out with new objects, the base class
	 * and the extended class can actually how two different objects. Don't do this.
	 * Make the members final and use clear() to reset the cache.
	 */
        classMethodMaps.clear();
        cachedClassNames.clear();
    }

    /**
     * Access to the classMethodMaps map.
     *
     * @return The classMethodsMaps HashMap.
     */
    protected Map getClassMethodMaps()
    {
        return classMethodMaps;
    }

    /**
     * Access to the list of cached class names.
     *
     * @return A set of names cached.
     */
    protected Set getCachedClassNames()
    {
        return cachedClassNames;
    }
}
