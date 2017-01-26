package org.apache.velocity.util;

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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Support for getAs<Type>() convention for rendering (String), evaluating (Boolean)
 * or doing math with (Number) references.
 *
 * @author Nathan Bubna
 * @since 2.0
 */
public class DuckType
{
    protected enum Types
    {
        STRING("getAsString"),
        NUMBER("getAsNumber"),
        BOOLEAN("getAsBoolean"),
        EMPTY("isEmpty");

        final String name;
        final Map<Class,Object> cache = new HashMap();

        Types(String name)
        {
            this.name = name;
        }

        void set(Class c, Object o)
        {
            cache.put(c, o);
        }

        Object get(Class c)
        {
            return cache.get(c);
        }
    }

    protected static final Object NO_METHOD = new Object();

    public static String asString(Object value)
    {
        return asString(value, true);
    }

    public static String asString(Object value, boolean coerceType)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof String)
        {
            return (String)value;
        }
        Object got = get(value, Types.STRING);
        if (got == NO_METHOD)
        {
            return coerceType ? value.toString() : null;
        }
        return (String)got;
    }

    public static boolean asNull(Object value)
    {
        if (value == null ||
            get(value, Types.STRING) == null ||
            get(value, Types.NUMBER) == null)
        {
            return true;
        }
        return false;
    }

    public static boolean asBoolean(Object value)
    {
        return asBoolean(value, true);
    }

    public static boolean asBoolean(Object value, boolean coerceType)
    {
        if (value == null)
        {
            return false;
        }
        if (value instanceof Boolean)
        {
            return ((Boolean)value).booleanValue();
        }
        Object got = get(value, Types.BOOLEAN);
        if (got != NO_METHOD)
        {
            return ((Boolean)got).booleanValue();
        }
        if (coerceType)
        {
            return !asEmpty(value);
        }
        return true;
    }

    // see VELOCITY-692 for discussion about empty values
    public static boolean asEmpty(Object value)
    {
        // empty variable
        if (value == null)
        {
            return true;
        }

        // empty string
        if (value instanceof CharSequence)
        {
            return ((CharSequence)value).length() == 0;
        }

        // isEmpty() object
        Object isEmpty = get(value, Types.EMPTY);
        if (isEmpty != NO_METHOD)
        {
            return (Boolean)isEmpty;
        }

        // empty array
        if (value.getClass().isArray())
        {
            return Array.getLength(value) == 0;// [] is false
        }

        // null getAsString()
        Object asString = get(value, Types.STRING);
        if (asString == null)
        {
            return true;// duck null
        }
        // empty getAsString()
        else if (asString != NO_METHOD)
        {
            return ((String)asString).length() == 0;
        }

        // null getAsNumber()
        Object asNumber = get(value, Types.NUMBER);
        if (asNumber == null)
        {
            return true;
        }

        // empty toString()
        String string = value.toString();
        return string == null || string.length() == 0;
    }

    public static Number asNumber(Object value)
    {
        return asNumber(value, true);
    }

    public static Number asNumber(Object value, boolean coerceType)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return (Number)value;
        }
        Object got = get(value, Types.NUMBER);
        if (got != NO_METHOD)
        {
            return (Number)got;
        }
        if (coerceType)
        {
            String string = asString(value);// coerce to string
            if (string != null)
            {
                return new BigDecimal(string);
            }
        }
        return null;
    }

    protected static Object get(Object value, Types type)
    {
        try
        {
            // check cache
            Class c = value.getClass();
            Object cached = type.get(c);
            if (cached == NO_METHOD)
            {
                return cached;
            }
            if (cached != null)
            {
                return ((Method)cached).invoke(value);
            }
            // ok, search the class
            Method method = findMethod(c, type);
            if (method == null)
            {
                type.set(c, NO_METHOD);
                return NO_METHOD;
            }
            type.set(c, method);
            return method.invoke(value);
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);// no checked exceptions, please
        }
    }

    protected static Method findMethod(Class c, Types type)
    {
        if (c == null || c == Object.class)
        {
            return null;
        }
        Method m = getMethod(c, type.name);
        if (m != null)
        {
            return m;
        }
        for (Class i : c.getInterfaces())
        {
            m = findMethod(i, type);
            if (m != null)
            {
                return m;
            }
        }
        m = findMethod(c.getSuperclass(), type);
        if (m != null)
        {
            return m;
        }
        return null;
    }

    private static Method getMethod(Class c, String name)
    {
        if (Modifier.isPublic(c.getModifiers()))
        {
            try
            {
                Method m = c.getDeclaredMethod(name);
                if (Modifier.isPublic(m.getModifiers()))
                {
                    return m;
                }
            }
            catch (NoSuchMethodException nsme) {}
        }
        return null;
    }

}
