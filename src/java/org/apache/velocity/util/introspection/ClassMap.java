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

/**
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @version $Id: ClassMap.java,v 1.3 2000/11/01 20:45:30 werken Exp $
 */

public class ClassMap
{
    static final private class CacheMiss { }
    
    static final private CacheMiss CACHE_MISS = new CacheMiss();

    /** 
     * Class passed into the constructor used to as
     * the basis for the Method map.
     */

    private Class clazz;

    /**
     * Cache of Methods, or CACHE_MISS, keyed by method
     * name and actual arguments used to find it.
     */
    private Map methodCache = new Hashtable();

    private MethodMap methodMap = new MethodMap();

    /**
     * Standard constructor
     */
    public ClassMap(Class clazz)
    {
        this.clazz = clazz;
        populateMethodCache();
    }
    
    /**
     * Find a Method using the methodKey
     * provided.
     *
     * Look in the methodMap for an entry.  If found,
     * it'll either be a CACHE_MISS, in which case we
     * simply give up, or it'll be a Method, in which
     * case, we return it.
     *
     * If nothing is found, then we must actually go
     * and introspect the method from the MethodMap.
     */
    public Method findMethod(String name, Object[] params)
    {
        String methodKey = makeMethodKey(name, params);

        Object cacheEntry = methodCache.get( methodKey );

        if (cacheEntry == CACHE_MISS)
        {
            return null;
        }

        if (cacheEntry == null)
        {
            cacheEntry = methodMap.find( name,
                                         params );
            
            if ( cacheEntry == null )
            {
                methodCache.put( methodKey,
                                 CACHE_MISS );
            }
            else
            {
                methodCache.put( methodKey,
                                 cacheEntry );
            }
        }

        // Yes, this might just be null.
        
        return (Method) cacheEntry;
    }
    
    /**
     * Populate the Map of direct hits. These
     * are taken from all the public methods
     * that our class provides.
     */
    private void populateMethodCache()
    {
        Method[] methods = clazz.getMethods();
        StringBuffer methodKey;

        for (int i = 0; i < methods.length; i++)
        {
            if (Modifier.isPublic(methods[i].getModifiers()))
            {
                methodMap.add(methods[i]);
                methodCache.put(makeMethodKey(methods[i]), methods[i]);
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
