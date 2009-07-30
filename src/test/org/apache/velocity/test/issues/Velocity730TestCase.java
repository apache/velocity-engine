package org.apache.velocity.test.issues;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.VelocityContext;

/**
 * This class tests VELOCITY-730.
 */
public class Velocity730TestCase extends BaseTestCase
{
    public Velocity730TestCase(String name)
    {
        super(name);
        //DEBUG = true;
    }

    public void setUpContext(VelocityContext ctx)
    {
        ctx.put("foo", new Foo());
    }

    public void testIt()
    {
        String template = "$foo.foo #set( $foo.bar = 'foo' ) $foo.bar";
        assertEvalEquals("bar foo", template);
    }

    public static interface MyMap extends Map
    {
    }

    public abstract static class MyMapImpl
    {
        private HashMap map = new HashMap();

        protected Map map()
        {
            return map;
        }

        public void clear() 
        {
            map.clear();
        }

        public boolean containsKey(Object key) 
        {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) 
        {
            return map.containsValue(value);
        }

        public boolean isEmpty() 
        {
            return map.isEmpty();
        }

        public Set keySet() 
        {
            return map.keySet();
        }

        public void putAll(Map t) 
        {
            map.putAll(t);
        }

        public Object remove(Object key) 
        {
            return map.remove(key);
        }

        public int size() 
        {
            return map.size();
        }

        public Collection values() 
        {
            return map.values();
        }

        public Set entrySet()
        {
            return map.entrySet();
        }
    }

    private final static class Foo extends MyMapImpl implements MyMap
    {
        public Foo()
        {
            super();
            put("foo","bar");
        }

        public Object get(Object key) 
        {
            return map().get(key);
        }

        public Object put(Object k, Object v)
        {
            return map().put(k, v);
        }
    }

}
