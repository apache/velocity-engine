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
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.StringWriter;




/**
 * Test that #parse and #include pass errors to calling code.
 * Specifically checking against VELOCITY-95 and VELOCITY-96.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class IncludeErrorTestCase extends BaseTestCase implements TemplateTestBase
{
    VelocityEngine ve;

    /**
     * Default constructor.
     */
    public IncludeErrorTestCase(String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(IncludeErrorTestCase.class);
    }

    public void setUp() throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, TemplateTestBase.TEST_COMPARE_DIR + "/includeerror");

        ve.init();
    }



    public void testMissingParseError() throws Exception
    {
        checkException("missingparse.vm",ResourceNotFoundException.class);
    }

    public void testMissingIncludeError() throws Exception
    {
        checkException("missinginclude.vm",ResourceNotFoundException.class);
    }

    public void testParseError() throws Exception
    {
        checkException("parsemain.vm",ParseErrorException.class);
    }

    public void testParseError2() throws Exception
    {
        checkException("parsemain2.vm",ParseErrorException.class);
    }


    /**
     * Check that an exception is thrown for the given template
     * @param templateName
     * @param exceptionClass
     * @throws Exception
     */
    private void checkException(String templateName,Class exceptionClass)
    throws Exception
    {
        Context context = new VelocityContext();
        Template template = ve.getTemplate(templateName, "UTF-8");

        try (StringWriter writer = new StringWriter())
        {
            template.merge(context, writer);
            writer.flush();
            fail("File should have thrown an exception");
        }
        catch (Exception E)
        {
            assertTrue(exceptionClass.isAssignableFrom(E.getClass()));
        }

    }

}
