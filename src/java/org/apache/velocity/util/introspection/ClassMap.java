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

import java.util.Map;
import java.util.List;
import java.util.Hashtable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ClassMap.java,v 1.9 2001/05/14 04:43:36 geirm Exp $
 */

// TODO: public boolean (String[] list)
//       how to convert this so that it can be used
//       in VTL.

public class ClassMap
{
    private static final class CacheMiss { }
    private static final CacheMiss CACHE_MISS = new CacheMiss();
    private static final Object OBJECT = new Object();

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
     * of all the public classes / interfaces
     * that our class provides.
     */
    private void populateMethodCache()
    {
        /*
         *  start with the interfaces
         */

        Class[] classes = clazz.getInterfaces();

        for (int j = 0; j < classes.length; j++)
        {
            /*
             * if the class is public, then add it to the cache
             */

            if (Modifier.isPublic( classes[j].getModifiers()))
            {
                populateMethodCache( classes[j] );
            }
        }

        /*
         *  and now the classes
         */

        classes = clazz.getClasses();

        for (int j = 0; j < classes.length; j++)
        {
            /*
             * if the class is public, then add it to the cache
             */
            
            if (Modifier.isPublic( classes[j].getModifiers()))
            {
                populateMethodCache( classes[j] );
            }
        }
    }

    /**
     *  adds all public methods to the method cache
     *  and map
     *  @param claz  Class to analyze
     */
    private void populateMethodCache( Class claz )
    {
        /*
         * now, get all methods, from both interfaces
         * as well as super
         */

        Method[] methods = claz.getMethods();
        String  methodKey = null;

        for (int i = 0; i < methods.length; i++)
        {
            /*
             *  only care if the method is public
             */

            if (Modifier.isPublic(methods[i].getModifiers()))
            {
                methodKey = makeMethodKey( methods[i] );

                /*
                 *  Only add this if we don't already have it, because the method
                 *  key doesn't distinguish for which class/interface it 
                 *  belongs FOR THIS CLASS.  And it shouldn't matter.
                 */
                
                if( methodCache.get( methodKey ) == null)
                {
                    methodMap.add( methods[i] );
                    methodCache.put( methodKey, methods[i]);
                }
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
        {
            /*
             * If the argument type is primitive then we want
             * to convert our primitive type signature to the 
             * corresponding Object type so introspection for
             * methods with primitive types will work correctly.
             */
            if (parameterTypes[j].isPrimitive())
            {
                if (parameterTypes[j].equals(Boolean.TYPE))
                    methodKey.append("java.lang.Boolean");
                else if (parameterTypes[j].equals(Byte.TYPE))
                    methodKey.append("java.lang.Byte");
                else if (parameterTypes[j].equals(Character.TYPE))
                    methodKey.append("java.lang.Character");
                else if (parameterTypes[j].equals(Double.TYPE))
                    methodKey.append("java.lang.Double");
                else if (parameterTypes[j].equals(Float.TYPE))
                    methodKey.append("java.lang.Float");
                else if (parameterTypes[j].equals(Integer.TYPE))
                    methodKey.append("java.lang.Integer");
                else if (parameterTypes[j].equals(Long.TYPE))
                    methodKey.append("java.lang.Long");
                else if (parameterTypes[j].equals(Short.TYPE))
                    methodKey.append("java.lang.Short");
            }                
            else
                methodKey.append(parameterTypes[j].getName());
        }            
        
        return methodKey.toString();
    }

    private static String makeMethodKey(String method, Object[] params)
    {
        StringBuffer methodKey = new StringBuffer().append(method);
        
        for (int j = 0; j < params.length; j++)
        {
            if (params[j] == null)
                params[j] = OBJECT;
            methodKey.append(params[j].getClass().getName());
        }            
        
        return methodKey.toString();
    }
}
