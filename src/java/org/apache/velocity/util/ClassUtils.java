package org.apache.velocity.util;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class provides some methods for dynamically
 * invoking methods in objects
 *
 *  @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 *  @version $Id: ClassUtils.java,v 1.3 2000/10/15 18:29:14 dlr Exp $
 */
public class ClassUtils
{
    /**
     * Invokes the specified method of the provided object.
     *
     * @param object The object whose method to call.
     * @param method The name of the method to invoke.
     * @return       The return value of the method call.
     */
    public static Object invoke(Object object, String method)
    {
        return invoke(object, method, null);
    }

    /**
     * Invokes the specified method of the provided object.
     *
     * @param object The object whose method to call.
     * @param method The name of the method to invoke.
     * @param args   The arguments to pass to the method.
     * @return       The return value of the method call.
     */
    public static Object invoke(Object object, String method, Object[] args)
    {
        return invoke(object, method, args, null);
    }

    /**
     * Invokes the specified method of the provided object.
     *
     * @param object     The object whose method to call.
     * @param method     The name of the method to invoke.
     * @param args       The arguments to pass to the method.
     * @param paramTypes The class types of the arguments.
     * @return           The return value of the method call.
     */
    public static Object invoke(Object object, String method, 
                                Object[] args, Class[] paramTypes)
    {
        if (args != null && paramTypes == null)
        {
            // Discover parameter types for the provided args.
            int size = args.length;
            paramTypes = new Class[size];
            for (int i = 0; i < size; i++)
            {
                paramTypes[i] = args[i].getClass();
                if (paramTypes[i] != String.class)
                    paramTypes[i] = Object.class;
            }                
        }            

        // Call the object's method.
        try
        {
            Class c = object.getClass();
            Method m = c.getMethod(method, paramTypes);
            Object o = m.invoke(object, args);
            return o;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Checks whether the provided object implements a given interface.
     *
     * @param object        The object to check.
     * @param interfaceName The interface to check for.
     * @return              Whether the interface is implemented.
     */
    public static boolean implementsInterface(Object object, 
                                              String interfaceName)
    {
        int i;
        
        Class[] interfaces = object.getClass().getInterfaces();
        for (i = 0 ; i < interfaces.length ; ++i)
        {
            if (interfaceName.equals(interfaces[i].getName()))
                break;
        }

        return (i < interfaces.length);
    }

    /**
     * Checks whether the provided object implements a given method.
     *
     * @param object     The object to check.
     * @param methodName The method to check for.
     * @return           Whether the method is implemented.
     */
    public static boolean implementsMethod(Object object, String methodName)
    {
        int m;
        
        Method[] methods = object.getClass().getMethods();
        for (m = 0 ; m < methods.length ; ++m)
        {
            if (methodName.equals(methods[m].getName()))
                break;
        }

        return (m < methods.length);
    }

    /**
     * Checks whether the provided object has a given field.
     *
     * @param object    The object to check.
     * @param fieldName The field to check for.
     * @return          Whether the field is had.
     */
    public static boolean hasField(Object object, String fieldName)
    {
        int f;
        
        Field[] fields = object.getClass().getFields();
        for (f = 0 ; f < fields.length ; ++f)
        {
            if (fieldName.equals(fields[f].getName()))
                break;
        }

        return (f < fields.length);
    }
}
