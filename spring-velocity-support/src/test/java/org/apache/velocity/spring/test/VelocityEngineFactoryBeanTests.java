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
import org.apache.velocity.spring.SpringResourceLoader;
import org.apache.velocity.spring.VelocityEngineFactoryBean;
import org.apache.velocity.spring.VelocityEngineUtils;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Juergen Hoeller
 * @author Issam El-atif
 * @author Sam Brannen
 */
public class VelocityEngineFactoryBeanTests
{
    private static final String resourcesPath = System.getProperty("test.resources.dir");
    private final VelocityEngineFactoryBean vefb = new VelocityEngineFactoryBean();

    @Test
    public void velocityFactoryBeanWithConfigLocation() throws Exception {
    	vefb.setConfigLocation(new ClassPathResource("velocity.properties"));
    	vefb.afterPropertiesSet();
    	VelocityEngine engine = vefb.getObject();
    	assertEquals("bean config location failed", "bar", engine.getProperty("foo"));
    }

    @Test
    public void velocityFactoryBeanWithResourceLoaderPath() throws Exception {
    	vefb.setResourceLoaderPath("file:" + resourcesPath);
    	vefb.afterPropertiesSet();
    	VelocityEngine engine = vefb.getObject();
    	Map<String, Object> model = new HashMap<String, Object>();
    	model.put("foo", "bar");
    	String merged = VelocityEngineUtils.mergeTemplateIntoString(engine, "simple.vm", "utf-8", model).trim();
    	assertEquals("resource loader failed", "foo=bar", merged);
    }

    @Test  // SPR-12448
    public void velocityConfigurationAsBean() {
    	DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    	RootBeanDefinition loaderDef = new RootBeanDefinition(SpringResourceLoader.class);
    	loaderDef.getConstructorArgumentValues().addGenericArgumentValue(new DefaultResourceLoader());
    	loaderDef.getConstructorArgumentValues().addGenericArgumentValue("/freemarker");
    	// RootBeanDefinition configDef = new RootBeanDefinition(Configuration.class);
    	//configDef.getPropertyValues().add("templateLoader", loaderDef);
    	//beanFactory.registerBeanDefinition("freeMarkerConfig", configDef);
    	// assertThat(beanFactory.getBean(Configuration.class)).isNotNull();
    }

}
