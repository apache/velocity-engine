package org.apache.velocity.test.util.introspection;

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
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.introspection.ClassMap;
import org.slf4j.Logger;

/**
 * Test the ClassMap Lookup
 */
public class ClassMapTestCase
        extends BaseTestCase
{
    public ClassMapTestCase(final String name)
    	throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ClassMapTestCase.class);
    }

    public void setUp()
            throws Exception
    {
        Velocity.setProperty(Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());
	Velocity.init();
    }
    
    public void tearDown()
    {
    }

    public void testPrimitives()
    	throws Exception
    {
        Logger log = Velocity.getLog();
	
        ClassMap c = new ClassMap(TestClassMap.class, log);
        assertNotNull(c.findMethod("setBoolean",   new Object[] { Boolean.TRUE }));
        assertNotNull(c.findMethod("setByte",      new Object[] { new Byte((byte) 4)}));
        assertNotNull(c.findMethod("setCharacter", new Object[] { new Character('c')}));
        assertNotNull(c.findMethod("setDouble",    new Object[] { new Double(8.0) }));
        assertNotNull(c.findMethod("setFloat",     new Object[] { new Float(15.0) }));
        assertNotNull(c.findMethod("setInteger",   new Object[] { new Integer(16) }));
        assertNotNull(c.findMethod("setLong",      new Object[] { new Long(23) }));
        assertNotNull(c.findMethod("setShort",     new Object[] { new Short((short)42)}));
    }

    public static final class TestClassMap
    {
        public void setBoolean(boolean b)
        {
        }

        public void setByte(byte b)
        {
        }

        public void setCharacter(char c)
        {
        }

        public void setDouble(double d)
        {
        }

        public void setFloat(float f)
        {
        }

        public void setInteger(int i)
        {
        }

        public void setLong(long l)
        {
        }

        public void setShort(short s)
        {
        }
    }
}
