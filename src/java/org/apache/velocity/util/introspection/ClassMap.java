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
 * A cache of introspection information for a specific class instance.
 * Keys {@link java.lang.Method} objects by a concatenation of the
 * method name and the names of classes that make up the parameters.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ClassMap.java,v 1.13 2001/09/10 10:36:09 geirm Exp $
 */
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
     * @return the class object whose methods are cached by this map.
     */
     Class getCachedClass()
     {
         return clazz;
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
        StringBuffer methodKey;

        /*
         *  get all publicly accessible methods
         */

        Method[] methods = getAccessibleMethods(clazz);

        /*
         * map and cache them
         */

        for (int i = 0; i < methods.length; i++)
        {
            Method method = methods[i];

            /*
             *  now get the 'public method', the method declared by a 
             *  public interface or class. (because the actual implementing
             *  class may be a facade...
             */

            Method publicMethod = getPublicMethod( method );

            /*
             *  it is entirely possible that there is no public method for
             *  the methods of this class (i.e. in the facade, a method
             *  that isn't on any of the interfaces or superclass
             *  in which case, ignore it.  Otherwise, map and cache
             */

            if ( publicMethod != null)
            {
                methodMap.add( publicMethod );
                methodCache.put(  makeMethodKey( publicMethod), publicMethod);
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

    /**
     * Retrieves public methods for a class. In case the class is not
     * public, retrieves methods with same signature as its public methods
     * from public superclasses and interfaces (if they exist). Basically
     * upcasts every method to the nearest acccessible method.
     */
    private static Method[] getAccessibleMethods(Class clazz)
    {
        Method[] methods = clazz.getMethods();
        
        /*
         *  Short circuit for the (hopefully) majority of cases where the
         *  clazz is public
         */
        
        if (Modifier.isPublic(clazz.getModifiers()))
        {
            return methods;
        }

        /*
         *  No luck - the class is not public, so we're going the longer way.
         */

        MethodInfo[] methodInfos = new MethodInfo[methods.length];

        for(int i = methods.length; i-- > 0; )
        {
            methodInfos[i] = new MethodInfo(methods[i]);
        }

        int upcastCount = getAccessibleMethods(clazz, methodInfos, 0);

        /*
         *  Reallocate array in case some method had no accessible counterpart.
         */

        if(upcastCount < methods.length)
        {
            methods = new Method[upcastCount];
        }

        int j = 0;
        for(int i = 0; i < methodInfos.length; ++i)
        {
            MethodInfo methodInfo = methodInfos[i];
            if(methodInfo.upcast)
            {
                methods[j++] = methodInfo.method;
            }
        }
        return methods;
    }

    /**
     *  Recursively finds a match for each method, starting with the class, and then
     *  searching the superclass and interfaces.
     *
     *  @param clazz Class to check
     *  @param methodInfos array of methods we are searching to match
     *  @param upcastCount current number of methods we have matched
     *  @return count of matched methods
     */
    private static int getAccessibleMethods( Class clazz, MethodInfo[] methodInfos, int upcastCount)
    {
        int l = methodInfos.length;
        
        /*
         *  if this class is public, then check each of the currently
         *  'non-upcasted' methods to see if we have a match
         */

        if( Modifier.isPublic(clazz.getModifiers()) )
        {
            for(int i = 0; i < l && upcastCount < l; ++i)
            {
                try
                {
                    MethodInfo methodInfo = methodInfos[i];

                    if(!methodInfo.upcast)
                    {
                        methodInfo.tryUpcasting(clazz);
                    }

                    upcastCount++;
                }
                catch(NoSuchMethodException e)
                {
                    /*
                     *  Intentionally ignored - it means
                     *  it wasn't found in the current class
                     */
                }
            }

            /*
             *  Short circuit if all methods were upcast
             */

            if(upcastCount == l)
            {
                return upcastCount;
            }
        }

        /*
         *   Examine superclass
         */

        Class superclazz = clazz.getSuperclass();

        if(superclazz != null)
        {
            upcastCount = getAccessibleMethods(superclazz , methodInfos, upcastCount);

            /*
             *  Short circuit if all methods were upcast
             */

            if(upcastCount == l)
            {
                return upcastCount;
            }
        }

        /*
         *  Examine interfaces. Note we do it even if superclazz == null.
         *  This is redundant as currently java.lang.Object does not implement
         *  any interfaces, however nothing guarantees it will not in future.
         */

        Class[] interfaces = clazz.getInterfaces();

        for(int i = interfaces.length; i-- > 0; )
        {
            upcastCount = getAccessibleMethods(interfaces[i], methodInfos, upcastCount);

            /*
             *  Short circuit if all methods were upcast
             */

            if(upcastCount == l)
            {
                return upcastCount;
            }
        }

        return upcastCount;
    }
    
    /**
     *  For a given method, retrieves its publicly accessible counterpart. 
     *  This method will look for a method with same name
     *  and signature declared in a public superclass or implemented interface of this 
     *  method's declaring class. This counterpart method is publicly callable.
     *
     *  @param method a method whose publicly callable counterpart is requested.
     *  @return the publicly callable counterpart method. Note that if the parameter
     *  method is itself declared by a public class, this method is an identity
     *  function.
     */
    public static Method getPublicMethod(Method method)
    {
        Class clazz = method.getDeclaringClass();
        
        /*
         *   Short circuit for (hopefully the majority of) cases where the declaring
         *   class is public.
         */

        if((clazz.getModifiers() & Modifier.PUBLIC) != 0)
        {
            return method;
        }

        return getPublicMethod(clazz, method.getName(), method.getParameterTypes());
    }

    /**
     *  Looks up the method with specified name and signature in the first public
     *  superclass or implemented interface of the class. 
     * 
     *  @param class the class whose method is sought
     *  @param name the name of the method
     *  @param paramTypes the classes of method parameters
     */
    private static Method getPublicMethod(Class clazz, String name, Class[] paramTypes)
    {
        /*
         *  if this class is public, then try to get it
         */

        if((clazz.getModifiers() & Modifier.PUBLIC) != 0)
        {
            try
            {
                return clazz.getMethod(name, paramTypes);
            }
            catch(NoSuchMethodException e)
            {
                /*
                 *  If the class does not have the method, then neither its
                 *  superclass nor any of its interfaces has it so quickly return
                 *  null.
                 */
                 return null;
            }
        }

        /*
         *  try the superclass
         */

 
        Class superclazz = clazz.getSuperclass();

        if ( superclazz != null )
        {
            Method superclazzMethod = getPublicMethod(superclazz, name, paramTypes);
            
            if(superclazzMethod != null)
            {
                return superclazzMethod;
            }
        }

        /*
         *  and interfaces
         */

        Class[] interfaces = clazz.getInterfaces();

        for(int i = 0; i < interfaces.length; ++i)
        {
            Method interfaceMethod = getPublicMethod(interfaces[i], name, paramTypes);
            
            if(interfaceMethod != null)
            {
                return interfaceMethod;
            }
        }

        return null;
    }

    /**
     *  Used for the iterative discovery process for public methods.
     */
    private static final class MethodInfo
    {
        Method method;
        String name;
        Class[] parameterTypes;
        boolean upcast;
        
        MethodInfo(Method method)
        {
            this.method = null;
            name = method.getName();
            parameterTypes = method.getParameterTypes();
            upcast = false;
        }
        
        void tryUpcasting(Class clazz)
            throws NoSuchMethodException
        {
            method = clazz.getMethod(name, parameterTypes);
            name = null;
            parameterTypes = null;
            upcast = true;
        }
    }
}
