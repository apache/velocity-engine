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
 *  @version $Id: ClassUtils.java,v 1.2 2000/10/09 15:09:16 jvanzyl Exp $
 */
public class ClassUtils
{
    public static Object invoke(Object object, String method)
    {
        return invoke(object, method, null);
    }

    public static Object invoke(Object object, String method, Object[] args)
    {
        return invoke(object, method, args, null);
    }

    public static Object invoke(Object object, String method, 
        Object[] args, Class[] paramTypes)
    {
        if (args != null && paramTypes == null)
        {
            int size = args.length;
            paramTypes = new Class[size];
            for (int i = 0; i < size; i++)
            {
                paramTypes[i] = args[i].getClass();
                if (paramTypes[i] != String.class)
                    paramTypes[i] = Object.class;
            }                
        }            

        try
        {
            Class c = object.getClass();
            Method m = c.getMethod(method, paramTypes);
            Object o = m.invoke(object, args);
            return (o);
        }
        catch (Exception e)
        {
            return(null);
        }
    }

    public static boolean implementsInterface(Object object, String interfaceName)
    {
        int ii;
        Class c = object.getClass();
        
        Class[] interfaces = c.getInterfaces();

        for (ii = 0 ; ii < interfaces.length ; ++ii)
        {
            if (interfaceName.equals(interfaces[ii].getName()))
                break;
        }

        return (ii < interfaces.length);
    }

    public static boolean implementsMethod(Object object, String methodName)
    {
        int ii;
        Class c = object.getClass();
        
        Method[] methods = c.getMethods();

        for (ii = 0 ; ii < methods.length ; ++ii)
        {
            if (methodName.equals(methods[ii].getName()))
                break;
        }

        return (ii < methods.length);
    }

    public static boolean hasField(Object object, String fieldName)
    {
        int ii;
        Class c = object.getClass();
        
        Field[] fields = c.getFields();
        
        for (ii = 0 ; ii < fields.length ; ++ii)
        {
            if (fieldName.equals(fields[ii].getName()))
                break;
        }

        return (ii < fields.length);
    }
}
