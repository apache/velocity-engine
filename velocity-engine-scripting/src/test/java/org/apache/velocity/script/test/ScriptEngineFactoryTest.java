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

import org.apache.velocity.script.VelocityScriptEngineFactory;
import org.junit.Test;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.List;

public class ScriptEngineFactoryTest extends AbstractScriptTest {

    @Override
    public void setUp() {
        super.setUp();
        super.setupEngine(constructFactory());
    }
    @Test
    public void testCreateFactory() {

        ScriptEngineFactory factory = engine.getFactory();

        assertEquals(1, factory.getNames().size());
        assertEquals("HelloWorld", factory.getNames().get(0));

        assertEquals(1, factory.getExtensions().size());
        assertEquals("hello-vm", factory.getExtensions().get(0));


        assertEquals(1, factory.getMimeTypes().size());
        assertEquals("mimetype1", factory.getMimeTypes().get(0));

        assertEquals("test-engine",factory.getEngineName());
        assertEquals("1.0.0",factory.getEngineVersion());
        assertEquals("test-language",factory.getLanguageName());
        assertEquals("2.0.0",factory.getLanguageVersion());

    }

    @Test
    public void testParameters(){

        ScriptEngineFactory factory = engine.getFactory();

        assertEquals("test-engine",factory.getParameter(ScriptEngine.ENGINE));

        assertEquals("1.0.0",factory.getParameter(ScriptEngine.ENGINE_VERSION));

        assertEquals("test-language",factory.getParameter(ScriptEngine.LANGUAGE));

        assertEquals("2.0.0",factory.getParameter(ScriptEngine.LANGUAGE_VERSION));

    }


    private ScriptEngineFactory constructFactory() {
        List<String> names = new ArrayList<String>();
        names.add("HelloWorld");
        List<String> extensions = new ArrayList<String>();
        extensions.add("hello-vm");
        List<String> mimeTypes = new ArrayList<String>();
        mimeTypes.add("mimetype1");

        return new VelocityScriptEngineFactory(names,
                extensions,
                mimeTypes,
                "test-engine",
                "1.0.0",
                "test-language",
                "2.0.0");
    }
}
