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
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.ExceptionGeneratingDirective;
import org.apache.velocity.test.misc.ExceptionGeneratingEventHandler;
import org.apache.velocity.test.misc.ExceptionGeneratingResourceLoader;
import org.apache.velocity.test.provider.TestProvider;

import java.io.StringWriter;

/**
 * Test case for miscellaneous Exception related issues.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class ExceptionTestCase extends BaseTestCase implements TemplateTestBase
{
    VelocityEngine ve;

    /**
     * Default constructor.
     */
    public ExceptionTestCase(String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(ExceptionTestCase.class);
    }


    public void testReferenceInsertionEventHandlerException()
    throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION,ExceptionGeneratingEventHandler.class.getName());
        ve.init();
        assertException(ve);
    }

    /**
     * Note - this is the one case where RuntimeExceptions *are not* passed through
     * verbatim.
     * @throws Exception
     */
    public void testMethodExceptionEventHandlerException()
    throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION,ExceptionGeneratingEventHandler.class.getName());
        ve.init();
        Context context = new VelocityContext();
        context.put ("test",new TestProvider());
        assertMethodInvocationException(ve,context,"$test.getThrow()");
        assertMethodInvocationException(ve,context,"$test.throw");
    }

    public void testIncludeEventHandlerException()
    throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE,ExceptionGeneratingEventHandler.class.getName());
        ve.init();
        assertException(ve,"#include('dummy')");
    }

    public void testResourceLoaderException()
    throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER,"except");
        ve.setProperty("except.resource.loader.class",ExceptionGeneratingResourceLoader.class.getName());
        try
        {
            ve.init();  // tries to get the macro file
            ve.getTemplate("test.txt");
            fail("Should have thrown RuntimeException");
        }
        catch (RuntimeException E)
        {
            // do nothing
        }
    }


    public void testDirectiveException()
    throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty("userdirective",ExceptionGeneratingDirective.class.getName());
        ve.init();
        assertException(ve,"#Exception() test #end");
    }



    public void assertException(VelocityEngine ve)
    throws Exception
    {
        Context context = new VelocityContext();
        context.put ("test","test");
        assertException(ve,context,"this is a $test");
    }

    public void assertException(VelocityEngine ve, String input)
    throws Exception
    {
        Context context = new VelocityContext();
        context.put ("test","test");
        assertException(ve,context,input);
    }

    public void assertException(VelocityEngine ve, Context context, String input)
    throws Exception
    {
        try
        {
            StringWriter writer = new StringWriter();
            ve.evaluate(context,writer,"test",input);
            fail("Expected RuntimeException");
        }
        catch (RuntimeException E)
        {
            // do nothing
        }
    }
    public void assertMethodInvocationException(VelocityEngine ve, Context context, String input)
    throws Exception
    {
        try
        {
            StringWriter writer = new StringWriter();
            ve.evaluate(context,writer,"test",input);
            fail("Expected MethodInvocationException");
        }
        catch (MethodInvocationException E)
        {
            // do nothing
        }
    }


}
