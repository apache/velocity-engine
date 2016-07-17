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
import org.apache.velocity.test.provider.TestProvider;

import java.io.StringWriter;

/**
 * Test thrown exceptions include a proper cause (under JDK 1.4+).
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class WrappedExceptionTestCase extends BaseTestCase implements TemplateTestBase
{
    VelocityEngine ve;

    /**
     * Default constructor.
     */
    public WrappedExceptionTestCase(String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(WrappedExceptionTestCase.class);
    }

    public void setUp() throws Exception
    {
        ve = new VelocityEngine();
        ve.init();
    }


    public void testMethodException() throws Exception
    {

        // accumulate a list of invalid references
        Context context = new VelocityContext();
        StringWriter writer = new StringWriter();
        context.put("test",new TestProvider());

        try
        {
            ve.evaluate(context,writer,"test","$test.getThrow()");
            fail ("expected an exception");
        }
        catch (MethodInvocationException E)
        {
            assertEquals(Exception.class,E.getCause().getClass());
            assertEquals("From getThrow()",E.getCause().getMessage());
        }

    }

}
