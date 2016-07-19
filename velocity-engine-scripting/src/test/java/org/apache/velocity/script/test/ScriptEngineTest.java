package org.apache.velocity.script.test;
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

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.io.Writer;

public class ScriptEngineTest extends AbstractScriptTest {

    @Override
    public void setUp() {
        super.setUp();
        super.setupWithDefaultFactory();
    }

    public void testBasicOpe() {

        engine.put("name1", "value1");
        Bindings engineScope = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        assertEquals("Engine#put should have same effect as context.put ", engineScope.get("name1"), "value1");

        String val = engine.get("name1").toString();
        assertEquals("Engine#get should have same effect as context.get ", engineScope.get("name1"), val);
    }


    public void testJSR223tException(){
       try {
            engine.get("");
            fail("Cannot pass empty name");
        } catch (IllegalArgumentException n) {
            //Success
        }

        try {
             engine.setContext(null);
             fail("Cannot pass null to context");
         } catch (NullPointerException n) {
             //Success
         }

    }

    public void testEngineEvals() throws ScriptException {
        String path = System.getProperty("test.resources.dir");
        engine.getContext().setWriter(new StringWriter());
        engine.getContext().setAttribute(VelocityScriptEngine.VELOCITY_PROPERTIES_KEY, path + "/test-classes/velocity.properties", ScriptContext.ENGINE_SCOPE);
        String script = "<html><body>#set( $foo = 'Velocity' )Hello $foo World!</body><html>";
        Object result = engine.eval(script);
        assertEquals(result.toString(), "<html><body>Hello Velocity World!</body><html>");
    }

    public void testCompilable() throws ScriptException
    {
        String path = System.getProperty("test.resources.dir");
        engine.getContext().setWriter(new StringWriter());
        engine.getContext().setAttribute(VelocityScriptEngine.VELOCITY_PROPERTIES_KEY, path + "/test-classes/velocity.properties", ScriptContext.ENGINE_SCOPE);
        String script = "$foo";
        engine.put("foo", "bar");
        CompiledScript compiled = ((Compilable)engine).compile(script);
        Object result = compiled.eval();
        assertEquals(result.toString(), "bar");
    }

    public void testContext() throws ScriptException
    {
        String path = System.getProperty("test.resources.dir");
        engine.getContext().setWriter(new StringWriter());
        engine.getContext().setAttribute(VelocityScriptEngine.VELOCITY_PROPERTIES_KEY, path + "/test-classes/velocity.properties", ScriptContext.ENGINE_SCOPE);
        String script = "$context.class.name $context.writer.class.name $context.reader.class.name $context.errorWriter.class.name";
        String result = engine.eval(script).toString();
        assertEquals("javax.script.SimpleScriptContext java.io.StringWriter java.io.InputStreamReader java.io.PrintWriter", result);
    }
    
}
