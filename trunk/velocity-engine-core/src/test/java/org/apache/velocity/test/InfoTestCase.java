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
import org.apache.velocity.test.misc.UberspectTestException;
import org.apache.velocity.util.introspection.Info;

import java.io.StringWriter;




/**
 * Test that the Info class in the Introspector holds the correct information.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:isidore@setgame.com">Llewellyn Falco</a>
 * @version $Id$
 */
public class InfoTestCase extends BaseTestCase implements TemplateTestBase
{
    VelocityEngine ve;

    /**
     * Default constructor.
     */
    public InfoTestCase(String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(InfoTestCase.class);
    }

    public void setUp() throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(
                "runtime.introspector.uberspect", "org.apache.velocity.test.misc.UberspectTestImpl");

        ve.setProperty(
                Velocity.FILE_RESOURCE_LOADER_PATH, TemplateTestBase.TEST_COMPARE_DIR + "/info");

        ve.init();
    }



    public void testInfoProperty() throws Exception
    {
        // check property
        checkInfo("info1.vm", 1, 7);
    }

    public void testInfoMethod() throws Exception
    {
        // check method
        checkInfo("info2.vm", 1, 7);
    }

    public void checkInfo(String templateName,
            int expectedLine, int expectedCol) throws Exception
    {
        Context context = new VelocityContext();
        Template template = ve.getTemplate(templateName, "UTF-8");
        Info info = null;

        context.put("main", this);

        try (StringWriter writer = new StringWriter())
        {
            template.merge(context, writer);
            writer.flush();
            fail("Uberspect should have thrown an exception");
        }
        catch (UberspectTestException E)
        {
            info = E.getInfo();
        }
        assertInfoEqual(info, templateName, expectedLine, expectedCol);

    }

    private void assertInfoEqual(Info i, String name, int line, int column)
    {
        assertEquals("Template Name", name, i.getTemplateName());
        assertEquals("Template Line", line, i.getLine());
        assertEquals("Template Column", column, i.getColumn());
    }

}
