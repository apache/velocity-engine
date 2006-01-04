package org.apache.velocity.runtime.parser.node;
/*
 * Copyright 2000-2004 The Apache Software Foundation.
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

import java.lang.reflect.InvocationTargetException;

import org.apache.velocity.runtime.RuntimeLogger;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.log.RuntimeLoggerLog;
import org.apache.velocity.util.introspection.Introspector;

/**
 * Returned the value of object property when executed.
 */
public class PropertyExecutor extends AbstractExecutor
{
    protected Introspector introspector = null;

    protected String methodUsed = null;

    public PropertyExecutor(Log log, Introspector ispctr,
                            Class clazz, String property)
    {
        this.log = log;
        introspector = ispctr;

        discover(clazz, property);
    }

    /**
     * @deprecated RuntimeLogger is deprecated. Use the other constructor.
     */
    public PropertyExecutor(RuntimeLogger r, Introspector ispctr,
                            Class clazz, String property)
    {
        this(new RuntimeLoggerLog(r), ispctr, clazz, property);
    }

    protected void discover(Class clazz, String property)
    {
        /*
         *  this is gross and linear, but it keeps it straightforward.
         */

        try
        {
            char c;
            StringBuffer sb;

            Object[] params = {  };

            /*
             *  start with get<property>
             *  this leaves the property name
             *  as is...
             */
            sb = new StringBuffer("get");
            sb.append(property);

            methodUsed = sb.toString();

            method = introspector.getMethod(clazz, methodUsed, params);

            if (method != null)
            {
                return;
            }

            /*
             *  now the convenience, flip the 1st character
             */

            sb = new StringBuffer("get");
            sb.append(property);

            c = sb.charAt(3);

            if (Character.isLowerCase(c))
            {
                sb.setCharAt(3, Character.toUpperCase(c));
            }
            else
            {
                sb.setCharAt(3, Character.toLowerCase(c));
            }

            methodUsed = sb.toString();
            method = introspector.getMethod(clazz, methodUsed, params);

            if (method != null)
            {
                return;
            }

        }
        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            throw e;
        }
        catch(Exception e)
        {
            log.error("PROGRAMMER ERROR : PropertyExector()", e);
        }
    }


    /**
     * Execute method against context.
     */
    public Object execute(Object o)
        throws IllegalAccessException,  InvocationTargetException
    {
        if (method == null)
        {
            return null;
        }

        return method.invoke(o, ((Object []) null));
    }
}
