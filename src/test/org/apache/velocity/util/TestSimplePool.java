/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.velocity.util;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *  Simpletest for the SimplePool
 *
 * @version $Id: TestSimplePool.java,v 1.2 2004/02/27 18:43:21 dlr Exp $ 
 */
public class TestSimplePool extends TestCase
{
    public static Test suite()
    {
        return new TestSuite(TestSimplePool.class);
    }

    public TestSimplePool(String testName)
    {
        super(testName);
    }

    public void testPool()
        throws Exception
    {
        SimplePool sp = new SimplePool(10);

        for (int i=0; i<10; i++)
        {
            sp.put(new Integer(i));
        }

        for (int i=9; i>=0; i--)
        {
            Integer obj = (Integer) sp.get();

            assertTrue(i == obj.intValue());
        }

        Object[] pool = sp.getPool();

        for (int i=0; i<10; i++)
        {
            assertTrue("Pool not empty", pool[i] == null);
        }
    }
}
