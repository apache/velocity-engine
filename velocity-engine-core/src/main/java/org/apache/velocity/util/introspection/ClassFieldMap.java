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


import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache of introspection information for a specific class instance.
 * Keys {@link java.lang.reflect.Field} objects by the field names.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @author Nathan Bubna
 * @author <a href="mailto:cdauth@cdauth.eu">Candid Dauth</a>
 */
public class ClassFieldMap
{
    /** Set true if you want to debug the reflection code */
    private static final boolean debugReflection = false;

    /** Class logger */
    private final Logger log;

    /**
     * Class passed into the constructor used to as
     * the basis for the Field map.
     */
    private final Class clazz;

    /**
     * String --&gt; Field map, the key is the field name
     */
    private final Map fieldCache;

    /**
     * Standard constructor
     * @param clazz The class for which this ClassMap gets constructed.
     */
    public ClassFieldMap(final Class clazz, final Logger log)
    {
        this.clazz = clazz;
        this.log = log;

        if (debugReflection)
        {
            log.debug("=================================================================");
            log.debug("== Class: {}", clazz);
        }

        fieldCache = createFieldCache();

        if (debugReflection)
        {
            log.debug("=================================================================");
        }
    }

    /**
     * Returns the class object whose fields are cached by this map.
     *
     * @return The class object whose fields are cached by this map.
     */
    public Class getCachedClass()
    {
        return clazz;
    }

    /**
     * Find a Field using the field name.
     *
     * @param name The field name to look up.
     * @return A Field object representing the field to invoke or null.
     */
    public Field findField(final String name)
    {
        return (Field)fieldCache.get(name);
    }

    /**
     * Populate the Map of direct hits. These
     * are taken from all the public fields
     * that our class, its parents and their implemented interfaces provide.
     */
    private Map createFieldCache()
    {
        Map fieldCache = new ConcurrentHashMap();
	//
	// Looks through all elements in the class hierarchy.
	//
	// We ignore all SecurityExceptions that might happen due to SecurityManager restrictions (prominently
	// hit with Tomcat 5.5).
        // Ah, the miracles of Java for(;;) ...
        for (Class classToReflect = getCachedClass(); classToReflect != null ; classToReflect = classToReflect.getSuperclass())
        {
            if (Modifier.isPublic(classToReflect.getModifiers()))
            {
                populateFieldCacheWith(fieldCache, classToReflect);
            }
            Class [] interfaces = classToReflect.getInterfaces();
            for (Class anInterface : interfaces)
            {
                populateFieldCacheWithInterface(fieldCache, anInterface);
            }
        }
        // return the already initialized cache
        return fieldCache;
    }

    /* recurses up interface hierarchy to get all super interfaces (VELOCITY-689) */
    private void populateFieldCacheWithInterface(Map fieldCache, Class iface)
    {
        if (Modifier.isPublic(iface.getModifiers()))
        {
            populateFieldCacheWith(fieldCache, iface);
        }
        Class[] supers = iface.getInterfaces();
        for (Class aSuper : supers)
        {
            populateFieldCacheWithInterface(fieldCache, aSuper);
        }
    }

    private void populateFieldCacheWith(Map fieldCache, Class classToReflect)
    {
        if (debugReflection)
        {
            log.debug("Reflecting {}", classToReflect);
        }

        try
        {
            Field[] fields = classToReflect.getDeclaredFields();
            for (Field field : fields)
            {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers))
                {
                    fieldCache.put(field.getName(), field);
                }
            }
        }
        catch (SecurityException se) // Everybody feels better with...
        {
            log.debug("While accessing fields of {}:", classToReflect, se);
        }
    }

}
