package org.apache.velocity.script.test.tools;
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

import org.apache.velocity.script.VelocityScriptEngine;
import org.apache.velocity.script.test.AbstractScriptTest;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

public class EventToolTest extends AbstractScriptTest {

    @Override
    public void setUp() {
        super.setUp();
        super.setupWithDefaultFactory();
    }

    public void testHelloWorldTool() throws ScriptException {
        ScriptContext context = engine.getContext();
        Properties properties = new Properties();
        properties.put("resource.loader", "class");
        properties.put("class.resource.loader.description", "Template Class Loader");
        properties.put("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        CustomEvent event = new CustomEvent("MyEvent");
        context.getBindings(ScriptContext.ENGINE_SCOPE).put("event", event);
        context.setAttribute(VelocityScriptEngine.VELOCITY_PROPERTIES_KEY, properties, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(VelocityScriptEngine.FILENAME, "eventtool.vm", ScriptContext.ENGINE_SCOPE);
        Writer writer = new StringWriter();
        context.setWriter(writer);
        engine.eval("$event;\n" +
                "Event Created by $event.getName()\n" +
                "Event Created on $event.getDate()\n" +
                "Event ID is $event.getID()");
        // check string start
        String check = "This is a test event template: created by MyEvent on ";
        assertEquals(writer.toString().substring(0, check.length()), check);
    }


}
