package org.apache.velocity.util.introspection;

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

import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeSingleton;

public class JDK15UberspectTestCase
        extends TestCase
{
    public JDK15UberspectTestCase(final String name)
    {
        super(name);
    }

    public void setUp()
    	throws Exception
    {
    	Properties p = new Properties();
    	p.put(RuntimeConstants.UBERSPECT_CLASSNAME, JDK15UberspectImpl.class.getName());
    	RuntimeSingleton.init(p);
    }

    public static Test suite()
    {
        return new TestSuite(JDK15UberspectTestCase.class);
    }

    public void testIterable()
    	throws Exception
    {
        TestIterator itobj = new TestIterator();
        TestIterable io = new TestIterable(itobj);

        Uberspect u = RuntimeSingleton.getUberspect();
        assertEquals("Wrong Uberspector configured!", JDK15UberspectImpl.class, u.getClass());
        Iterator it = u.getIterator(io, null);

        assertNotNull("The introspector did not return an iterator!", it);
        assertEquals("The introspector did return a wrong iterator!", itobj, it);
    }

    public static class TestIterator
            implements Iterator
    {

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			return null;
		}

		public void remove() {
		}
    }

    public static class TestIterable
            implements Iterable
    {
        private final Iterator it;

        public TestIterable(final Iterator it)
        {
            this.it = it;
        }

        public Iterator iterator()
        {
            return it;
        }
    }
}
