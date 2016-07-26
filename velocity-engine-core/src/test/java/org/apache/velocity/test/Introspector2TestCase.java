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
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.introspection.Introspector;

import java.lang.reflect.Method;

/**
 * Test case for the Velocity Introspector which
 *  tests the ability to find a 'best match'
 *
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class Introspector2TestCase extends BaseTestCase
{

    /**
      * Creates a new instance.
      */
    public Introspector2TestCase(String name)
    {
        super(name);
    }

    /**
      * Get the containing <code>TestSuite</code>.
      *
      * @return The <code>TestSuite</code> to run.
      */
    public static Test suite ()
    {
        return new TestSuite(Introspector2TestCase.class);
    }

    public void testIntrospector()
            throws Exception
    {
        Velocity.setProperty(
					Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.init();

        Method method;
        String result;
        Tester t = new Tester();

        Object[] params = { new Foo(), new Foo() };

        Introspector introspector = new Introspector(log);

        method = introspector
            .getMethod( Tester.class, "find", params );

        if ( method == null)
            fail("Returned method was null");

        result = (String) method.invoke( t, params);

        if ( !result.equals( "Bar-Bar" ) )
        {
            fail("Should have gotten 'Bar-Bar' : received '" + result + "'");
        }

        /*
         *  now test for failure due to ambiguity
         */

        method = introspector
            .getMethod( Tester2.class, "find", params );

        if ( method != null)
            fail("Introspector shouldn't have found a method as it's ambiguous.");
    }

    public interface Woogie
    {
    }

    public static class Bar implements Woogie
    {
        int i;
    }

    public static class Foo extends Bar
    {
        int j;
    }

    public static class Tester
    {
        public static String find(Woogie w, Object o )
        {
            return "Woogie-Object";
        }

        public static String find(Object w, Bar o )
        {
            return "Object-Bar";
        }

        public static String find(Bar w, Bar o )
        {
            return "Bar-Bar";
        }

        public static String find( Object o )
        {
            return "Object";
        }

        public static String find( Woogie o )
        {
            return "Woogie";
        }
    }

    public static class Tester2
    {
        public static String find(Woogie w, Object o )
        {
            return "Woogie-Object";
        }

        public static String find(Object w, Bar o )
        {
            return "Object-Bar";
        }

        public static String find(Bar w, Object o )
        {
            return "Bar-Object";
        }

        public static String find( Object o )
        {
            return "Object";
        }

        public static String find( Woogie o )
        {
            return "Woogie";
        }
    }
}
