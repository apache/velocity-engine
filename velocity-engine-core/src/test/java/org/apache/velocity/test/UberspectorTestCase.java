package org.apache.velocity.test;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.test.misc.GetPutObject;
import org.apache.velocity.test.misc.UberspectorTestObject;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;

import java.util.HashMap;
import java.util.Map;


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

        // Don't screw up on empty properties. That should map to get("")
        assertNotNull(getter);
        assertEquals("Found wrong method", "get", getter.getMethodName());
    }

    public void testEmptyPropertySetter()
            throws Exception
    {
        Uberspect u = ri.getUberspect();
        Map map = new HashMap();

        VelPropertySet setter = u.getPropertySet(map, "", Object.class, null);

        // Don't screw up on empty properties. That should map to put("", Object)
        assertNotNull(setter);
        assertEquals("Found wrong method", "put", setter.getMethodName());
    }

    public void testNullPropertyGetter()
        throws Exception
    {
        Uberspect u = ri.getUberspect();
        GetPutObject gpo = new GetPutObject();
        Map map = new HashMap();

        VelPropertyGet getter = u.getPropertyGet(gpo, null, null);

        // Don't screw up on null properties. That should map to get() on the GPO.
        assertNotNull(getter);
        assertEquals("Found wrong method", "get", getter.getMethodName());

        // And should be null on a Map which does not have a get()
        getter = u.getPropertyGet(map, null, null);
        assertNull(getter);

    }

    public void testNullPropertySetter()
        throws Exception
    {
        Uberspect u = ri.getUberspect();
        GetPutObject gpo = new GetPutObject();
        Map map = new HashMap();

        // Don't screw up on null properties. That should map to put() on the GPO.
        VelPropertySet setter = u.getPropertySet(gpo, null, "", null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "put", setter.getMethodName());

        // And should be null on a Map which does not have a put()
        setter = u.getPropertySet(map, null, "", null);
        assertNull(setter);
    }

    public void testNullParameterType()
            throws Exception
    {
        VelPropertySet setter;

        Uberspect u = ri.getUberspect();
        UberspectorTestObject uto = new UberspectorTestObject();

        // setRegular()
        setter = u.getPropertySet(uto, "Regular", null, null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setRegular", setter.getMethodName());

        // setAmbigous() - String and StringBuffer available
        setter = u.getPropertySet(uto, "Ambigous", null, null);
        assertNull(setter);

        // setAmbigous() - same with Object?
        setter = u.getPropertySet(uto, "Ambigous", new Object(), null);
        assertNull(setter);
    }

    public void testMultipleParameterTypes()
            throws Exception
    {
        VelPropertySet setter;

        Uberspect u = ri.getUberspect();
        UberspectorTestObject uto = new UberspectorTestObject();

        // setAmbigous() - String
        setter = u.getPropertySet(uto, "Ambigous", "", null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setAmbigous", setter.getMethodName());

        // setAmbigous() - StringBuffer
        setter = u.getPropertySet(uto, "Ambigous", new StringBuffer(), null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setAmbigous", setter.getMethodName());
    }


    public void testRegularGetters()
            throws Exception
    {
        VelPropertyGet getter;

        Uberspect u = ri.getUberspect();
        UberspectorTestObject uto = new UberspectorTestObject();

        // getRegular()
        getter = u.getPropertyGet(uto, "Regular", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "getRegular", getter.getMethodName());

        // Lowercase regular
        getter = u.getPropertyGet(uto, "regular", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "getRegular", getter.getMethodName());

        // lowercase: getpremium()
        getter = u.getPropertyGet(uto, "premium", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "getpremium", getter.getMethodName());

        // test uppercase: getpremium()
        getter = u.getPropertyGet(uto, "Premium", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "getpremium", getter.getMethodName());
    }

    public void testBooleanGetters()
            throws Exception
    {
        VelPropertyGet getter;

        Uberspect u = ri.getUberspect();
        UberspectorTestObject uto = new UberspectorTestObject();

        // getRegular()
        getter = u.getPropertyGet(uto, "RegularBool", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "isRegularBool", getter.getMethodName());

        // Lowercase regular
        getter = u.getPropertyGet(uto, "regularBool", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "isRegularBool", getter.getMethodName());

        // lowercase: getpremiumBool()
        getter = u.getPropertyGet(uto, "premiumBool", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "ispremiumBool", getter.getMethodName());

        // test uppercase: ()
        getter = u.getPropertyGet(uto, "PremiumBool", null);
        assertNotNull(getter);
        assertEquals("Found wrong method", "ispremiumBool", getter.getMethodName());
    }


    public void testRegularSetters()
            throws Exception
    {
        VelPropertySet setter;

        Uberspect u = ri.getUberspect();
        UberspectorTestObject uto = new UberspectorTestObject();

        // setRegular()
        setter = u.getPropertySet(uto, "Regular", "", null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setRegular", setter.getMethodName());

        // Lowercase regular
        setter = u.getPropertySet(uto, "regular", "", null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setRegular", setter.getMethodName());

        // lowercase: setpremium()
        setter = u.getPropertySet(uto, "premium", "", null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setpremium", setter.getMethodName());

        // test uppercase: getpremium()
        setter = u.getPropertySet(uto, "Premium", "", null);
        assertNotNull(setter);
        assertEquals("Found wrong method", "setpremium", setter.getMethodName());
    }

    public void testDisambiguation()
            throws Exception
    {
        VelPropertySet setter;

        Uberspect u = ri.getUberspect();
        UberspectorTestObject uto = new UberspectorTestObject();

        // setUnambigous() - String
        setter = u.getPropertySet(uto, "unambiguous", "string", null);
        assertNotNull(setter);
        setter.invoke(uto, "string");

        // setUnambigous() - HashMap
        setter = u.getPropertySet(uto, "unambiguous", new HashMap(), null);
        assertNotNull(setter);
        setter.invoke(uto, new HashMap());
    }

    /*
     *
     *    public void testMapGetSet()
     *        throws Exception
     *    {
     *        Uberspect u = ri.getUberspect();
     *        Map map = new HashMap();
     *
     *        VelPropertyGet getter = u.getPropertyGet(map, "", null);
     *        VelPropertySet setter = u.getPropertySet(map, "", Object.class, null);
     *
     *        assertNotNull("Got a null getter", getter);
     *        assertNotNull("Got a null setter", setter);
     *
     *        assertEquals("Got wrong getter", "foo", getter.getMethodName());
     *        assertEquals("Got wrong setter", "bar", setter.getMethodName());
     *    }
     */
}




