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

import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * GetExecutor that is smart about Maps. If it detects one, it does not
 * use Reflection but a cast to access the getter.
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 * @since 1.5
 */
public class MapGetExecutor
        extends AbstractExecutor
{
    private final String property;
    private final boolean isAlive;

    public MapGetExecutor(final Logger log, final Object object, final String property)
    {
        this.log = log;
        this.property = property;
        isAlive = discover(object);
    }

    @Override
    public Method getMethod()
    {
        if (isAlive())
        {
            return MapGetMethod.instance();
        }
        return null;
    }

    @Override
    public boolean isAlive()
    {
        return isAlive;
    }

    protected boolean discover (final Object object)
    {
        if (object instanceof Map)
        {
            if (property != null)
            {
                return true;
            }
        }
        return false;
    }

    public Object execute(final Object o)
    {
        return ((Map) o).get(property);
    }

    private static final class MapGetMethod
    {
        private static final Method instance;

        static
        {
            try
            {
                instance = Map.class.getMethod("get", new Class[]{Object.class});
            }
            catch (final NoSuchMethodException mapGetMethodMissingError)
            {
                throw new Error(mapGetMethodMissingError);
            }
        }

        private MapGetMethod() { }

        static Method instance()
        {
            return instance;
        }
    }
}
