/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.apache.velocity.util.introspection;

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
public class DeprecatedCheckUberspector extends AbstractChainableUberspector implements Uberspect, UberspectLoggable
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
