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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @version $Id$
 */
public class MethodMap
{
    private static final int MORE_SPECIFIC = 0;
    private static final int LESS_SPECIFIC = 1;
    private static final int INCOMPARABLE = 2;

    /**
     * Keep track of all methods with the same name.
     */
    Map methodByNameMap = new Hashtable();

    /**
     * Add a method to a list of methods by name.
     * For a particular class we are keeping track
     * of all the methods with the same name.
     * @param method
     */
    public void add(Method method)
    {
        String methodName = method.getName();

        List l = get( methodName );

        if ( l == null)
        {
            l = new ArrayList();
            methodByNameMap.put(methodName, l);
        }

        l.add(method);
    }

    /**
     * Return a list of methods with the same name.
     *
     * @param key
     * @return List list of methods
     */
    public List get(String key)
    {
        return (List) methodByNameMap.get(key);
    }

    /**
     *  <p>
     *  Find a method.  Attempts to find the
     *  most specific applicable method using the
     *  algorithm described in the JLS section
     *  15.12.2 (with the exception that it can't
     *  distinguish a primitive type argument from
     *  an object type argument, since in reflection
     *  primitive type arguments are represented by
     *  their object counterparts, so for an argument of
     *  type (say) java.lang.Integer, it will not be able
     *  to decide between a method that takes int and a
     *  method that takes java.lang.Integer as a parameter.
     *  </p>
     *
     *  <p>
     *  This turns out to be a relatively rare case
     *  where this is needed - however, functionality
     *  like this is needed.
     *  </p>
     *
     *  @param methodName name of method
     *  @param args the actual arguments with which the method is called
     *  @return the most specific applicable method, or null if no
     *  method is applicable.
     *  @throws AmbiguousException if there is more than one maximally
     *  specific applicable method
     */
    public Method find(String methodName, Object[] args)
        throws AmbiguousException
    {
        List methodList = get(methodName);

        if (methodList == null)
        {
            return null;
        }

        int l = args.length;
        Class[] classes = new Class[l];

        for(int i = 0; i < l; ++i)
        {
            Object arg = args[i];

            /*
             * if we are careful down below, a null argument goes in there
             * so we can know that the null was passed to the method
             */
            classes[i] =
                    arg == null ? null : arg.getClass();
        }

        return getMostSpecific(methodList, classes);
    }

    /**
     *  Simple distinguishable exception, used when
     *  we run across ambiguous overloading.  Caught
     *  by the introspector.
     */
    public static class AmbiguousException extends RuntimeException
    {
        /**
         * Version Id for serializable
         */
        private static final long serialVersionUID = -2314636505414551663L;
    }


    private static Method getMostSpecific(List methods, Class[] classes)
        throws AmbiguousException
    {
        LinkedList applicables = getApplicables(methods, classes);

        if(applicables.isEmpty())
        {
            return null;
        }

        if(applicables.size() == 1)
        {
            return (Method)applicables.getFirst();
        }

        /*
         * This list will contain the maximally specific methods. Hopefully at
         * the end of the below loop, the list will contain exactly one method,
         * (the most specific method) otherwise we have ambiguity.
         */

        LinkedList maximals = new LinkedList();

        for (Iterator applicable = applicables.iterator();
             applicable.hasNext();)
        {
            Method app = (Method) applicable.next();
            Class[] appArgs = app.getParameterTypes();
            boolean lessSpecific = false;

            for (Iterator maximal = maximals.iterator();
                 !lessSpecific && maximal.hasNext();)
            {
                Method max = (Method) maximal.next();

                switch(moreSpecific(appArgs, max.getParameterTypes()))
                {
                    case MORE_SPECIFIC:
                    {
                        /*
                         * This method is more specific than the previously
                         * known maximally specific, so remove the old maximum.
                         */

                        maximal.remove();
                        break;
                    }

                    case LESS_SPECIFIC:
                    {
                        /*
                         * This method is less specific than some of the
                         * currently known maximally specific methods, so we
                         * won't add it into the set of maximally specific
                         * methods
                         */

                        lessSpecific = true;
                        break;
                    }
                }
            }

            if(!lessSpecific)
            {
                maximals.addLast(app);
            }
        }

        if(maximals.size() > 1)
        {
            // We have more than one maximally specific method
            throw new AmbiguousException();
        }

        return (Method)maximals.getFirst();
    }

    /**
     * Determines which method signature (represented by a class array) is more
     * specific. This defines a partial ordering on the method signatures.
     * @param c1 first signature to compare
     * @param c2 second signature to compare
     * @return MORE_SPECIFIC if c1 is more specific than c2, LESS_SPECIFIC if
     * c1 is less specific than c2, INCOMPARABLE if they are incomparable.
     */
    private static int moreSpecific(Class[] c1, Class[] c2)
    {
        boolean c1MoreSpecific = false;
        boolean c2MoreSpecific = false;

        // compare lengths to handle comparisons where the size of the arrays
        // doesn't match, but the methods are both applicable due to the fact
        // that one is a varargs method
        if (c1.length > c2.length)
        {
            return MORE_SPECIFIC;
        }
        if (c2.length > c1.length)
        {
            return LESS_SPECIFIC;
        }

        // ok, move on and compare those of equal lengths
        for(int i = 0; i < c1.length; ++i)
        {
            if(c1[i] != c2[i])
            {
                boolean last = (i == c1.length - 1);
                c1MoreSpecific =
                    c1MoreSpecific ||
                    isStrictConvertible(c2[i], c1[i], last);
                c2MoreSpecific =
                    c2MoreSpecific ||
                    isStrictConvertible(c1[i], c2[i], last);
            }
        }

        if(c1MoreSpecific)
        {
            if(c2MoreSpecific)
            {
                /*
                 *  Incomparable due to cross-assignable arguments (i.e.
                 * foo(String, Object) vs. foo(Object, String))
                 */

                return INCOMPARABLE;
            }

            return MORE_SPECIFIC;
        }

        if(c2MoreSpecific)
        {
            return LESS_SPECIFIC;
        }

        /*
         * Incomparable due to non-related arguments (i.e.
         * foo(Runnable) vs. foo(Serializable))
         */

        return INCOMPARABLE;
    }

    /**
     * Returns all methods that are applicable to actual argument types.
     * @param methods list of all candidate methods
     * @param classes the actual types of the arguments
     * @return a list that contains only applicable methods (number of
     * formal and actual arguments matches, and argument types are assignable
     * to formal types through a method invocation conversion).
     */
    private static LinkedList getApplicables(List methods, Class[] classes)
    {
        LinkedList list = new LinkedList();

        for (Iterator imethod = methods.iterator(); imethod.hasNext();)
        {
            Method method = (Method) imethod.next();

            if(isApplicable(method, classes))
            {
                list.add(method);
            }

        }
        return list;
    }

    /**
     * Returns true if the supplied method is applicable to actual
     * argument types.
     * 
     * @param method method that will be called
     * @param classes arguments to method
     * @return true if method is applicable to arguments
     */
    private static boolean isApplicable(Method method, Class[] classes)
    {
        Class[] methodArgs = method.getParameterTypes();

        if (methodArgs.length > classes.length)
        {
            // if there's just one more methodArg than class arg
            // and the last methodArg is an array, then treat it as a vararg
            if (methodArgs.length == classes.length + 1 &&
                methodArgs[methodArgs.length - 1].isArray())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if (methodArgs.length == classes.length)
        {
            // this will properly match when the last methodArg
            // is an array/varargs and the last class is the type of array
            // (e.g. String when the method is expecting String...)
            for(int i = 0; i < classes.length; ++i)
            {
                if(!isConvertible(methodArgs[i], classes[i], false))
                {
                    // if we're on the last arg and the method expects an array
                    if (i == classes.length - 1 && methodArgs[i].isArray())
                    {
                        // check to see if the last arg is convertible
                        // to the array's component type
                        return isConvertible(methodArgs[i], classes[i], true);
                    }
                    return false;
                }
            }
        }
        else if (methodArgs.length > 0) // more arguments given than the method accepts; check for varargs
        {
            // check that the last methodArg is an array
            Class lastarg = methodArgs[methodArgs.length - 1];
            if (!lastarg.isArray())
            {
                return false;
            }

            // check that they all match up to the last method arg
            for (int i = 0; i < methodArgs.length - 1; ++i)
            {
                if (!isConvertible(methodArgs[i], classes[i], false))
                {
                    return false;
                }
            }

            // check that all remaining arguments are convertible to the vararg type
            Class vararg = lastarg.getComponentType();
            for (int i = methodArgs.length - 1; i < classes.length; ++i)
            {
                if (!isConvertible(vararg, classes[i], false))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isConvertible(Class formal, Class actual,
                                         boolean possibleVarArg)
    {
        return IntrospectionUtils.
            isMethodInvocationConvertible(formal, actual, possibleVarArg);
    }

    private static boolean isStrictConvertible(Class formal, Class actual,
                                               boolean possibleVarArg)
    {
        return IntrospectionUtils.
            isStrictMethodInvocationConvertible(formal, actual, possibleVarArg);
    }
}
