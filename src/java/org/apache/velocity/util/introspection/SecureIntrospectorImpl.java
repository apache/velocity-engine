package org.apache.velocity.util.introspection;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.lang.reflect.Method;

import org.apache.velocity.runtime.log.Log;

/**
 * <p>Prevent "dangerous" classloader/reflection related calls.  Use this 
 * introspector for situations in which template writers are numerous
 * or untrusted.  Specifically, this introspector prevents creation of
 * arbitrary objects and prevents reflection on objects.
 * 
 * <p>See documentation of checkObjectExecutePermission() for 
 * more information on specific classes and methods blocked.
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class SecureIntrospectorImpl extends Introspector implements SecureIntrospectorControl
{
    private String[] badClasses;
    private String[] badPackages;
    
    public SecureIntrospectorImpl(String[] badClasses, String[] badPackages, Log log)
    {
        super(log);
        this.badClasses = badClasses;
        this.badPackages = badPackages;
    }
    
    
    public Method getMethod(Class c, String name, Object[] params) throws Exception
    {
        if (!checkObjectExecutePermission(c,name))
        {
            log.warn ("Cannot retrieve method " + name + 
                      " from object of class " + c.getName() +
                      " due to security restrictions.");
            return null;
            
        }
        else
        {
            return super.getMethod(c, name, params);
        }
    }
    
    /**
     * Determine which methods and classes to prevent from executing.  Always blocks
     * methods wait() and notify().  Always allows methods on Number, Boolean, and String.
     * Prohibits method calls on classes related to reflection and system operations.
     * For the complete list, see the properties <code>introspector.restrict.classes</code>
     * and <code>introspector.restrict.packages</code>.
     * 
     * @see org.apache.velocity.util.introspection.SecureIntrospectorControl#checkObjectExecutePermission(java.lang.Class, java.lang.String)
     */
    public boolean checkObjectExecutePermission(Class clazz, String method)
    {
        if (method == null)
        {
            return false;
        }
        
        /**
         * check for wait and notify 
         */
        if ( method.equals("wait") || method.equals("notify") )
        {
            return false;
        }
        
        /**
         * Always allow the most common classes - Number, Boolean and String
         */
        else if (java.lang.Number.class.isAssignableFrom(clazz))
        {
            return true;
        }
        
        else if (java.lang.Boolean.class.isAssignableFrom(clazz))
        {
            return true;
        }
        
        else if (java.lang.String.class.isAssignableFrom(clazz))
        {
            return true;
        }
        
        /**
         * Always allow Class.getName()
         */
        else if (java.lang.Class.class.isAssignableFrom(clazz) && method.equals("getName"))
        {
            return true;
        }
        
        /**
         * check the classname (minus any array info)
         * whether it matches disallowed classes or packages
         */ 
        String className = clazz.getName();
        if (className.startsWith("[L") && className.endsWith(";"))
        {
            className = className.substring(2,className.length() - 1);
        }
        
        String packageName;
        int dotPos = className.lastIndexOf('.');
        packageName = (dotPos == -1) ? "" : className.substring(0,dotPos);
        
        int sz = badPackages.length;
        for (int i = 0; i < sz; i++)
        {
            if (packageName.equals(badPackages[i]))
            {
                return false;
            }
        }
        
        sz = badClasses.length;
        for (int i = 0; i < sz; i++)
        {
            if (className.equals(badClasses[i]))
            {
                return false;
            }
        }
        
        return true;
    }
}
