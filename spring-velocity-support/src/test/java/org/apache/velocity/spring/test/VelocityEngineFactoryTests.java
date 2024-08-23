/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.velocity.spring.test;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.spring.VelocityEngineFactory;
import org.apache.velocity.spring.VelocityEngineUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Juergen Hoeller
 * @author Issam El-atif
 * @author Sam Brannen
 */
public class VelocityEngineFactoryTests {

    private static final String resourcesPath = System.getProperty("test.resources.dir");

    @Test
    public void testCreateEngineDefaultFileLoader() throws Exception {
    	VelocityEngineFactory factory = new VelocityEngineFactory();
    	factory.setResourceLoaderPath("."); // defaults to target/test-classes file resource loading
    	VelocityEngine engine = factory.createVelocityEngine();
    	Map<String, Object> model = new HashMap<String, Object>();
    	model.put("foo", "bar");
    	String merged = VelocityEngineUtils.mergeTemplateIntoString(engine, "simple.vm", "utf-8", model).trim();
    	assertEquals("file loader failed", "foo=bar", merged);
    }

    @Test
    public void testCreateEngineDefaultClasspathLoader() throws Exception {
    	VelocityEngineFactory factory = new VelocityEngineFactory();
    	factory.setResourceLoaderPath("/"); // defaults to classpath resource loading
    	VelocityEngine engine = factory.createVelocityEngine();
    	Map<String, Object> model = new HashMap<String, Object>();
    	model.put("foo", "bar");
    	String merged = VelocityEngineUtils.mergeTemplateIntoString(engine, "simple.vm", "utf-8", model).trim();
    	assertEquals("classpath loader failed", "foo=bar", merged);
    }

    @Test
    public void testCreateEngineCustomConfig() throws Exception {
    	VelocityEngineFactory factory = new VelocityEngineFactory();
    	factory.setResourceLoaderPath(".");
    	factory.setConfigLocation(new ClassPathResource("/velocity.properties"));
    	VelocityEngine engine = factory.createVelocityEngine();
    	assertEquals("custom config failed", "bar", engine.getProperty("foo"));
    }

}
