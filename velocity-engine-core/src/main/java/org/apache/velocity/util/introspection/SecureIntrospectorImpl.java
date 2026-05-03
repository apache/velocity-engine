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
import java.util.List;

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
    private String[] badClasses;
    private String[] badPackages;
    private List<RestrictedMethod> badMethods;

    /**
     * Backwards-compatible constructor without method-level restrictions.
     */
    public SecureIntrospectorImpl(String[] badClasses, String[] badPackages, Logger log)
    {
        this(badClasses, badPackages, null, log);
    }

    /**
     * @param badClasses fully-qualified class names whose methods are entirely blocked (exact match)
     * @param badPackages package names whose classes are entirely blocked (exact match)
     * @param badMethods method specs of the form <code>fully.qualified.ClassName.methodName</code>;
     *                   restriction applies to the named class and all its subclasses, all overloads
     * @param log logger
     * @since 2.5
     */
    public SecureIntrospectorImpl(String[] badClasses, String[] badPackages, String[] badMethods, Logger log)
    {
        super(log);
        this.badClasses = badClasses == null ? new String[0] : badClasses;
        this.badPackages = badPackages == null ? new String[0] : badPackages;
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
     * methods wait() and notify().  Always allows methods on Number, Boolean, and String.
     * Prohibits method calls on classes related to reflection and system operations.
     * For the complete list, see the properties <code>introspector.restrict.classes</code>,
     * <code>introspector.restrict.packages</code> and <code>introspector.restrict.methods</code>.
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
         * check the classname (minus any array info)
         * whether it matches disallowed classes or packages
         */
        String className = clazz.getName();
        if (className.startsWith("[L") && className.endsWith(";"))
        {
            className = className.substring(2, className.length() - 1);
        }

        int dotPos = className.lastIndexOf('.');
        String packageName = (dotPos == -1) ? "" : className.substring(0, dotPos);

        for (String badPackage : badPackages)
        {
            if (packageName.equals(badPackage))
            {
                return false;
            }
        }

        for (String badClass : badClasses)
        {
            if (className.equals(badClass))
            {
                return false;
            }
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
