package org.apache.velocity.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.util.introspection.Uberspect;

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

    public void testEmptyProperties()
            throws Exception
    {
        Uberspect u = ri.getUberspect();
        Map map = new HashMap();

        // Don't screw up on empty propeties. That should map to get(Object) and put(Object, Object) on a Map
        assertNotNull(u.getPropertyGet(map, "", null));
        assertNotNull(u.getPropertySet(map, "", Object.class, null));
    }
    
    public void testNullProperties()
        throws Exception
    {
        Uberspect u = ri.getUberspect();
        Map map = new HashMap();

        // Don't screw up on null propeties. That should map to get(Object) and put(Object, Object) on a Map, too?
        assertNotNull(u.getPropertyGet(map, null, null));
        assertNotNull(u.getPropertySet(map, null, Object.class, null));
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
        



