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
        String path = ScriptEngineTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String propertyFile = path.substring(0,path.indexOf("target/test-classes")) +
                "src/test/java/org/apache/velocity/script/test/resources/velocity.properties";
        System.setProperty(VelocityScriptEngine.VELOCITY_PROPERTIES,  propertyFile) ;
        //Comment test case

        Writer writer = new StringWriter();
        engine.getContext().setWriter(writer);
        String script = "<html>\n" +
                "<body>\n" +
                "#set( $foo = \"Velocity\" )\n" +
                "Hello $foo World!\n" +
                "</body>\n" +
                "<html>";
//        String script = "## This is a comment ";
        Object result = engine.eval(script);
        assertTrue(Boolean.valueOf(result.toString()));
        System.out.println(">>>"+writer);
        //TODO add more engine script evaluation test cases

    }




}
