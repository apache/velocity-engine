package org.apache.velocity.runtime.parser.node;
/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.util.introspection.Introspector;

import java.lang.reflect.InvocationTargetException;
import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.runtime.RuntimeLogger;


/**
 * Executor that simply tries to execute a get(key)
 * operation. This will try to find a get(key) method
 * for any type of object, not just objects that
 * implement the Map interface as was previously
 * the case.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: GetExecutor.java,v 1.8.4.1 2004/03/03 23:22:59 geirm Exp $
 */
public class GetExecutor extends AbstractExecutor
{
    /**
     * Container to hold the 'key' part of 
     * get(key).
     */
    private Object[] args = new Object[1];
    
    /**
     * Default constructor.
     */
    public GetExecutor(RuntimeLogger r, Introspector ispect, Class c, String key)
        throws Exception
    {
        rlog = r;
        args[0] = key;
        method = ispect.getMethod(c, "get", args);
    }

    /**
     * Execute method against context.
     */
    public Object execute(Object o)
        throws IllegalAccessException, InvocationTargetException
    {
        if (method == null)
            return null;

        return method.invoke(o, args);
    }

    /**
     * Execute method against context.
     */
    public Object OLDexecute(Object o, InternalContextAdapter context)
        throws IllegalAccessException, MethodInvocationException
    {
        if (method == null)
            return null;
     
        try 
        {
            return method.invoke(o, args);  
        }
        catch(InvocationTargetException ite)
        {
            /*
             *  the method we invoked threw an exception.
             *  package and pass it up
             */

            throw  new MethodInvocationException( 
                "Invocation of method 'get(\"" + args[0] + "\")'" 
                + " in  " + o.getClass() 
                + " threw exception " 
                + ite.getTargetException().getClass(), 
                ite.getTargetException(), "get");
        }
        catch(IllegalArgumentException iae)
        {
            return null;
        }
    }
}









