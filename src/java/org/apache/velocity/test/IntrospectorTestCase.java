package org.apache.velocity.test;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.ArrayList;

import java.lang.reflect.Method;

import org.apache.velocity.runtime.RuntimeSingleton;

import junit.framework.TestCase;

/**
 * Test case for the Velocity Introspector which uses
 * the Java Reflection API to determine the correct
 * signature of the methods used in VTL templates.
 *
 * This should be split into separate tests for each
 * of the methods searched for but this is a start
 * for now.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: IntrospectorTestCase.java,v 1.9 2001/09/09 21:50:02 geirm Exp $
 */
public class IntrospectorTestCase extends BaseTestCase
{
    private Method method;
    private String result;
    private String type;
    private ArrayList failures = new ArrayList();

    IntrospectorTestCase()
    {
        super("IntrospectorTestCase");
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
    public static junit.framework.Test suite ()
    {
        return new IntrospectorTestCase();
    }

    public void runTest()
    {
        MethodProvider mp = new MethodProvider();
    
        try
        {
            // Test boolean primitive.
            Object[] booleanParams = { new Boolean(true) };
            type = "boolean";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", booleanParams);
            result = (String) method.invoke(mp, booleanParams);
            
            if (!result.equals(type))
                failures.add(type + "Method could not be found!");
            
            // Test byte primitive.
            Object[] byteParams = { new Byte("1") };
            type = "byte";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", byteParams);
            result = (String) method.invoke(mp, byteParams);

            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test char primitive.
            Object[] characterParams = { new Character('a') };
            type = "character";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", characterParams);
            result = (String) method.invoke(mp, characterParams);

            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test double primitive.
            Object[] doubleParams = { new Double((double)1) };
            type = "double";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", doubleParams);
            result = (String) method.invoke(mp, doubleParams);

            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test float primitive.
            Object[] floatParams = { new Float((float)1) };
            type = "float";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", floatParams);
            result = (String) method.invoke(mp, floatParams);

            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test integer primitive.
            Object[] integerParams = { new Integer((int)1) };
            type = "integer";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", integerParams);
            result = (String) method.invoke(mp, integerParams);

            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test long primitive.
            Object[] longParams = { new Long((long)1) };
            type = "long";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", longParams);
            result = (String) method.invoke(mp, longParams);

            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test short primitive.
            Object[] shortParams = { new Short((short)1) };
            type = "short";
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, type + "Method", shortParams);
            result = (String) method.invoke(mp, shortParams);
        
            if (!result.equals(type))
                failures.add(type + "Method could not be found!");

            // Test untouchable

            Object[] params = {};
           
            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, "untouchable", params);

            if (method != null)
                failures.add(type + "able to access a private-access method.");      

            // Test really untouchable

            method = RuntimeSingleton.getIntrospector().getMethod(
                MethodProvider.class, "reallyuntouchable", params);

            if (method != null)
                failures.add(type + "able to access a default-access method.");      

            // There were any failures then show all the
            // errors that occured.
            
            int totalFailures = failures.size();
            if (totalFailures > 0)
            {
                StringBuffer sb = new StringBuffer("\nIntrospection Errors:\n");
                for (int i = 0; i < totalFailures; i++)
                    sb.append((String) failures.get(i)).append("\n");
            
                fail(sb.toString());
            }                    
        }
        catch (Exception e)
        {
            fail( e.toString() );
        }
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
        private String reallyuntouchable() { return "yech!"; }

    }
}
