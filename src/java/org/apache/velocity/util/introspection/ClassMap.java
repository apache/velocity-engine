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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.runtime.log.Log;

/**
 * A cache of introspection information for a specific class instance.
 * Keys {@link java.lang.reflect.Method} objects by a concatenation of the
 * method name and the names of classes that make up the parameters.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class ClassMap
{
    /** Set true if you want to debug the reflection code */
    private static final boolean debugReflection = false;

    /** Class logger */
    private final Log log;
    
    /**
     * Class passed into the constructor used to as
     * the basis for the Method map.
     */
    private final Class clazz;

    private final MethodCache methodCache;

    /**
     * Standard constructor
     * @param clazz The class for which this ClassMap gets constructed.
     */
    public ClassMap(final Class clazz, final Log log)
    {
        this.clazz = clazz;
        this.log = log;
        
        if (debugReflection && log.isDebugEnabled())
        {
            log.debug("=================================================================");
            log.debug("== Class: " + clazz);
        }
        
        methodCache = new MethodCache(log);
        
        populateMethodCache();

        if (debugReflection && log.isDebugEnabled())
        {
            log.debug("=================================================================");
        }
    }

    /**
     * Returns the class object whose methods are cached by this map.
     *
     * @return The class object whose methods are cached by this map.
     */
    public Class getCachedClass()
    {
        return clazz;
    }

    /**
     * Find a Method using the method name and parameter objects.
     *
     * @param name The method name to look up.
     * @param params An array of parameters for the method.
     * @return A Method object representing the method to invoke or null.
     * @throws MethodMap.AmbiguousException When more than one method is a match for the parameters.
     */
    public Method findMethod(final String name, final Object[] params)
            throws MethodMap.AmbiguousException
    {
        return methodCache.get(name, params);
    }

    /**
     * Populate the Map of direct hits. These
     * are taken from all the public methods
     * that our class, its parents and their implemented interfaces provide.
     */
    private void populateMethodCache()
    {
	//
	// Build a list of all elements in the class hierarchy. This one is bottom-first (i.e. we start
	// with the actual declaring class and its interfaces and then move up (superclass etc.) until we
	// hit java.lang.Object. That is important because it will give us the methods of the declaring class
	// which might in turn be abstract further up the tree.
	//
	// We also ignore all SecurityExceptions that might happen due to SecurityManager restrictions (prominently 
	// hit with Tomcat 5.5).
	//
	// We can also omit all that complicated getPublic, getAccessible and upcast logic that the class map had up
	// until Velocity 1.4. As we always reflect all elements of the tree (that's what we have a cache for), we will
	// hit the public elements sooner or later because we reflect all the public elements anyway.
	//
        List classesToReflect = new ArrayList();
        
        // Ah, the miracles of Java for(;;) ... 
        for (Class classToReflect = getCachedClass(); classToReflect != null ; classToReflect = classToReflect.getSuperclass())
        {
            if (Modifier.isPublic(classToReflect.getModifiers()))
            {
                classesToReflect.add(classToReflect);
                if (debugReflection && log.isDebugEnabled())
                {
                    log.debug("Adding " + classToReflect + " for reflection");
                }
            }
            Class [] interfaces = classToReflect.getInterfaces();
            for (int i = 0; i < interfaces.length; i++)
            {
                if (Modifier.isPublic(interfaces[i].getModifiers()))
                {
                    classesToReflect.add(interfaces[i]);
                    if (debugReflection && log.isDebugEnabled())
                    {
                        log.debug("Adding " + interfaces[i] + " for reflection");
                    }
                }
            }
        }

        for (Iterator it = classesToReflect.iterator(); it.hasNext(); )
        {
            Class classToReflect = (Class) it.next();
            if (debugReflection && log.isDebugEnabled())
            {
                log.debug("Reflecting " + classToReflect);
            }
            

            try
            {
                Method[] methods = classToReflect.getMethods();

                for (int i = 0; i < methods.length; i++)
                {
                    // Strictly spoken that check shouldn't be necessary
                    // because getMethods only returns public methods.
                    int modifiers = methods[i].getModifiers();
                    if (Modifier.isPublic(modifiers)) //  && !)
            	    {
                        // Some of the interfaces contain abstract methods. That is fine, because the actual object must 
                        // implement them anyway (else it wouldn't be implementing the interface). If we find an abstract
                        // method in a non-interface, we skip it, because we do want to make sure that no abstract methods end up in
                        // the cache.                       
                        if (classToReflect.isInterface() || !Modifier.isAbstract(modifiers))
                        {
                            methodCache.put(methods[i]);
                        }
                    }
                }
            }
            catch (SecurityException se) // Everybody feels better with...
            {
        	if (log.isDebugEnabled())
        	{
        	    log.debug("While accessing methods of " + classToReflect + ": ", se);
        	}
            }
        }
    }

    /**
     * This is the cache to store and look up the method information. 
     * 
     * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
     * @version $Id$
     */
    private static final class MethodCache
    {
        private static final class CacheMiss { }
        
        private static final CacheMiss CACHE_MISS = new CacheMiss();

        private static final Object OBJECT = new Object();

        private static final Map convertPrimitives = new HashMap();

        static
        {
            convertPrimitives.put(Boolean.TYPE,   Boolean.class.getName());
            convertPrimitives.put(Byte.TYPE,      Byte.class.getName());
            convertPrimitives.put(Character.TYPE, Character.class.getName());
            convertPrimitives.put(Double.TYPE,    Double.class.getName());
            convertPrimitives.put(Float.TYPE,     Float.class.getName());
            convertPrimitives.put(Integer.TYPE,   Integer.class.getName());
            convertPrimitives.put(Long.TYPE,      Long.class.getName());
            convertPrimitives.put(Short.TYPE,     Short.class.getName());
        }

    	/** Class logger */
	private final Log log;

        /**
         * Cache of Methods, or CACHE_MISS, keyed by method
         * name and actual arguments used to find it.
         */
        private final Map cache = new HashMap();

        /** Map of methods that are searchable according to method parameters to find a match */
        private final MethodMap methodMap = new MethodMap();

        private MethodCache(Log log)
        {
            this.log = log;
        }

        /**
         * Find a Method using the method name and parameter objects.
         *
         * Look in the methodMap for an entry.  If found,
         * it'll either be a CACHE_MISS, in which case we
         * simply give up, or it'll be a Method, in which
         * case, we return it.
         *
         * If nothing is found, then we must actually go
         * and introspect the method from the MethodMap.
         *
         * @param name The method name to look up.
         * @param params An array of parameters for the method.
         * @return A Method object representing the method to invoke or null.
         * @throws MethodMap.AmbiguousException When more than one method is a match for the parameters.
         */
        public synchronized Method get(final String name, final Object [] params)
                throws MethodMap.AmbiguousException
        {
            String methodKey = makeMethodKey(name, params);

            Object cacheEntry = cache.get(methodKey);

            // We looked this up before and failed. 
            if (cacheEntry == CACHE_MISS)
            {
                return null;
            }

            if (cacheEntry == null)
            {
                try
                {
                    // That one is expensive...
                    cacheEntry = methodMap.find(name, params);
                }
                catch(MethodMap.AmbiguousException ae)
                {
                    /*
                     *  that's a miss :-)
                     */
                    cache.put(methodKey, CACHE_MISS);
                    throw ae;
                }

                cache.put(methodKey, 
                        (cacheEntry != null) ? cacheEntry : CACHE_MISS);
            }

            // Yes, this might just be null.

            return (Method) cacheEntry;
        }

        public synchronized void put(Method method)
        {
            String methodKey = makeMethodKey(method);
            
            // We don't overwrite methods. Especially not if we fill the
            // cache from defined class towards java.lang.Object because 
            // abstract methods in superclasses would else overwrite concrete
            // classes further down the hierarchy.
            if (cache.get(methodKey) == null)
            {
        	cache.put(methodKey, method);
        	methodMap.add(method);
        	if (debugReflection && log.isDebugEnabled())
        	{
        	    log.debug("Adding " + method);
        	}
            }
        }

        /**
         * Make a methodKey for the given method using
         * the concatenation of the name and the
         * types of the method parameters.
         * 
         * @param method to be stored as key
         * @return key for ClassMap
         */
        private String makeMethodKey(final Method method)
        {
            Class[] parameterTypes = method.getParameterTypes();

            StringBuffer methodKey = new StringBuffer(method.getName());

            for (int j = 0; j < parameterTypes.length; j++)
            {
                /*
                 * If the argument type is primitive then we want
                 * to convert our primitive type signature to the
                 * corresponding Object type so introspection for
                 * methods with primitive types will work correctly.
                 *
                 * The lookup map (convertPrimitives) contains all eight
                 * primitives (boolean, byte, char, double, float, int, long, short)
                 * known to Java. So it should never return null for the key passed in.
                 */
                if (parameterTypes[j].isPrimitive())
                {
                    methodKey.append((String) convertPrimitives.get(parameterTypes[j]));
                }
                else
                {
                    methodKey.append(parameterTypes[j].getName());
                }
            }

            return methodKey.toString();
        }

        private String makeMethodKey(String method, Object[] params)
        {
            StringBuffer methodKey = new StringBuffer().append(method);

            for (int j = 0; j < params.length; j++)
            {
                Object arg = params[j];

                if (arg == null)
                {
                    arg = OBJECT;
                }

                methodKey.append(arg.getClass().getName());
            }

            return methodKey.toString();
        }
    }
}
