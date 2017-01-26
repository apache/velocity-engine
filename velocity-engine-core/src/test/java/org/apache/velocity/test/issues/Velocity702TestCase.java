package org.apache.velocity.test.issues;

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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-702.
 */
public class Velocity702TestCase extends BaseTestCase
{
    public Velocity702TestCase(String name)
    {
        super(name);
    }

    public void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "high,low");
        engine.addProperty("high.resource.loader.class", StringResourceLoader.class.getName());
        engine.addProperty("high.resource.loader.cache", "false");
        engine.addProperty("high.resource.loader.repository.name", "high");
        engine.addProperty("high.resource.loader.repository.static", "false");
        engine.addProperty("high.resource.loader.modificationCheckInterval", "1");
        engine.addProperty("low.resource.loader.class", StringResourceLoader.class.getName());
        engine.addProperty("low.resource.loader.cache", "true");
        engine.addProperty("low.resource.loader.repository.name", "low");
        engine.addProperty("low.resource.loader.repository.static", "false");
        engine.addProperty("low.resource.loader.modificationCheckInterval", "1");
        engine.init();
    }

    public void testIt() throws Exception
    {
        addToHigh("foo", "foo");
        addToLow("foo", "bar");
        assertTmplEquals("foo", "foo");

        removeFromHigh("foo");
        assertTmplEquals("bar", "foo");

        Thread.sleep(1500);
        addToHigh("foo", "woogie");
        assertTmplEquals("woogie", "foo");
    }

    private void addToHigh(String name, String content)
    {
        getHighRepo().putStringResource(name, content);
    }

    private void removeFromHigh(String name)
    {
        getHighRepo().removeStringResource(name);
    }

    private StringResourceRepository getHighRepo()
    {
        return (StringResourceRepository)engine.getApplicationAttribute("high");
    }

    private void addToLow(String name, String content)
    {
        getLowRepo().putStringResource(name, content);
    }

    private void removeFromLow(String name)
    {
        getLowRepo().removeStringResource(name);
    }

    private StringResourceRepository getLowRepo()
    {
        return (StringResourceRepository)engine.getApplicationAttribute("low");
    }

}
