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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.io.StringWriter;

/**
 * Tests the normalization of resource names, and the refusal of a name which
 * climbs above the resource root.
 */
public class ResourceNameNormalizationTestCase extends BaseTestCase
{
    private static final String ROOT = TEST_COMPARE_DIR + "/normalization/root";

    public ResourceNameNormalizationTestCase(String name)
    {
        super(name);
    }

    /**
     * An engine whose file loader is rooted in the normalization directory.
     */
    private VelocityEngine fileEngine()
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "file");
        ve.setProperty("resource.loader.file.path", ROOT);
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ve.init();
        return ve;
    }

    private String merge(VelocityEngine ve, String templateName)
    {
        Template template = ve.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.merge(new VelocityContext(), writer);
        return writer.toString();
    }

    /**
     * The default normalization collapses . and .. segments, and refuses a name
     * climbing above the root.
     */
    public void testDefaultNormalization()
    {
        ResourceLoader loader = new ClasspathResourceLoader();

        assertEquals("a.vm", loader.normalizeResourceName("a.vm"));
        assertEquals("sub/a.vm", loader.normalizeResourceName("sub/a.vm"));
        assertEquals("a.vm", loader.normalizeResourceName("./a.vm"));
        assertEquals("a.vm", loader.normalizeResourceName("sub/../a.vm"));
        assertEquals("sub/a.vm", loader.normalizeResourceName("sub/deeper/../a.vm"));
        assertEquals("/sub/a.vm", loader.normalizeResourceName("/sub/deeper/../a.vm"));

        assertNull(loader.normalizeResourceName("../a.vm"));
        assertNull(loader.normalizeResourceName("sub/../../a.vm"));
        assertNull(loader.normalizeResourceName("/../a.vm"));
    }

    /**
     * A template name climbing above the file loader root is not found.
     */
    public void testFileLoaderRefusesClimbingName()
    {
        VelocityEngine ve = fileEngine();

        // the file is really there, one level above the root
        assertTrue(new java.io.File(TEST_COMPARE_DIR + "/normalization/outside.txt").canRead());

        try
        {
            ve.getTemplate("../outside.txt");
            fail("Expected ResourceNotFoundException");
        }
        catch (ResourceNotFoundException rnfe)
        {
            // the message names the resource as asked
            assertTrue(rnfe.getMessage(), rnfe.getMessage().contains("../outside.txt"));
        }

        assertFalse(ve.resourceExists("../outside.txt"));
    }

    /**
     * Same through #include and #parse.
     */
    public void testDirectivesRefuseClimbingName()
    {
        VelocityEngine ve = fileEngine();

        try
        {
            merge(ve, "includeup.vm");
            fail("Expected ResourceNotFoundException");
        }
        catch (ResourceNotFoundException rnfe)
        {
            // expected
        }

        try
        {
            merge(ve, "parseup.vm");
            fail("Expected ResourceNotFoundException");
        }
        catch (ResourceNotFoundException rnfe)
        {
            // expected
        }
    }

    /**
     * A name whose .. segments stay inside the root is found.
     */
    public void testFileLoaderAcceptsInnerName()
    {
        VelocityEngine ve = fileEngine();

        assertEquals("inner", merge(ve, "sub/inner.vm"));
        assertEquals("included", merge(ve, "sub/../inc.vm"));
        assertEquals("included", merge(ve, "./inc.vm"));
        assertEquals("inner", merge(ve, "sub/../sub/inner.vm"));

        assertTrue(ve.resourceExists("sub/../inc.vm"));

        // and through #include
        assertEquals("included", merge(ve, "includedown.vm"));
    }

    /**
     * The classpath loader refuses a climbing name too.
     */
    public void testClasspathLoaderRefusesClimbingName()
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "class");
        ve.setProperty("resource.loader.class.class", ClasspathResourceLoader.class.getName());
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ve.init();

        // the resource is there under its plain name
        assertEquals("included", merge(ve, "normalization/root/inc.vm"));

        try
        {
            ve.getTemplate("normalization/root/../../normalization/../../inc.vm");
            fail("Expected ResourceNotFoundException");
        }
        catch (ResourceNotFoundException rnfe)
        {
            // expected
        }
    }

    /**
     * The string loader uses the name as an opaque key: it is left untouched.
     */
    public void testStringLoaderKeepsKeyUntouched()
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        ve.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        ve.setProperty("resource.loader.string.repository.name", "normalization.repo");
        ve.setProperty("resource.loader.string.repository.static", "false");
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);
        ve.init();

        StringResourceRepository repo =
            (StringResourceRepository)ve.getApplicationAttribute("normalization.repo");
        repo.putStringResource("../up.vm", "from the key with dots");
        repo.putStringResource("a/../b.vm", "another key with dots");

        assertEquals("from the key with dots", merge(ve, "../up.vm"));
        assertEquals("another key with dots", merge(ve, "a/../b.vm"));
        assertEquals("../up.vm", new StringResourceLoader().normalizeResourceName("../up.vm"));
    }
}
