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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeLogger;

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
 * @version $Id: Introspector.java,v 1.21.4.1 2004/03/03 23:23:08 geirm Exp $
 */
public class Introspector extends IntrospectorBase
{
    /**
     *  define a public string so that it can be looked for
     *  if interested
     */
     
    public final static String CACHEDUMP_MSG = 
        "Introspector : detected classloader change. Dumping cache.";

    /**
     *  our engine runtime services
     */
    private RuntimeLogger rlog = null;

    /**
     *  Recieves our RuntimeServices object
     */
    public Introspector(RuntimeLogger logger)
    {
        this.rlog = logger;
    }
   
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
        /*
         *  just delegate to the base class
         */

        try
        {
            return super.getMethod( c, name, params );
        }
        catch( MethodMap.AmbiguousException ae )
        {
            /*
             *  whoops.  Ambiguous.  Make a nice log message and return null...
             */

            String msg = "Introspection Error : Ambiguous method invocation "
                + name + "( ";

            for (int i = 0; i < params.length; i++)
            {
                if ( i > 0)
                    msg = msg + ", ";
                
                msg = msg + params[i].getClass().getName();
            }
            
            msg = msg + ") for class " + c;
            
            rlog.error( msg );
        }

        return null;
    }

    /**
     * Clears the classmap and classname
     * caches, and logs that we did so
     */
    protected void clearCache()
    {
        super.clearCache();
        rlog.info( CACHEDUMP_MSG );
    }
}
