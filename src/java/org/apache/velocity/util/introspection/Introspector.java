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
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import java.lang.reflect.Method;

import org.apache.velocity.runtime.RuntimeServices;

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
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @version $Id: Introspector.java,v 1.16 2001/09/26 11:19:04 geirm Exp $
 */
public class Introspector
{
    /*
     *  define a public string so that it can be looked for
     *  if interested
     */
     
    public final static String CACHEDUMP_MSG = 
        "Introspector : detected classloader change. Dumping cache.";
    
    private RuntimeServices rsvc = null;

    /**
     * Holds the method maps for the classes we know about, keyed by
     * Class object.
     */ 
    private final Map classMethodMaps = new HashMap();
    
    /**
     * Holds the qualified class names for the classes
     * we hold in the classMethodMaps hash
     */
    private Set cachedClassNames = new HashSet();

    /**
     *  Recieves our RuntimeServices object
     */
    public Introspector( RuntimeServices r )
    {
        this.rsvc = r;
    }
   
    /**
     * Gets the method defined by <code>name</code> and
     * <code>params</code> for the Class <code>c</code>.
     *
     * @return The desired Method object.
     */
    public Method getMethod(Class c, String name, Object[] params)
        throws Exception
    {
        if (c == null)
        {
            throw new Exception ( 
                "Introspector.getMethod(): Class method key was null: " + name );
        }                

        ClassMap classMap = null;
        
        synchronized(classMethodMaps)
        {
            classMap = (ClassMap)classMethodMaps.get(c);
          
            /*
             *  if we don't have this, check to see if we have it
             *  by name.  if so, then we have a classloader change
             *  so dump our caches.
             */
             
            if (classMap == null)
            {                
                if ( cachedClassNames.contains( c.getName() ))
                {
                    /*
                     * we have a map for a class with same name, but not
                     * this class we are looking at.  This implies a 
                     * classloader change, so dump
                     */
                    clearCache();                    
                    rsvc.info( CACHEDUMP_MSG );
                }
                 
                classMap = createClassMap(c);
            }
        }
        
        return classMap.findMethod(name, params);
    }

    /**
     * Creates a class map for specific class and registers it in the
     * cache.  Also adds the qualified name to the name->class map
     * for later Classloader change detection.
     */
    private ClassMap createClassMap(Class c)
    {        
        ClassMap classMap = new ClassMap(c);        
        classMethodMaps.put(c, classMap);
        cachedClassNames.add( c.getName() );

        return classMap;
    }

    /**
     * Clears the classmap and classname
     * caches
     */
    private void clearCache()
    {
        /*
         *  since we are synchronizing on this
         *  object, we have to clear it rather than
         *  just dump it.
         */            
        classMethodMaps.clear();
        
        /*
         * for speed, we can just make a new one
         * and let the old one be GC'd
         */
        cachedClassNames = new HashSet();
    }
}
