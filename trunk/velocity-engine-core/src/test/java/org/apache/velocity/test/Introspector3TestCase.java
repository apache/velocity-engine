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
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.introspection.Introspector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *  Simple introspector test case for primitive problem found in 1.3
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class Introspector3TestCase extends BaseTestCase
{
    /**
      * Creates a new instance.
      */
    public Introspector3TestCase(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(Introspector3TestCase.class);
    }

    public void testSimple()
        throws Exception
    {
        Method method;
        String result;

        MethodProvider mp = new MethodProvider();

        /*
         * string integer
         */

        Object[] listIntInt = { new ArrayList(), new Integer(1), new Integer(2) };
        Object[] listLongList = { new ArrayList(), new Long(1), new ArrayList() };
        Object[] intInt = {  new Integer(1), new Integer(2) };
        Object[] longInt = {  new Long(1), new Integer(2) };
        Object[] longLong = {  new Long(1), new Long(2) };

        Introspector introspector = new Introspector(log);
        method = introspector.getMethod(
            MethodProvider.class, "lii", listIntInt);
        result = (String) method.invoke(mp, listIntInt);

        assertTrue(result.equals("lii"));

        method = introspector.getMethod(
            MethodProvider.class, "ii", intInt);
        result = (String) method.invoke(mp, intInt);

        assertTrue(result.equals("ii"));

        method = introspector.getMethod(
            MethodProvider.class, "ll", longInt);
        result = (String) method.invoke(mp, longInt);

        assertTrue(result.equals("ll"));

        /*
         * test overloading with primitives
         */

        method = introspector.getMethod(
            MethodProvider.class, "ll", longLong);
        result = (String) method.invoke(mp, longLong);

        assertTrue(result.equals("ll"));

        method = introspector.getMethod(
            MethodProvider.class, "lll", listLongList);
        result = (String) method.invoke(mp, listLongList);

        assertTrue(result.equals("lll"));

        /*
         *  test invocation with nulls
         */

        Object [] oa = {null, new Integer(0)};
        method = introspector.getMethod(
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
