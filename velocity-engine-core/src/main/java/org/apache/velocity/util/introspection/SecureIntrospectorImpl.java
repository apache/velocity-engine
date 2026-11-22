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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Prevent "dangerous" classloader/reflection related calls.  Use this
 * introspector for situations in which template writers are numerous
 * or untrusted.  Specifically, this introspector prevents creation of
 * arbitrary objects and prevents reflection on objects.
 *
 * <p>See documentation of checkObjectExecutePermission() for
 * more information on specific classes and methods blocked.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 * @since 1.5
 */
public class SecureIntrospectorImpl extends Introspector implements SecureIntrospectorControl
{
    private final Set<String> badClasses;
    private final Set<String> badPackages;
    private List<RestrictedMethod> badMethods;

    /**
     * Whether a class is restricted by <code>badClasses</code> or <code>badPackages</code>, cached per class.
     */
    private final ClassValue<Boolean> restricted = new ClassValue<Boolean>()
    {
        @Override
        protected Boolean computeValue(Class<?> clazz)
        {
            return isRestricted(clazz);
        }
    };

    /**
     * Backwards-compatible constructor without method-level restrictions.
     */
    public SecureIntrospectorImpl(String[] badClasses, String[] badPackages, Logger log)
    {
        this(badClasses, badPackages, null, log);
    }

    /**
     * @param badClasses fully-qualified names of the classes and interfaces whose methods are entirely blocked,
     *                   along with the methods of the classes extending or implementing them
     * @param badPackages names of the packages whose classes are entirely blocked (exact match, a class
     *                    extending a class of a restricted package from another package is not blocked)
     * @param badMethods method specs of the form <code>fully.qualified.ClassName.methodName</code>;
     *                   restriction applies to the named class and all its subclasses, all overloads
     * @param log logger
     * @since 2.5
     */
    public SecureIntrospectorImpl(String[] badClasses, String[] badPackages, String[] badMethods, Logger log)
    {
        this(badClasses, badPackages, badMethods, log, null);
    }

    /**
     * @param badClasses fully-qualified names of the classes and interfaces whose methods are entirely blocked,
     *                   along with the methods of the classes extending or implementing them
     * @param badPackages names of the packages whose classes are entirely blocked (exact match, a class
     *                    extending a class of a restricted package from another package is not blocked)
     * @param badMethods method specs of the form <code>fully.qualified.ClassName.methodName</code>;
     *                   restriction applies to the named class and all its subclasses, all overloads
     * @param log logger
     * @param conversionHandler conversion handler, may be null
     * @since 2.5
     */
    public SecureIntrospectorImpl(String[] badClasses, String[] badPackages, String[] badMethods, Logger log, TypeConversionHandler conversionHandler)
    {
        super(log, conversionHandler);
        this.badClasses = badClasses == null ? new HashSet<>() : new HashSet<>(Arrays.asList(badClasses));
        this.badPackages = badPackages == null ? new HashSet<>() : new HashSet<>(Arrays.asList(badPackages));
        this.badMethods = parseRestrictedMethods(badMethods, log);
    }

    private static List<RestrictedMethod> parseRestrictedMethods(String[] entries, Logger log)
    {
        List<RestrictedMethod> result = new ArrayList<>();
        if (entries == null)
        {
            return result;
        }
        for (String entry : entries)
        {
            if (entry == null)
            {
                continue;
            }
            String spec = entry.trim();
            if (spec.isEmpty())
            {
                continue;
            }
            int dot = spec.lastIndexOf('.');
            if (dot <= 0 || dot == spec.length() - 1)
            {
                log.warn("Ignoring malformed introspector.restrict.methods entry: '{}' (expected fully.qualified.ClassName.methodName)", spec);
                continue;
            }
            String className = spec.substring(0, dot);
            String methodName = spec.substring(dot + 1);
            try
            {
                Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                result.add(new RestrictedMethod(clazz, methodName));
            }
            catch (ClassNotFoundException | LinkageError e)
            {
                log.warn("Cannot resolve class '{}' for introspector.restrict.methods entry '{}'; ignoring", className, spec);
            }
        }
        return result;
    }

    /**
     * Get the Method object corresponding to the given class, name and parameters.
     * Will check for appropriate execute permissions and return null if the method
     * is not allowed to be executed.
     *
     * @param clazz Class on which method will be called
     * @param methodName Name of method to be called
     * @param params array of parameters to method
     * @return Method object retrieved by Introspector
     * @throws IllegalArgumentException The parameter passed in were incorrect.
     */
    @Override
    public Method getMethod(Class<?> clazz, String methodName, Object[] params)
        throws IllegalArgumentException
    {
        if (!checkObjectExecutePermission(clazz, methodName))
        {
            log.warn("Cannot retrieve method {} from object of class {} due to security restrictions."
                     , methodName, clazz.getName());
            return null;
        }
        else
        {
            return super.getMethod(clazz, methodName, params);
        }
    }

    /**
     * Determine which methods and classes to prevent from executing.  Always blocks
     * methods wait() and notify().  Always allows the instance methods of Number, Boolean
     * and String.
     * Prohibits method calls on classes related to reflection and system operations.
     * For the complete list, see the properties <code>introspector.restrict.classes</code>,
     * <code>introspector.restrict.packages</code> and <code>introspector.restrict.methods</code>.
     * A restricted class or interface also restricts the classes extending or implementing it,
     * while a restricted package only restricts the classes it contains.
     *
     * @param clazz Class on which method will be called
     * @param methodName Name of method to be called
     * @see org.apache.velocity.util.introspection.SecureIntrospectorControl#checkObjectExecutePermission(java.lang.Class, java.lang.String)
     */
    @Override
    public boolean checkObjectExecutePermission(Class<?> clazz, String methodName)
    {
        /*
         * check for wait and notify
         */
        if (methodName != null &&
            (methodName.equals("wait") || methodName.equals("notify")) )
        {
            return false;
        }

        /*
         * Always allow the most common classes - Number, Boolean and String
         */
        else if (Number.class.isAssignableFrom(clazz))
        {
            return true;
        }
        else if (Boolean.class.isAssignableFrom(clazz))
        {
            return true;
        }
        else if (String.class.isAssignableFrom(clazz))
        {
            return true;
        }

        /*
         * Always allow Class.getName()
         */
        else if (Class.class.isAssignableFrom(clazz) &&
                 (methodName != null) && methodName.equals("getName"))
        {
            return true;
        }

       /*
       * Always disallow ClassLoader, Thread and subclasses
       */
        if (ClassLoader.class.isAssignableFrom(clazz) ||
                Thread.class.isAssignableFrom(clazz))
        {
            return false;
        }

        /*
         * check whether the class matches disallowed classes or packages
         */
        if (restricted.get(clazz))
        {
            return false;
        }

        /*
         * check method-level restrictions: blocked if methodName matches and the
         * restricted class is assignable from the target class (covers subclasses)
         */
        if (methodName != null)
        {
            for (RestrictedMethod bad : badMethods)
            {
                if (bad.methodName.equals(methodName) && bad.clazz.isAssignableFrom(clazz))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check a class (minus any array info) against disallowed classes and packages.
     */
    private boolean isRestricted(Class<?> clazz)
    {
        while (clazz.isArray())
        {
            clazz = clazz.getComponentType();
        }
        String className = clazz.getName();
        int dotPos = className.lastIndexOf('.');
        String packageName = (dotPos == -1) ? "" : className.substring(0, dotPos);
        return badPackages.contains(packageName) || inheritsBadClass(clazz);
    }

    /**
     * Look for a disallowed name among a class, its superclasses and all the interfaces they implement.
     * Names are compared rather than classes: an entry needs not be resolvable when the engine starts.
     */
    private boolean inheritsBadClass(Class<?> clazz)
    {
        if (clazz == null)
        {
            return false;
        }
        if (badClasses.contains(clazz.getName()))
        {
            return true;
        }
        for (Class<?> implemented : clazz.getInterfaces())
        {
            if (inheritsBadClass(implemented))
            {
                return true;
            }
        }
        return inheritsBadClass(clazz.getSuperclass());
    }

    private static final class RestrictedMethod
    {
        final Class<?> clazz;
        final String methodName;

        RestrictedMethod(Class<?> clazz, String methodName)
        {
            this.clazz = clazz;
            this.methodName = methodName;
        }
    }
}
