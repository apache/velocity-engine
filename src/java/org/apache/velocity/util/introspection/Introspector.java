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

import java.util.Hashtable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.velocity.util.StringUtils;

/**
 * This basic function of this class is to return a Method
 * object for a particular class given the name of a method
 * and the parameters to the method in the form of an Object[]
 *
 * The first time the Introspector sees a 
 * class it creates a class method map for the
 * class in question. Basically the class method map
 * is a Hastable where Method objects are keyed by a
 * concatenation of the method name and the names of
 * classes that make up the parameters.
 *
 * For example, a method with the following signature:
 *
 * public void method(String a, StringBuffer b)
 *
 * would be mapped by the key:
 *
 * "method" + "java.lang.String" + "java.lang.StringBuffer"
 *
 * This mapping is performed for all the methods in a class
 * and stored for 
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @version $Id: Introspector.java,v 1.8 2000/11/25 18:27:27 jon Exp $
 */

// isAssignable checks for arguments that are subclasses
// DirectHit map
// DirectMiss map

public class Introspector
{
    private static Hashtable classMethodMaps = new Hashtable();

    public static Method getMethod(Class c, String name, Object[] params)
        throws Exception
    {
        if (c == null)
            throw new Exception ( "Introspector.getMethod(): Class method key was null: " + name );

        // If this is the first time seeing this class
        // then create a method map for this class and
        // store it in Hashtable of class method maps.
        if (!classMethodMaps.containsKey(c))
        {
            // Lots of threads might be whizzing through here,
            // so we do a double-checked lock, which only involves
            // synchronization when there's a key-miss.  Avoids
            // doing duplicate work, and constructing objects twice
            // in particular race conditions

            // Though, some folks say that double-checked-locking
            // doesn't necessarily work-as-expected in Java on
            // multi-proc machines.  Doesn't make things worse,
            // but just doesn't help as much as you'd imagine it
            // would.  Darn re-ordering of instructions.
        
            synchronized (classMethodMaps)
            {
                if (!classMethodMaps.containsKey(c))
                {
                    classMethodMaps.put(c, new ClassMap(c));
                }
            }
        }
        return findMethod(c, name, params);
    }

    private static Method findMethod(Class c, String name, Object[] params)
    {
        ClassMap classMethodMap = (ClassMap) classMethodMaps.get(c);
        return classMethodMap.findMethod(name, params);
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
            if (methodName.equals(methods[m].getName()))
                break;
        
        return (m < methods.length);
    }
}
