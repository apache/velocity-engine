package org.apache.velocity.util;

import java.io.InputStream;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Simple utility functions for manipulating classes and resources
 * from the classloader.  
 *
 *  @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 *  @version $Id$
 */
public class ClassUtils {

    /**
     * Utility class; cannot be instantiated.
     */
    private ClassUtils() 
    {
    }
    
    /**
     * Return a new instance of the given class.  Checks the ThreadContext
     * classloader first, then uses the System classloader.  Should replace all calls
     * to <code>Class.forName( claz ).newInstance()</code> (which only calls
     * the System class loader) when the class might be in a different classloader
     * (e.g. in a webapp).
     *
     * @param classname the name of the class to instantiate
     * @return
     */
    public static Object getNewInstance(String classname) 
        throws ClassNotFoundException,IllegalAccessException,InstantiationException
    {
        /**
         * Use the Thread context classloader if possible
         */
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) 
        {
            try 
            {
                return Class.forName(classname, true, loader).newInstance();
            } 
            catch (ClassNotFoundException E) {
                
                /**
                 * If not found with ThreadContext loader, try System classloader
                 * (works around bug in ant).
                 */
                return Class.forName(classname).newInstance();
            }
        }
        
        /**
         * No Thread context classloader, so use system loader.
         */
        else 
        {
            return Class.forName(classname).newInstance();
        }

    }
    
    /**
     * Finds a resource with the given name.  Checks the Thread Context
     * classloader, then uses the System classloader.  Should replace all 
     * calls to <code>Class.getResourceAsString</code> when the resource
     * might come from a different classloader.  (e.g. a webapp).
     * @param claz Class to use when getting the System classloader (used if no Thread
     * Context classloader available or fails to get resource). 
     * @param name name of the resource
     * @return
     */
    public static InputStream getResourceAsStream(Class claz, String name)
    {
        InputStream result = null;
        
        /**
         * remove leading slash so path will work with classes in a JAR file
         */        
        while (name.startsWith("/"))
        {
            name = name.substring(1);
        }
        
        ClassLoader classLoader = Thread.currentThread()
                                    .getContextClassLoader();

        if (classLoader == null) 
        {
            classLoader = claz.getClassLoader();
            result = classLoader.getResourceAsStream( name );
        } 
        else 
        {
            result= classLoader.getResourceAsStream( name );
    
            /**
            * for compatibility with texen / ant tasks, fall back to 
            * old method when resource is not found.
            */
            
            if (result == null) 
            {
                classLoader = claz.getClassLoader();
                if (classLoader != null)
                    result = classLoader.getResourceAsStream( name );
            }
        }

        return result;

    }


}
