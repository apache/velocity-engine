package org.apache.velocity.util.introspection;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import java.lang.reflect.Method;

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
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:szegedia@freemail.hu">Attila Szegedi</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @version $Id: IntrospectorBase.java,v 1.2.8.1 2004/03/03 23:23:08 geirm Exp $
 */
public class IntrospectorBase
{   
    /**
     * Holds the method maps for the classes we know about, keyed by
     * Class object.
     */ 
    protected  Map classMethodMaps = new HashMap();
    
    /**
     * Holds the qualified class names for the classes
     * we hold in the classMethodMaps hash
     */
    protected Set cachedClassNames = new HashSet();
   
    /**
     * Gets the method defined by <code>name</code> and
     * <code>params</code> for the Class <code>c</code>.
     *
     * @param c Class in which the method search is taking place
     * @param name Name of the method being searched for
     * @param params An array of Objects (not Classes) that describe the
     *               the parameters
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
    protected ClassMap createClassMap(Class c)
    {        
        ClassMap classMap = new ClassMap( c );        
        classMethodMaps.put(c, classMap);
        cachedClassNames.add( c.getName() );

        return classMap;
    }

    /**
     * Clears the classmap and classname
     * caches
     */
    protected void clearCache()
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
