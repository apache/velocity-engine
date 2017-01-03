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

import junit.framework.TestCase;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;
import org.apache.velocity.test.misc.TestLogger;

import java.io.StringWriter;

/**
 * Tests ability to have multiple repositories in the same app.
 *
 * @author Nathan Bubna
 * @version $Id: StringResourceLoaderRepositoryTestCase.java 479058 2006-11-25 00:26:32Z henning $
 */
public class StringResourceLoaderRepositoryTestCase extends TestCase
{
    private VelocityContext context;

    public StringResourceLoaderRepositoryTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        Velocity.reset();
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "string");
        Velocity.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        Velocity.addProperty("string.resource.loader.modificationCheckInterval", "1");
        Velocity.setProperty(Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());
        Velocity.init();

        StringResourceRepository repo = getRepo(null, null);
        repo.putStringResource("foo", "This is $foo");
        repo.putStringResource("bar", "This is $bar");

        context = new VelocityContext();
        context.put("foo", "wonderful!");
        context.put("bar", "horrible!");
        context.put("woogie", "a woogie");
    }


    protected VelocityEngine newStringEngine(String repoName, boolean isStatic)
    {
        VelocityEngine engine = new VelocityEngine();
        TestLogger logger = new TestLogger();
        engine.setProperty(Velocity.RESOURCE_LOADER, "string");
        engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        if (repoName != null)
        {
            engine.addProperty("string.resource.loader.repository.name", repoName);
        }
        if (!isStatic)
        {
            engine.addProperty("string.resource.loader.repository.static", "false");
        }
        engine.addProperty("string.resource.loader.modificationCheckInterval", "1");
        engine.setProperty(Velocity.RUNTIME_LOG_INSTANCE, logger);
        return engine;
    }

    protected StringResourceRepository getRepo(String name, VelocityEngine engine)
    {
        if (engine == null)
        {
            if (name == null)
            {
                return StringResourceLoader.getRepository();
            }
            else
            {
                return StringResourceLoader.getRepository(name);
            }
        }
        else
        {
            if (name == null)
            {
                return (StringResourceRepository)engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
            }
            else
            {
                return (StringResourceRepository)engine.getApplicationAttribute(name);
            }
        }
    }

    protected String render(Template template) throws Exception
    {
        StringWriter out = new StringWriter();
        template.merge(this.context, out);
        return out.toString();
    }


    public void testSharedRepo() throws Exception
    {
        // this engine's string resource loader should share a repository
        // with the singleton's string resource loader
        VelocityEngine engine = newStringEngine(null, true);

        // get and merge the same template from both runtimes with the same context
        String engineOut = render(engine.getTemplate("foo"));
        String singletonOut = render(RuntimeSingleton.getTemplate("foo"));

        // make sure they're equal
        assertEquals(engineOut, singletonOut);
    }

    public void testAlternateStaticRepo() throws Exception
    {
        VelocityEngine engine = newStringEngine("alternate.repo", true);
        // should be null be for init
        StringResourceRepository repo = getRepo("alternate.repo", null);
        assertNull(repo);
        engine.init();
        // and not null after init
        repo = getRepo("alternate.repo", null);
        assertNotNull(repo);
        repo.putStringResource("foo", "This is NOT $foo");

        // get and merge template with the same name from both runtimes with the same context
        String engineOut = render(engine.getTemplate("foo"));
        String singletonOut = render(RuntimeSingleton.getTemplate("foo"));

        // make sure they're NOT equal
        assertFalse(engineOut.equals(singletonOut));
    }

    public void testPreCreatedStaticRepo() throws Exception
    {
        VelocityEngine engine = newStringEngine("my.repo", true);
        MyRepo repo = new MyRepo();
        repo.put("bar", "This is NOT $bar");
        StringResourceLoader.setRepository("my.repo", repo);

        String out = render(engine.getTemplate("bar"));
        assertEquals(out, "This is NOT horrible!");
    }

    public void testAppRepo() throws Exception
    {
        VelocityEngine engine = newStringEngine(null, false);
        engine.init();

        StringResourceRepository repo = getRepo(null, engine);
        assertNotNull(repo);
        repo.putStringResource("woogie", "What is $woogie?");

        String out = render(engine.getTemplate("woogie"));
        assertEquals(out, "What is a woogie?");
    }

    public void testAlternateAppRepo() throws Exception
    {
        VelocityEngine engine = newStringEngine("alternate.app.repo", false);
        engine.init();

        StringResourceRepository repo = getRepo("alternate.app.repo", engine);
        assertNotNull(repo);
        repo.putStringResource("you/foo.vm", "You look $foo");

        String out = render(engine.getTemplate("you/foo.vm"));
        assertEquals(out, "You look wonderful!");
    }

    public void testPreCreatedAppRepo() throws Exception
    {
        VelocityEngine engine = newStringEngine("my.app.repo", false);
        MyRepo repo = new MyRepo();
        repo.put("you/bar.vm", "You look $bar");
        engine.setApplicationAttribute("my.app.repo", repo);

        String out = render(engine.getTemplate("you/bar.vm"));
        assertEquals(out, "You look horrible!");
    }

    public static class MyRepo extends StringResourceRepositoryImpl
    {
        public void put(String name, String template)
        {
            putStringResource(name, template);
        }
    }

}
