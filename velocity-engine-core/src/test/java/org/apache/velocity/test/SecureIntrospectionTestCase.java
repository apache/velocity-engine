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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.introspection.SecureUberspector;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;

/**
 * Checks that the secure introspector is working properly.
 *
 * @author <a href="Will Glass-Husain">wglass@forio.com</a>
 * @version $Id$
 */
public class SecureIntrospectionTestCase extends BaseTestCase
{

    /**
     * Default constructor.
     * @param name
     */
    public SecureIntrospectionTestCase(String name)
    {
        super(name);
    }

    public static Test suite()
    {
       return new TestSuite(SecureIntrospectionTestCase.class);
    }


    private String [] badTemplateStrings =
    {
        "$test.Class.Methods",
        "$test.Class.ClassLoader",
        "$test.Class.ClassLoader.loadClass('java.util.HashMap').newInstance().size()"
    };

    private String [] goodTemplateStrings =
    {
        "#foreach($item in $test.collection)$item#end",
        "$test.Class.Name",
        "#set($test.Property = 'abc')$test.Property",
        "$test.aTestMethod()"
    };

    /**
     *  Test to see that "dangerous" methods are forbidden
     *  @exception Exception
     */
    public void testBadMethodCalls()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        ve.init();

        /*
         * all of the following method calls should not work
         */
        doTestMethods(ve, badTemplateStrings, false);
    }

    /**
     *  Test to see that "dangerous" methods are forbidden
     *  @exception Exception
     */
    public void testGoodMethodCalls()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        ve.init();

        /*
         * all of the following method calls should not work
         */
        doTestMethods(ve, goodTemplateStrings, true);
    }

    private void doTestMethods(VelocityEngine ve, String[] templateStrings, boolean shouldeval)
    {
        Context c = new VelocityContext();
        c.put("test", this);

        try
        {
            for (String templateString : templateStrings)
            {
                if (shouldeval && !doesStringEvaluate(ve, c, templateString))
                {
                    fail("Should have evaluated: " + templateString);
                }

                if (!shouldeval && doesStringEvaluate(ve, c, templateString))
                {
                    fail("Should not have evaluated: " + templateString);
                }
            }

        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    private boolean doesStringEvaluate(VelocityEngine ve, Context c, String inputString) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException
    {
        // assume that an evaluation is bad if the input and result are the same (e.g. a bad reference)
        // or the result is an empty string (e.g. bad #foreach)
        Writer w = new StringWriter();
        ve.evaluate(c, w, "foo", inputString);
        String result = w.toString();
        return (result.length() > 0 ) &&  !result.equals(inputString);
    }

    private String testProperty;
    public String getProperty()
    {
        return testProperty;
    }

    public int aTestMethod()
    {
        return 1;
    }

    public void setProperty(String val)
    {
        testProperty = val;
    }


    public Collection getCollection()
    {
        Collection c = new HashSet();
        c.add("aaa");
        c.add("bbb");
        c.add("ccc");
        return c;
    }

    public static class Parent
    {
        public String forbidden()
        {
            return "PARENT_FORBIDDEN";
        }

        public String allowed()
        {
            return "PARENT_ALLOWED";
        }
    }

    public static class Child extends Parent
    {
        @Override
        public String forbidden()
        {
            return "CHILD_FORBIDDEN";
        }
    }

    /**
     * Method restriction blocks a direct call on the configured class.
     */
    public void testRestrictedMethodDirectMatch() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        ve.setProperty(RuntimeConstants.INTROSPECTOR_RESTRICT_METHODS,
                       Parent.class.getName() + ".forbidden");
        ve.init();

        Context c = new VelocityContext();
        c.put("p", new Parent());
        assertFalse("forbidden() on Parent should be blocked",
                    doesStringEvaluate(ve, c, "$p.forbidden()"));
        assertTrue("allowed() on Parent should pass",
                   doesStringEvaluate(ve, c, "$p.allowed()"));
    }

    /**
     * A restriction on a parent class also blocks the method on subclasses.
     */
    public void testRestrictedMethodSubclassMatch() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        ve.setProperty(RuntimeConstants.INTROSPECTOR_RESTRICT_METHODS,
                       Parent.class.getName() + ".forbidden");
        ve.init();

        Context c = new VelocityContext();
        c.put("ch", new Child());
        assertFalse("forbidden() on Child must be blocked via parent restriction",
                    doesStringEvaluate(ve, c, "$ch.forbidden()"));
        assertTrue("allowed() on Child should still pass",
                   doesStringEvaluate(ve, c, "$ch.allowed()"));
    }

    /**
     * An unknown class in the configuration should not crash init; the entry is ignored.
     */
    public void testRestrictedMethodUnknownClass() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        ve.setProperty(RuntimeConstants.INTROSPECTOR_RESTRICT_METHODS,
                       "com.example.does.not.Exist.someMethod");
        ve.init();

        Context c = new VelocityContext();
        c.put("p", new Parent());
        assertTrue("unknown-class entry must not block legitimate calls",
                   doesStringEvaluate(ve, c, "$p.forbidden()"));
    }

    /**
     * The default-shipped restriction on VelocityEngine.init must block that call from a template.
     */
    public void testDefaultVelocityEngineRestriction() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        ve.init();

        Context c = new VelocityContext();
        c.put("ve", ve);
        assertFalse("VelocityEngine.init must be blocked by default",
                    doesStringEvaluate(ve, c, "$ve.init()"));
        assertFalse("VelocityEngine.reset must be blocked by default",
                    doesStringEvaluate(ve, c, "$ve.reset()"));
    }
}


