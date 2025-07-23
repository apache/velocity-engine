package org.apache.velocity.runtime.parser.node;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.util.ArrayListWrapper;
import org.apache.velocity.util.introspection.Introspector;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Returned the value of object property when executed.
 */
public class PropertyExecutor extends AbstractExecutor
{
    private final Introspector introspector;
    private final boolean wrapArray;

    /**
     * @param log
     * @param introspector
     * @param clazz
     * @param property
     * @since 1.5
     */
    public PropertyExecutor(final Logger log, final Introspector introspector,
                            final Class<?> clazz, final String property)
    {
        this(log, introspector, clazz, property, false);
    }

    /**
     * @param log
     * @param introspector
     * @param clazz
     * @param property
     * @param wrapArray
     * @since 1.5
     */
    public PropertyExecutor(final Logger log, final Introspector introspector,
            final Class<?> clazz, final String property, final boolean wrapArray)
    {
        this.log = log;
        this.introspector = introspector;
        this.wrapArray = wrapArray;

        // Don't allow passing in the empty string or null because
        // it will either fail with a StringIndexOutOfBounds error
        // or the introspector will get confused.
        if (StringUtils.isNotEmpty(property))
        {
            discover(clazz, property);
        }
    }

    /**
     * @return The current introspector.
     * @since 1.5
     */
    protected Introspector getIntrospector()
    {
        return this.introspector;
    }

    /**
     * @param clazz
     * @param property
     */
    protected void discover(final Class<?> clazz, final String property)
    {
        /*
         *  this is gross and linear, but it keeps it straightforward.
         */

        try
        {
            Object [] params = {};

            StringBuilder sb = new StringBuilder("get");
            sb.append(property);

            setMethod(introspector.getMethod(clazz, sb.toString(), params));

            if (!isAlive())
            {
                /*
                 *  now the convenience, flip the 1st character
                 */

                char c = sb.charAt(3);

                if (Character.isLowerCase(c))
                {
                    sb.setCharAt(3, Character.toUpperCase(c));
                }
                else
                {
                    sb.setCharAt(3, Character.toLowerCase(c));
                }

                setMethod(introspector.getMethod(clazz, sb.toString(), params));
            }

            // Check if no valid method was found and
            // the class is not a Map before trying record-style property access
            if (!isAlive() && !Map.class.isAssignableFrom(clazz))
            {
                /*
                 * If no JavaBean property was found, try the convention used by Java 16 records.
                 * No convenience case flip because the more convenient lowerCamelCase is also the
                 * more likely to be used in the record itself.
                 */
                setMethod(introspector.getMethod(clazz, property, params));
            }
        }
        /*
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            throw e;
        }
        catch(Exception e)
        {
            String msg = "Exception while looking for property getter for '" + property + "'";
            log.error(msg, e);
            throw new VelocityException(msg, e);
        }
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.AbstractExecutor#execute(java.lang.Object)
     */
    @Override
    public Object execute(Object o)
        throws IllegalAccessException,  InvocationTargetException
    {
        if (wrapArray)
        {
            o = new ArrayListWrapper(o);
        }
        return isAlive() ? getMethod().invoke(o, ((Object []) null)) : null;
    }
}
