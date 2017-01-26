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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author Nathan Bubna
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id: IntrospectionUtils.java 476785 2006-11-19 10:06:21Z henning $
 * @since 1.6
 */
public class IntrospectionUtils
{
    /**
     * boxing helper maps for standard types
     */
    static Map<Class, Class> boxingMap, unboxingMap;

    static
    {
        boxingMap = new HashMap<Class, Class>();
        boxingMap.put(Boolean.TYPE, Boolean.class);
        boxingMap.put(Character.TYPE, Character.class);
        boxingMap.put(Byte.TYPE, Byte.class);
        boxingMap.put(Short.TYPE, Short.class);
        boxingMap.put(Integer.TYPE, Integer.class);
        boxingMap.put(Long.TYPE, Long.class);
        boxingMap.put(Float.TYPE, Float.class);
        boxingMap.put(Double.TYPE, Double.class);

        unboxingMap = new HashMap<Class, Class>();
        for (Map.Entry<Class,Class> entry : (Set<Map.Entry<Class,Class>>)boxingMap.entrySet())
        {
            unboxingMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * returns boxed type (or input type if not a primitive type)
     * @param clazz input class
     * @return boxed class
     */
    static Class getBoxedClass(Class clazz)
    {
        Class boxed = boxingMap.get(clazz);
        return boxed == null ? clazz : boxed;
    }

    /**
     * returns unboxed type (or input type if not successful)
     * @param clazz input class
     * @return unboxed class
     */
    static Class getUnboxedClass(Class clazz)
    {
        Class unboxed = unboxingMap.get(clazz);
        return unboxed == null ? clazz : unboxed;
    }

    /**
     *
     */


    /**
     * Determines whether a type represented by a class object is
     * convertible to another type represented by a class object using a
     * method invocation conversion, treating object types of primitive
     * types as if they were primitive types (that is, a Boolean actual
     * parameter type matches boolean primitive formal type). This behavior
     * is because this method is used to determine applicable methods for
     * an actual parameter list, and primitive types are represented by
     * their object duals in reflective method calls.
     *
     * @param formal the formal parameter type to which the actual
     * parameter type should be convertible
     * @param actual the actual parameter type.
     * @param possibleVarArg whether or not we're dealing with the last parameter
     * in the method declaration
     * @return true if either formal type is assignable from actual type,
     * or formal is a primitive type and actual is its corresponding object
     * type or an object type of a primitive type that can be converted to
     * the formal type.
     */
    public static boolean isMethodInvocationConvertible(Class formal,
                                                        Class actual,
                                                        boolean possibleVarArg)
    {
        /* if it's a null, it means the arg was null */
        if (actual == null)
        {
            return !formal.isPrimitive();
        }

        /* Check for identity or widening reference conversion */
        if (formal.isAssignableFrom(actual))
        {
            return true;
        }

        /* 2.0: Since MethodMap's comparison functions now use this method with potentially reversed arguments order,
         * actual can be a primitive type. */

        /* Check for boxing */
        if (!formal.isPrimitive() && actual.isPrimitive())
        {
            Class boxed = boxingMap.get(actual);
            if (boxed != null && boxed == formal || formal.isAssignableFrom(boxed)) return true;
        }

        if (formal.isPrimitive())
        {
            if (actual.isPrimitive())
            {
                /* check for widening primitive conversion */
                if (formal == Short.TYPE && actual == Byte.TYPE)
                    return true;
                if (formal == Integer.TYPE && (
                        actual == Byte.TYPE || actual == Short.TYPE))
                    return true;
                if (formal == Long.TYPE && (
                        actual == Byte.TYPE || actual == Short.TYPE || actual == Integer.TYPE))
                    return true;
                if (formal == Float.TYPE && (
                        actual == Byte.TYPE || actual == Short.TYPE || actual == Integer.TYPE ||
                                actual == Long.TYPE))
                    return true;
                if (formal == Double.TYPE && (
                        actual == Byte.TYPE || actual == Short.TYPE || actual == Integer.TYPE ||
                                actual == Long.TYPE || actual == Float.TYPE))
                    return true;
            }
            else
            {
                /* Check for unboxing with widening primitive conversion. */
                if (formal == Boolean.TYPE && actual == Boolean.class)
                    return true;
                if (formal == Character.TYPE && actual == Character.class)
                    return true;
                if (formal == Byte.TYPE && actual == Byte.class)
                    return true;
                if (formal == Short.TYPE && (actual == Short.class || actual == Byte.class))
                    return true;
                if (formal == Integer.TYPE && (actual == Integer.class || actual == Short.class ||
                        actual == Byte.class))
                    return true;
                if (formal == Long.TYPE && (actual == Long.class || actual == Integer.class ||
                        actual == Short.class || actual == Byte.class))
                    return true;
                if (formal == Float.TYPE && (actual == Float.class || actual == Long.class ||
                        actual == Integer.class || actual == Short.class || actual == Byte.class))
                    return true;
                if (formal == Double.TYPE && (actual == Double.class || actual == Float.class ||
                        actual == Long.class || actual == Integer.class || actual == Short.class ||
                        actual == Byte.class))
                    return true;
            }
        }

        /* Check for vararg conversion. */
        if (possibleVarArg && formal.isArray())
        {
            if (actual.isArray())
            {
                actual = actual.getComponentType();
            }
            return isMethodInvocationConvertible(formal.getComponentType(),
                                                 actual, false);
        }
        return false;
    }

    /**
     * Determines whether a type represented by a class object is
     * convertible to another type represented by a class object using a
     * method invocation conversion, without matching object and primitive
     * types. This method is used to determine the more specific type when
     * comparing signatures of methods.
     *
     * @param formal the formal parameter type to which the actual
     * parameter type should be convertible
     * @param actual the actual parameter type.
     * @param possibleVarArg whether or not we're dealing with the last parameter
     * in the method declaration
     * @return true if either formal type is assignable from actual type,
     * or formal and actual are both primitive types and actual can be
     * subject to widening conversion to formal.
     */
    public static boolean isStrictMethodInvocationConvertible(Class formal,
                                                              Class actual,
                                                              boolean possibleVarArg)
    {
        /* we shouldn't get a null into, but if so */
        if (actual == null && !formal.isPrimitive())
        {
            return true;
        }

        /* Check for identity or widening reference conversion */
        if(formal.isAssignableFrom(actual))
        {
            return true;
        }

        /* Check for widening primitive conversion. */
        if(formal.isPrimitive())
        {
            if(formal == Short.TYPE && (actual == Byte.TYPE))
                return true;
            if(formal == Integer.TYPE &&
               (actual == Short.TYPE || actual == Byte.TYPE))
                return true;
            if(formal == Long.TYPE &&
               (actual == Integer.TYPE || actual == Short.TYPE ||
                actual == Byte.TYPE))
                return true;
            if(formal == Float.TYPE &&
               (actual == Long.TYPE || actual == Integer.TYPE ||
                actual == Short.TYPE || actual == Byte.TYPE))
                return true;
            if(formal == Double.TYPE &&
               (actual == Float.TYPE || actual == Long.TYPE ||
                actual == Integer.TYPE || actual == Short.TYPE ||
                actual == Byte.TYPE))
                return true;
        }

        /* Check for vararg conversion. */
        if (possibleVarArg && formal.isArray())
        {
            if (actual.isArray())
            {
                actual = actual.getComponentType();
            }
            return isStrictMethodInvocationConvertible(formal.getComponentType(),
                                                       actual, false);
        }
        return false;
    }
}
