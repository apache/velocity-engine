package org.apache.velocity.util.introspection;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;

import java.lang.reflect.Method;

/**
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: MethodMap.java,v 1.10 2001/08/11 19:04:21 geirm Exp $
 */

public class MethodMap
{
    /**
     * Keep track of all methods with the same name.
     */
    Map methodByNameMap = new Hashtable();

    /**
     * Add a method to a list of methods by name.
     * For a particular class we are keeping track
     * of all the methods with the same name.
     */
    public void add(Method method)
    {
        String methodName = method.getName();

        List l = (List) methodByNameMap.get( methodName );

        if ( l == null)
        {
            l = new ArrayList();
            methodByNameMap.put(methodName, l);
        }            

        l.add(method);

        return;
    }
    
    /**
     * Return a list of methods with the same name.
     *
     * @param String key
     * @return List list of methods
     */
    public List get(String key)
    {
        return (List) methodByNameMap.get(key);
    }

    /**
     * Find a method.
     *
     * @param String name of method
     * @param Object[] params
     * @return Method
     */
    public Method find(String methodName, Object[] params)
    {
        List methodList = (List) methodByNameMap.get(methodName);
        
        if (methodList == null)
        {
            return null;
        }

        Class[] parameterTypes = null;
        Method  method = null;

        int numMethods = methodList.size();
        
        for (int i = 0; i < numMethods; i++)
        {
            method = (Method) methodList.get(i);
            parameterTypes = method.getParameterTypes();
            
            /*
             * The methods we are trying to compare must
             * the same number of arguments.
             */

            if (parameterTypes.length == params.length)
            {
                /* 
                 * Make sure the given parameter is a valid
                 * subclass of the method parameter in question.
                 */

                for (int j = 0; ; j++)
                {
                    if (j >= parameterTypes.length)
                        return method;

                    Class c = parameterTypes[j];
                    Object p = params[j];
                    if ( c.isPrimitive() )
                    {
                        try
                        {
                            if ( c != p.getClass().getField("TYPE").get(p) )
                                break;
                        } 
                        catch (Exception ex) 
                        {
                            break; // p is not a primitive derivate
                        }
                    }
                    else if ( (p != null) &&
                              !c.isAssignableFrom( p.getClass() ) )
                        break;
                }
            }
        }

        return null;
    }
}
