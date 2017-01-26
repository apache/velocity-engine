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

/**
 * Chainable Uberspector that checks for deprecated method calls. It does that by checking if the returned
 * method has a Deprecated annotation. Because this is a chainable uberspector, it has to re-get the method using a
 * default introspector, which is not safe; future uberspectors might not be able to return a precise method name, or a
 * method of the original target object.
 *
 * Borrowed from the XWiki project.
 *
 * @since 2.0
 * @version $Id:$
 * @see ChainableUberspector
 */
public class DeprecatedCheckUberspector extends AbstractChainableUberspector implements Uberspect
{
    @Override
    public void init()
    {
        super.init();
        this.introspector = new Introspector(this.log);
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
    {
        VelMethod method = super.getMethod(obj, methodName, args, i);
        if (method != null) {
            Method m = this.introspector.getMethod(obj.getClass(), method.getMethodName(), args);
            if (m != null
                && (m.isAnnotationPresent(Deprecated.class)
                    || m.getDeclaringClass().isAnnotationPresent(Deprecated.class)
                    || obj.getClass().isAnnotationPresent(Deprecated.class))) {
                logWarning("method", obj, method.getMethodName(), i);
            }
        }

        return method;
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
    {
        VelPropertyGet method = super.getPropertyGet(obj, identifier, i);
        if (method != null) {
            Method m = this.introspector.getMethod(obj.getClass(), method.getMethodName(), new Object[] {});
            if (m != null
                && (m.isAnnotationPresent(Deprecated.class)
                    || m.getDeclaringClass().isAnnotationPresent(Deprecated.class)
                    || obj.getClass().isAnnotationPresent(Deprecated.class))) {
                logWarning("getter", obj, method.getMethodName(), i);
            }
        }

        return method;
    }

    @Override
    public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info i)
    {
        // TODO Auto-generated method stub
        VelPropertySet method = super.getPropertySet(obj, identifier, arg, i);
        if (method != null) {
            Method m = this.introspector.getMethod(obj.getClass(), method.getMethodName(), new Object[] { arg });
            if (m != null
                && (m.isAnnotationPresent(Deprecated.class)
                    || m.getDeclaringClass().isAnnotationPresent(Deprecated.class)
                    || obj.getClass().isAnnotationPresent(Deprecated.class))) {
                logWarning("setter", obj, method.getMethodName(), i);
            }
        }

        return method;
    }

    /**
     * Helper method to log a warning when a deprecation has been found.
     *
     * @param deprecationType the type of deprecation (eg "getter", "setter", "method")
     * @param object the object that has a deprecation
     * @param methodName the deprecated method's name
     * @param info a Velocity {@link org.apache.velocity.util.introspection.Info} object containing information about
     *            where the deprecation was located in the Velocity template file
     */
    private void logWarning(String deprecationType, Object object, String methodName, Info info)
    {
        this.log.warn("Deprecated usage of {} [{}] in {}@{},{}", deprecationType, object.getClass()
            .getCanonicalName() + "." + methodName, info.getTemplateName(), info.getLine(), info.getColumn());
    }
}
