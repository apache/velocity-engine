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
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.introspection.Introspector;

import java.lang.reflect.Method;

/**
 * Test case for the Velocity Introspector which uses
 * the Java Reflection API to determine the correct
 * signature of the methods used in VTL templates.
 *
 * This should be split into separate tests for each
 * of the methods searched for but this is a start
 * for now.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class IntrospectorTestCase extends BaseTestCase
{
    private static MethodProvider mp;

    private Introspector introspector;

    public void setUp()
    {
        mp = new MethodProvider();
        log = new TestLogger();
        introspector = new Introspector(log);
    }

    /**
     * Creates a new instance.
     */
    public IntrospectorTestCase (String name)
    {
        super(name);
    }

    /**
      * Get the containing <code>TestSuite</code>.  This is always
      * <code>VelocityTestSuite</code>.
      *
      * @return The <code>TestSuite</code> to run.
      */
    public static Test suite ()
    {
        return new TestSuite(IntrospectorTestCase.class);
    }

    public void testIntrospectorBoolean()
            throws Exception
    {
        // Test boolean primitive.
        Object[] booleanParams = { new Boolean(true) };
        String type = "boolean";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", booleanParams);
        String result = (String) method.invoke(mp, booleanParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorByte()
            throws Exception
    {
        // Test byte primitive.
        Object[] byteParams = { new Byte("1") };
        String type = "byte";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", byteParams);
        String result = (String) method.invoke(mp, byteParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorChar()
            throws Exception
    {
        // Test char primitive.
        Object[] characterParams = { new Character('a') };
        String type = "character";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", characterParams);
        String result = (String) method.invoke(mp, characterParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorDouble()
            throws Exception
    {

        // Test double primitive.
        Object[] doubleParams = { new Double((double)1) };
        String type = "double";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", doubleParams);
        String result = (String) method.invoke(mp, doubleParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorFloat()
            throws Exception
    {

        // Test float primitive.
        Object[] floatParams = { new Float((float)1) };
        String type = "float";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", floatParams);
        String result = (String) method.invoke(mp, floatParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorInteger()
            throws Exception
    {

        // Test integer primitive.
        Object[] integerParams = { new Integer((int)1) };
        String type = "integer";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", integerParams);
        String result = (String) method.invoke(mp, integerParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorPrimitiveLong()
            throws Exception
    {

        // Test long primitive.
        Object[] longParams = { new Long((long)1) };
        String type = "long";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", longParams);
        String result = (String) method.invoke(mp, longParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorPrimitiveShort()
            throws Exception
    {
        // Test short primitive.
        Object[] shortParams = { new Short((short)1) };
        String type = "short";
        Method method = introspector.getMethod(
                MethodProvider.class, type + "Method", shortParams);
        String result = (String) method.invoke(mp, shortParams);

        assertEquals("Method could not be found", type, result);
    }

    public void testIntrospectorUntouchable()
            throws Exception
    {
        // Test untouchable

        Object[] params = {};

        Method method = introspector.getMethod(
                MethodProvider.class, "untouchable", params);

        assertNull("able to access a private-access method.", method);
    }

    public void testIntrospectorReallyUntouchable()
            throws Exception
    {
        // Test really untouchable
        Object[] params = {};

        Method method = introspector.getMethod(
                MethodProvider.class, "reallyuntouchable", params);

        assertNull("able to access a private-access method.", method);
    }

    public static class MethodProvider
    {
        /*
         * Methods with native parameter types.
         */
        public String booleanMethod (boolean p) { return "boolean"; }
        public String byteMethod (byte p) { return "byte"; }
        public String characterMethod (char p) { return "character"; }
        public String doubleMethod (double p) { return "double"; }
        public String floatMethod (float p) { return "float"; }
        public String integerMethod (int p) { return "integer"; }
        public String longMethod (long p) { return "long"; }
        public String shortMethod (short p) { return "short"; }

        String untouchable() { return "yech";}
        // don't remove! Used through introspection for testing!
        private String reallyuntouchable() { return "yech!"; }

    }
}
