package org.apache.velocity.util.introspection;

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

import java.util.Map;
import java.util.Hashtable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClassMap
{
    /** 
     * Class passed into the constructor used to as
     * the basis for the Method map.
     */
    private Class clazz;
    /**
     * Map of methods that can be accessed directly
     * with a method key.
     */
    private Map directHits = new Hashtable();
    /**
     * Map of nulls that represent methodKeys that
     * will never return a valid method.
     */
    private Map directMisses = new Hashtable();

    private MethodMap methodMap = new MethodMap();

    /**
     * Standard constructor
     */
    public ClassMap(Class clazz)
    {
        this.clazz = clazz;
        populateDirectHits();
    }
    
    /**
     * Find a Method using the methodKey
     * provided. First try a direct hit, if
     * that doesn't work then we have to do some
     * work to find out if we can return a Method
     * or not. Will implement this ASAP. If we
     * find a valid Method then we can generate
     * a methodKey and add it to the
     * directHits Map.
     */
    public Method findMethod(String name, Object[] params)
    {
        String methodKey = makeMethodKey(name, params);
        
        if (directMisses.containsKey(methodKey))
            return null;
            
        if (directHits.containsKey(methodKey))
            return (Method) directHits.get(methodKey);
        else
        {
            Method method = methodMap.find(name, params);
            
            if (method == null)
                directMisses.put(methodKey, "");
            else
            {
                directHits.put(methodKey, method);
                return method;
            }                
        }
                    
        return null;
    }
    
    /**
     * Populate the Map of direct hits. These
     * are taken from all the public methods
     * that our class provides.
     */
    private void populateDirectHits()
    {
        Method[] methods = clazz.getMethods();
        StringBuffer methodKey;

        for (int i = 0; i < methods.length; i++)
        {
            if (Modifier.isPublic(methods[i].getModifiers()))
            {
                methodMap.add(methods[i]);
                directHits.put(makeMethodKey(methods[i]), methods[i]);
            }
        }            
    }

    /**
     * Make a methodKey for the given method using
     * the concatenation of the name and the
     * types of the method parameters.
     */
    private String makeMethodKey(Method method)
    {
        Class[] parameterTypes = method.getParameterTypes();
        
        StringBuffer methodKey = new StringBuffer().append(method.getName());
        
        for (int j = 0; j < parameterTypes.length; j++)
            methodKey.append(parameterTypes[j].getName());

        return methodKey.toString();
    }

    private static String makeMethodKey(String method, Object[] params)
    {
        StringBuffer methodKey = new StringBuffer().append(method);
        
        for (int j = 0; j < params.length; j++)
            methodKey.append(params[j].getClass().getName());

        return methodKey.toString();
    }
}
