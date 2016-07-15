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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import java.util.List;


public class VelocityScriptContextTest extends AbstractScriptTest {

    @Override
    public void setUp() {
        super.setUp();
        super.setupWithDefaultFactory();
    }

    public void testInitialScopes() {
        List<Integer> defaultScopes = engine.getContext().getScopes();
        assertEquals(defaultScopes.size(), 2);
        assertTrue(defaultScopes.contains(ScriptContext.ENGINE_SCOPE));
        assertTrue(defaultScopes.contains(ScriptContext.GLOBAL_SCOPE));
    }

    public void testScopes() {

        Bindings velocityBindings = new SimpleBindings();
        engine.getContext().setBindings(velocityBindings, ScriptContext.ENGINE_SCOPE);
        assertNotNull(engine.getBindings(ScriptContext.ENGINE_SCOPE));
        assertNotNull("Engines Registered through manager sets the global scope", engine.getBindings(ScriptContext.GLOBAL_SCOPE));
    }

    public void testEngineScopeAttributes() {

        ScriptContext context = engine.getContext();
        context.setAttribute("engine-prop1", "engine-value1", ScriptContext.ENGINE_SCOPE);

        assertEquals(context.getAttribute("engine-prop1", ScriptContext.ENGINE_SCOPE), "engine-value1");
        assertNull(context.getAttribute("engine-prop1", ScriptContext.GLOBAL_SCOPE));

        context.removeAttribute("engine-prop1", ScriptContext.ENGINE_SCOPE);
        assertNull(context.getAttribute("engine-prop1", ScriptContext.ENGINE_SCOPE));


    }

    public void testGlobalScopeAttributes() {

        ScriptContext context = engine.getContext();
        context.setAttribute("global-prop1", "global-value1", ScriptContext.GLOBAL_SCOPE);

        assertEquals(context.getAttribute("global-prop1", ScriptContext.GLOBAL_SCOPE), "global-value1");
        assertNull(context.getAttribute("global-prop1", ScriptContext.ENGINE_SCOPE));

        context.removeAttribute("global-prop1", ScriptContext.GLOBAL_SCOPE);
        assertNull(context.getAttribute("global-prop1", ScriptContext.GLOBAL_SCOPE));

    }

    public void testJSRExceptions() {

        ScriptContext context = engine.getContext();
        context.setAttribute("global-prop", "global-value", ScriptContext.GLOBAL_SCOPE);
        int invalidScope = 99;

        try {
            context.setBindings(null, ScriptContext.ENGINE_SCOPE);
            fail("Cannot pass null binding for engine scope");
        } catch (NullPointerException n) {
            //Success
        }

        try {
            context.setBindings(new SimpleBindings(), invalidScope);
            fail("Cannot pass invalid scope");
        } catch (IllegalArgumentException n) {
            //Success
        }
        try {
            context.getBindings(invalidScope);
            fail("Cannot pass invalid scope");
        } catch (IllegalArgumentException n) {
            //Success
        }


        try {
            context.setAttribute(null, "value", ScriptContext.ENGINE_SCOPE);

            fail("Name cannot be null");
        } catch (NullPointerException n) {
            //Success
        }
        try {
            context.setAttribute("name1", "value", invalidScope);

            fail("Cannot pass invalid scope");
        } catch (IllegalArgumentException n) {
            //Success
        }



        try {
            context.getAttribute(null, ScriptContext.ENGINE_SCOPE);

            fail("Name cannot be null");
        } catch (NullPointerException n) {
            //Success
        }
        try {
            context.getAttribute("name1", invalidScope);

            fail("Cannot pass invalid scope");
        } catch (IllegalArgumentException n) {
            //Success
        }


         try {
            context.removeAttribute(null, ScriptContext.ENGINE_SCOPE);

            fail("Name cannot be null");
        } catch (NullPointerException n) {
            //Success
        }
        try {
            context.removeAttribute("name1", invalidScope);

            fail("Cannot pass invalid scope");
        } catch (IllegalArgumentException n) {
            //Success
        }


        context.setAttribute("prop2","engine-value2",ScriptContext.ENGINE_SCOPE);
        context.setAttribute("prop2","global-value2",ScriptContext.GLOBAL_SCOPE);
        assertEquals("Should return lowest scope value binded value",context.getAttribute("prop2"),"engine-value2");

    }
}
