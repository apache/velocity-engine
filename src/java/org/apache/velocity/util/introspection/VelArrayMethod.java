package org.apache.velocity.util.introspection;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Implementation of VelMethod to provide introspective "methods" for
 * arrays that match those that would work on a fixed-size {@link List}.
 * Currently only size(), isEmpty(), get(int), and set(int,Object) are
 * supported.  Later, support may be added for other read-only methods
 * such as contains(Object) or subList(int,int).  Patches are welcome! :)
 *
 * @author Nathan Bubna
 * @version $Id: VelArrayMethod.java 440740 2006-09-06 15:37:44Z nbubna $
 */
public class VelArrayMethod implements VelMethod
{
    public static String SIZE = "size";
    public static String IS_EMPTY = "isEmpty";
    public static String GET = "get";
    public static String SET = "set";

    public static boolean supports(String methodName, Object[] params)
    {
        // quickest way to narrow things down is to switch
        // on the number of parameters
        switch (params.length)
        {
            case 0:
                // then they must be calling one of these
                return SIZE.equals(methodName) || IS_EMPTY.equals(methodName);
            case 1:
                // must be get() with a numeric param
                return GET.equals(methodName) && isNumeric(params[0]);
            case 2:
                // must be set() with a numeric first param
                return SET.equals(methodName) && isNumeric(params[0]);
            default:
                // it's not a supported method
                return false;
        }
    }

    protected static boolean isNumeric(Object param)
    {
        if (param != null && Number.class.isAssignableFrom(param.getClass()))
        {
            return true;
        }
        //TODO? do we need to check for primitive number types?
        return false;
    }


    final Class arrayClass;
    final String methodName;
    final Object[] params;

    public VelArrayMethod(Class arrayClass, String methodName, Object[] params)
    {
        this.methodName = methodName;
        this.params = params;
        this.arrayClass = arrayClass;
    }

    protected int toInt(Object param)
    {
        return ((Number)param).intValue();
    }

    public Object invoke(Object array, Object[] params) throws Exception
    {
        // quickest way to narrow things down is to switch
        // on the number of parameters
        switch (params.length)
        {
            // 0 params is either size() or isEmpty() (maybe iterator() someday)
            case 0:
                int length = Array.getLength(array);
                if (SIZE.equals(methodName))
                {
                    return new Integer(length);
                }
                if (IS_EMPTY.equals(methodName))
                {
                    return Boolean.valueOf(length == 0);
                }

            // 1 param currently only could mean get() with a numeric param
            // it could mean contains(), indexOf(), etc someday
            case 1:
                try
                {
                    return Array.get(array, toInt(params[0]));
                }
                catch (RuntimeException re)
                {
                    throw new InvocationTargetException(re);
                }

            // 2 params currently means set() with a numeric first param
            // it could later mean subList(int,int) too
            case 2:
                try
                {
                    int index = toInt(params[0]);
                    // get the old value to return it (like List does)
                    Object old = Array.get(array, index);
                    Array.set(array, index, params[1]);
                    return old;
                }
                catch (RuntimeException re)
                {
                    throw new InvocationTargetException(re);
                }

            default:
                // if supports() was checked before creating this instance
                // then it should not be possible to get here
                throw new UnsupportedOperationException('\'' + methodName +
                                                        "' with " + params.length +
                                                        " parameters is not a supported array method");
        }
    }

    public boolean isCacheable()
    {
        return true;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public Class getReturnType()
    {
        if (SIZE.equals(methodName))
        {
            return int.class;
        }
        if (GET.equals(methodName) || SET.equals(methodName))
        {
            // should this be Object.class instead?
            return arrayClass.getComponentType();
        }
        if (IS_EMPTY.equals(methodName))
        {
            return boolean.class;
        }
        // not sure what else to do here
        return Object.class;
    }

}
