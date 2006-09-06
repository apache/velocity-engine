package org.apache.velocity.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.test.misc.GetPutObject;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;

/*
 * Copyright 2006 The Apache Software Foundation.
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

public class UberspectorTestCase
        extends BaseTestCase
{
    private RuntimeInstance ri;

    public UberspectorTestCase(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
        return new TestSuite(UberspectorTestCase.class);
    }
    
    public void setUp()
            throws Exception
    {
        ri = new RuntimeInstance();
        ri.init();
    }

    public void testNullObjects()
            throws Exception
    {
        // How about some null objects... Gee, I'm mean. ;-) 
        Uberspect u = ri.getUberspect();

        VelPropertyGet getter = u.getPropertyGet(null, "foo", null);
        assertNull(getter);

        VelPropertySet setter = u.getPropertySet(null, "foo", Object.class, null);
        assertNull(setter);
    }

    public void testEmptyPropertyGetter()
            throws Exception
    {
        Uberspect u = ri.getUberspect();
        Map map = new HashMap();

        VelPropertyGet getter = u.getPropertyGet(map, "", null);

        // Don't screw up on empty properties. That should map to get(Object)
        assertNotNull(getter);
        assertEquals("Found wrong method", "get", getter. getMethodName());
    }

    public void testEmptyPropertySetter()
            throws Exception
    {
        Uberspect u = ri.getUberspect();
        Map map = new HashMap();

        VelPropertySet setter = u.getPropertySet(map, "", Object.class, null);

        // Don't screw up on empty properties. That should map to put(Object, Object)
        assertNotNull(setter);
        assertEquals("Found wrong method", "put", setter.getMethodName());
    }
    
    public void testNullPropertyGetter()
        throws Exception
    {
        Uberspect u = ri.getUberspect();
        GetPutObject gpo = new GetPutObject();

        VelPropertyGet getter = u.getPropertyGet(gpo, null, null);

        // Don't screw up on null properties. That should map to get() on the GPO.
        assertNotNull(getter);
        assertEquals("Found wrong method", "get", getter.getMethodName());
    }

    public void testNullPropertySetter()
        throws Exception
    {
        Uberspect u = ri.getUberspect();
        GetPutObject gpo = new GetPutObject();

        // Don't screw up on null properties. That should map to put() on the GPO.
        VelPropertySet setter = u.getPropertySet(gpo, null, Object.class, null); 
        assertNotNull(setter);
        assertEquals("Found wrong method", "put", setter.getMethodName());
    }

    /*
    
    public void testMapGetSet()
        throws Exception
    {
        Uberspect u = ri.getUberspect();
        Map map = new HashMap();

        VelPropertyGet getter = u.getPropertyGet(map, "", null);
        VelPropertySet setter = u.getPropertySet(map, "", Object.class, null);

        assertNotNull("Got a null getter", getter);
        assertNotNull("Got a null setter", setter);

        assertEquals("Got wrong getter", "foo", getter.getMethodName());
        assertEquals("Got wrong setter", "bar", setter.getMethodName());
    }
    */
}    
        



