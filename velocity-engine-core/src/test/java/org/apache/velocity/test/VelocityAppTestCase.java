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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;

/**
 * This class is intended to test the app.Velocity.java class.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id$
 */
public class VelocityAppTestCase extends BaseTestCase implements TemplateTestBase
{
    private StringWriter compare1 = new StringWriter();
    private String input1 = "My name is $name -> $Floog";
    private String result1 = "My name is jason -> floogie woogie";

    public VelocityAppTestCase(String name)
    {
        super(name);
    }

    public void testVelocityApp()
            throws Exception
    {
        engine.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        engine.init();

        // the usage of engine here is equivalent to using static calls to Velocity. class

        VelocityContext context = new VelocityContext();
        context.put("name", "jason");
        context.put("Floog", "floogie woogie");

        String cmp = "Hello jason! Nice floogie woogie!";

        engine.evaluate(context, compare1, "evaltest", input1);
        if (!result1.equals(compare1.toString()))
        {
            fail("Output 1 incorrect.");
        }

        StringWriter result2 = new StringWriter();
        engine.mergeTemplate("mergethis.vm", "UTF-8", context, result2);
        if (!result2.toString().equals(cmp))
        {
            fail("Output 2 incorrect.");
        }

        StringWriter result3 = new StringWriter();
        engine.invokeVelocimacro("floog", "test", new String[]{"name", "Floog"}, context, result3);

        if (!result3.toString().equals(cmp))
        {
            fail("Output 3 incorrect.");
        }
    }
}
