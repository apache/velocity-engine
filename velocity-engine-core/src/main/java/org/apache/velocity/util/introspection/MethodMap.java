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

import org.apache.velocity.exception.VelocityException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id$
 */
public class MethodMap
{
    /* Constants for specificity */
    private static final int INCOMPARABLE = 0;
    private static final int MORE_SPECIFIC = 1;
    private static final int EQUIVALENT = 2;
    private static final int LESS_SPECIFIC = 3;

    /* Constants for applicability */
    private static final int NOT_CONVERTIBLE = 0;
    private static final int EXPLICITLY_CONVERTIBLE = 1;
    private static final int IMPLCITLY_CONVERTIBLE = 2;
    private static final int STRICTLY_CONVERTIBLE = 3;

    ConversionHandler conversionHandler;

    /**
     * Default constructor
     */
    public MethodMap()
    {
        this(null);
    }

    /**
     * Constructor with provided conversion handler
     * @param conversionHandler conversion handler
     * @since 2.0
     */
    public MethodMap(ConversionHandler conversionHandler)
    {
        this.conversionHandler = conversionHandler;
    }

    /**
     * Keep track of all methods with the same name.
     */
    Map methodByNameMap = new ConcurrentHashMap();

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

        return getBestMatch(methodList, classes);
    }

    private class Match
    {
        /* target method */
        Method method;

        /* cache arguments classes array */
        Class[] methodTypes;

        /* specificity: how does the best match compare to provided arguments
         * one one LESS_SPECIFIC, MORE_SPECIFIC or INCOMPARABLE */
        int specificity;

        /* applicability which conversion level is needed against provided arguments
         * one of STRICTLY_CONVERTIBLE, IMPLICITLY_CONVERTIBLE and EXPLICITLY_CONVERTIBLE_ */
        int applicability;

        /* whether the method has varrags */
        boolean varargs;

        Match(Method method, int applicability, Class[] unboxedArgs)
        {
            this.method = method;
            this.applicability = applicability;
            this.methodTypes = method.getParameterTypes();
            this.specificity = compare(methodTypes, unboxedArgs);
            this.varargs = methodTypes.length > 0 && methodTypes[methodTypes.length - 1].isArray();
        }
    }

    private static boolean onlyNullOrObjects(Class[] args)
    {
        for (Class cls : args)
        {
            if (cls != null && cls != Object.class) return false;
        }
        return args.length > 0;
    }

    private Method getBestMatch(List<Method> methods, Class[] args)
    {
        List<Match> bestMatches = new LinkedList<Match>();
        Class[] unboxedArgs = new Class[args.length];
        for (int i = 0; i < args.length; ++i)
        {
            unboxedArgs[i] = IntrospectionUtils.getUnboxedClass(args[i]);
        }
        for (Method method : methods)
        {
            int applicability = getApplicability(method, unboxedArgs);
            if (applicability > NOT_CONVERTIBLE)
            {
                Match match = new Match(method, applicability, unboxedArgs);
                if (bestMatches.size() == 0)
                {
                    bestMatches.add(match);
                }
                else
                {
                    /* filter existing matches */
                    boolean keepMethod = true;
                    for (ListIterator<Match> it = bestMatches.listIterator(); keepMethod && it.hasNext();)
                    {
                        Match best = it.next();
                        /* do not retain match if it's more specific than (or incomparable to) provided (unboxed) arguments
                         * while one of the best matches is less specific
                         */
                        if (best.specificity == LESS_SPECIFIC && match.specificity < EQUIVALENT) /* != LESS_SPECIFIC && != EQUIVALENT */
                        {
                            keepMethod = false;
                        }
                        /* drop considered best match if match is less specific than (unboxed) provided args while
                         * the considered best match is more specific or incomparable
                         */
                        else if (match.specificity == LESS_SPECIFIC && best.specificity < EQUIVALENT) /* != LESS_SPECIFIC && != EQUIVALENT */
                        {
                            it.remove();
                        }
                        /* compare applicability */
                        else if (best.applicability > match.applicability)
                        {
                            keepMethod = false;
                        }
                        else if (best.applicability < match.applicability)
                        {
                            it.remove();
                        }
                        /* compare methods between them */
                        else
                        {
                            /* but only if some provided args are non null and not Object */
                            if (onlyNullOrObjects(args))
                            {
                                /* in this case we only favor non-varrags methods */
                                if (match.varargs != best.varargs)
                                {
                                    if (match.varargs)
                                    {
                                        keepMethod = false;
                                    }
                                    else if (best.varargs)
                                    {
                                        it.remove();
                                    }
                                }
                            }
                            else
                            {
                                switch (compare(match.methodTypes, best.methodTypes))
                                {
                                    case LESS_SPECIFIC:
                                        keepMethod = false;
                                        break;
                                    case MORE_SPECIFIC:
                                        it.remove();
                                        break;
                                    case EQUIVALENT:
                                    case INCOMPARABLE:
                                        /* otherwise it's an equivalent match */
                                        break;
                                }
                            }
                        }
                    }
                    if (keepMethod)
                    {
                        bestMatches.add(match);
                    }
                }
            }
        }

        switch (bestMatches.size())
        {
            case 0: return null;
            case 1: return bestMatches.get(0).method;
            default: throw new AmbiguousException();
        }
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

    /**
     * Determines which method signature (represented by a class array) is more
     * specific. This defines a partial ordering on the method signatures.
     * @param c1 first signature to compare
     * @param c2 second signature to compare
     * @return MORE_SPECIFIC if c1 is more specific than c2, LESS_SPECIFIC if
     * c1 is less specific than c2, INCOMPARABLE if they are incomparable.
     */
    private int compare(Class[] c1, Class[] c2)
    {
        boolean c1IsVararag = false;
        boolean c2IsVararag = false;
        boolean fixedLengths = false;

        // compare lengths to handle comparisons where the size of the arrays
        // doesn't match, but the methods are both applicable due to the fact
        // that one is a varargs method
        if (c1.length > c2.length)
        {
            int l2 = c2.length;
            if (l2 == 0)
            {
                return MORE_SPECIFIC;
            }
            c2 = Arrays.copyOf(c2, c1.length);
            Class itemClass = c2[l2 - 1].getComponentType();
            /* if item class is null, then it implies the vaarg is #1
             * (and receives an empty array)
             */
            if (itemClass == null)
            {
                /* by construct, we have c1.length = l2 + 1 */
                c1IsVararag = true;
                c2[c1.length - 1] = null;
            }
            else
            {
                c2IsVararag = true;
                for (int i = l2 - 1; i < c1.length; ++i)
                {
                /* also overwrite the vaargs itself */
                    c2[i] = itemClass;
                }
            }
            fixedLengths = true;
        }
        else if (c2.length > c1.length)
        {
            int l1 = c1.length;
            if (l1 == 0)
            {
                return LESS_SPECIFIC;
            }
            c1 = Arrays.copyOf(c1, c2.length);
            Class itemClass = c1[l1 - 1].getComponentType();
            /* if item class is null, then it implies the vaarg is #2
             * (and receives an empty array)
             */
            if (itemClass == null)
            {
                /* by construct, we have c2.length = l1 + 1 */
                c2IsVararag = true;
                c1[c2.length - 1] = null;
            }
            else
            {
                c1IsVararag = true;
                for (int i = l1 - 1; i < c2.length; ++i)
                {
                /* also overwrite the vaargs itself */
                    c1[i] = itemClass;
                }
            }
            fixedLengths = true;
        }

        /* ok, move on and compare those of equal lengths */
        int fromC1toC2 = STRICTLY_CONVERTIBLE;
        int fromC2toC1 = STRICTLY_CONVERTIBLE;
        for(int i = 0; i < c1.length; ++i)
        {
            boolean last = !fixedLengths && (i == c1.length - 1);
            if (c1[i] != c2[i])
            {
                if (c1[i] == null)
                {
                    fromC2toC1 = NOT_CONVERTIBLE;
                    if (c2[i].isPrimitive())
                    {
                        fromC1toC2 = NOT_CONVERTIBLE;
                    }
                }
                else if (c2[i] == null)
                {
                    fromC1toC2 = NOT_CONVERTIBLE;
                    if (c1[i].isPrimitive())
                    {
                        fromC2toC1 = NOT_CONVERTIBLE;
                    }
                }
                else
                {
                    switch (fromC1toC2)
                    {
                        case STRICTLY_CONVERTIBLE:
                            if (isStrictConvertible(c2[i], c1[i], last)) break;
                            fromC1toC2 = IMPLCITLY_CONVERTIBLE;
                        case IMPLCITLY_CONVERTIBLE:
                            if (isConvertible(c2[i], c1[i], last)) break;
                            fromC1toC2 = EXPLICITLY_CONVERTIBLE;
                        case EXPLICITLY_CONVERTIBLE:
                            if (isExplicitlyConvertible(c2[i], c1[i], last)) break;
                            fromC1toC2 = NOT_CONVERTIBLE;
                    }
                    switch (fromC2toC1)
                    {
                        case STRICTLY_CONVERTIBLE:
                            if (isStrictConvertible(c1[i], c2[i], last)) break;
                            fromC2toC1 = IMPLCITLY_CONVERTIBLE;
                        case IMPLCITLY_CONVERTIBLE:
                            if (isConvertible(c1[i], c2[i], last)) break;
                            fromC2toC1 = EXPLICITLY_CONVERTIBLE;
                        case EXPLICITLY_CONVERTIBLE:
                            if (isExplicitlyConvertible(c1[i], c2[i], last)) break;
                            fromC2toC1 = NOT_CONVERTIBLE;
                    }
                }
            }
        }

        if (fromC1toC2 == NOT_CONVERTIBLE && fromC2toC1 == NOT_CONVERTIBLE)
        {
            /*
             *  Incomparable due to cross-assignable arguments (i.e.
             * foo(String, Foo) vs. foo(Foo, String))
             */
            return INCOMPARABLE;
        }

        if (fromC1toC2 > fromC2toC1)
        {
            return MORE_SPECIFIC;
        }
        else if (fromC2toC1 > fromC1toC2)
        {
            return LESS_SPECIFIC;
        }
        else
        {
            /*
             * If one method accepts varargs and the other does not,
             * call the non-vararg one more specific.
             */
            boolean last1Array = c1IsVararag || !fixedLengths && c1[c1.length - 1].isArray();
            boolean last2Array = c2IsVararag || !fixedLengths && c2[c2.length - 1].isArray();
            if (last1Array && !last2Array)
            {
                return LESS_SPECIFIC;
            }
            if (!last1Array && last2Array)
            {
                return MORE_SPECIFIC;
            }
        }
        return EQUIVALENT;
    }

    /**
     * Returns the applicability of the supplied method against actual argument types.
     *
     * @param method method that will be called
     * @param classes arguments to method
     * @return the level of applicability:
     *         0 = not applicable
     *         1 = explicitly applicable (i.e. using stock or custom conversion handlers)
     *         2 = implicitly applicable (i.e. using JAva implicit boxing/unboxing and primitive types widening)
     *         3 = strictly applicable
     */
    private int getApplicability(Method method, Class[] classes)
    {
        Class[] methodArgs = method.getParameterTypes();
        int ret = STRICTLY_CONVERTIBLE;
        if (methodArgs.length > classes.length)
        {
            // if there's just one more methodArg than class arg
            // and the last methodArg is an array, then treat it as a vararg
            if (methodArgs.length == classes.length + 1 &&
                methodArgs[methodArgs.length - 1].isArray())
            {
                // all the args preceding the vararg must match
                for (int i = 0; i < classes.length; i++)
                {
                    if (!isStrictConvertible(methodArgs[i], classes[i], false))
                    {
                        if (isConvertible(methodArgs[i], classes[i], false))
                        {
                            ret = Math.min(ret, IMPLCITLY_CONVERTIBLE);
                        }
                        else if (isExplicitlyConvertible(methodArgs[i], classes[i], false))
                        {
                            ret = Math.min(ret, EXPLICITLY_CONVERTIBLE);
                        }
                        else
                        {
                            return NOT_CONVERTIBLE;
                        }
                    }
                }
                return ret;
            }
            else
            {
                return NOT_CONVERTIBLE;
            }
        }
        else if (methodArgs.length == classes.length)
        {
            // this will properly match when the last methodArg
            // is an array/varargs and the last class is the type of array
            // (e.g. String when the method is expecting String...)
            for(int i = 0; i < classes.length; ++i)
            {
                if (!isStrictConvertible(methodArgs[i], classes[i], i == classes.length - 1 && methodArgs[i].isArray()))
                {
                    if (isConvertible(methodArgs[i], classes[i], i == classes.length - 1 && methodArgs[i].isArray()))
                    {
                        ret = Math.min(ret, IMPLCITLY_CONVERTIBLE);
                    }
                    else if (isExplicitlyConvertible(methodArgs[i], classes[i], i == classes.length - 1 && methodArgs[i].isArray()))
                    {
                        ret = Math.min(ret, EXPLICITLY_CONVERTIBLE);
                    }
                    else
                    {
                        return NOT_CONVERTIBLE;
                    }
                }
            }
            return ret;
        }
        else if (methodArgs.length > 0) // more arguments given than the method accepts; check for varargs
        {
            // check that the last methodArg is an array
            Class lastarg = methodArgs[methodArgs.length - 1];
            if (!lastarg.isArray())
            {
                return NOT_CONVERTIBLE;
            }

            // check that they all match up to the last method arg component type
            for (int i = 0; i < methodArgs.length - 1; ++i)
            {
                if (!isStrictConvertible(methodArgs[i], classes[i], false))
                {
                    if (isConvertible(methodArgs[i], classes[i], false))
                    {
                        ret = Math.min(ret, IMPLCITLY_CONVERTIBLE);
                    }
                    else if (isExplicitlyConvertible(methodArgs[i], classes[i], false))
                    {
                        ret = Math.min(ret, EXPLICITLY_CONVERTIBLE);
                    }
                    else
                    {
                        return NOT_CONVERTIBLE;
                    }
                }
            }

            // check that all remaining arguments are convertible to the vararg type
            Class vararg = lastarg.getComponentType();
            for (int i = methodArgs.length - 1; i < classes.length; ++i)
            {
                if (!isStrictConvertible(vararg, classes[i], false))
                {
                    if (isConvertible(vararg, classes[i], false))
                    {
                        ret = Math.min(ret, IMPLCITLY_CONVERTIBLE);
                    }
                    else if (isExplicitlyConvertible(vararg, classes[i], false))
                    {
                        ret = Math.min(ret, EXPLICITLY_CONVERTIBLE);
                    }
                    else
                    {
                        return NOT_CONVERTIBLE;
                    }
                }
            }
            return ret;
        }
        return NOT_CONVERTIBLE;
    }

    /**
     * Returns true if <code>actual</code> is convertible to <code>formal</code> by implicit Java method call conversions
     *
     * @param formal
     * @param actual
     * @param possibleVarArg
     * @return convertible
     */
    private boolean isConvertible(Class formal, Class actual, boolean possibleVarArg)
    {
        return IntrospectionUtils.
            isMethodInvocationConvertible(formal, actual, possibleVarArg);
    }

    /**
     * Returns true if <code>actual</code> is strictly convertible to <code>formal</code> (aka without implicit
     * boxing/unboxing)
     *
     * @param formal
     * @param actual
     * @param possibleVarArg
     * @return convertible
     */
    private static boolean isStrictConvertible(Class formal, Class actual, boolean possibleVarArg)
    {
        return IntrospectionUtils.
            isStrictMethodInvocationConvertible(formal, actual, possibleVarArg);
    }

    /**
     * Returns true if <code>actual</code> is convertible to <code>formal</code> using an explicit converter
     *
     * @param formal
     * @param actual
     * @param possibleVarArg
     * @return
     */
    private boolean isExplicitlyConvertible(Class formal, Class actual, boolean possibleVarArg)
    {
        return conversionHandler != null && conversionHandler.isExplicitlyConvertible(formal, actual, possibleVarArg);
    }
}
