package org.apache.velocity.test;

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

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Method;

import org.apache.velocity.runtime.RuntimeSingleton;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *  Simple introspector test case for primitive problem found in 1.3
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id: IntrospectorTestCase3.java,v 1.2.4.1 2004/03/03 23:23:04 geirm Exp $
 */
public class IntrospectorTestCase3 extends BaseTestCase
{
    /**
      * Creates a new instance.
      */
    public IntrospectorTestCase3(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(IntrospectorTestCase3.class);
    }

    public void testSimple()
        throws Exception
    {
        Method method;
        String result;
        String type;

        MethodProvider mp = new MethodProvider();

        /*
         * string integer
         */

        Object[] listIntInt = { new ArrayList(), new Integer(1), new Integer(2) };
        Object[] listLongList = { new ArrayList(), new Long(1), new ArrayList() };
        Object[] listLongInt = { new ArrayList(), new Long(1), new Integer(2) };
        Object[] intInt = {  new Integer(1), new Integer(2) };
        Object[] longInt = {  new Long(1), new Integer(2) };
        Object[] longLong = {  new Long(1), new Long(2) };

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "lii", listIntInt);
        result = (String) method.invoke(mp, listIntInt);

        assertTrue(result.equals("lii"));

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "ii", intInt);
        result = (String) method.invoke(mp, intInt);

        assertTrue(result.equals("ii"));

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "ll", longInt);
        result = (String) method.invoke(mp, longInt);

        assertTrue(result.equals("ll"));

        /*
         * test overloading with primitives
         */

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "ll", longLong);
        result = (String) method.invoke(mp, longLong);

        assertTrue(result.equals("ll"));

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "lll", listLongList);
        result = (String) method.invoke(mp, listLongList);

        assertTrue(result.equals("lll"));

        /*
         *  test invocation with nulls
         */

        Object [] oa = {null, new Integer(0)};
        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "lll", oa );
        result = (String) method.invoke(mp, oa);

        assertTrue(result.equals("Listl"));

    }

    public static class MethodProvider
    {
        public String ii(int p, int d)
        {
            return "ii";
        }

        public String lii(List s, int p, int d)
        {
            return "lii";
        }

        public String lll(List s, long p, List d)
        {
            return "lll";
        }


        public String lll(List s, long p, int d)
        {
            return "lli";
        }

        public String lll(List s, long p)
        {
            return "Listl";
        }

        public String ll(long p, long d)
        {
            return "ll";
        }

    }
}
