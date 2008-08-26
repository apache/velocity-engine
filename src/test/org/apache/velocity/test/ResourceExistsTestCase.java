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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

/**
 * Test the resource exists method
 *
 * @version $Id: ResourceExistsTestCase.java 687191 2008-08-19 23:02:41Z nbubna $
 */
public class ResourceExistsTestCase extends BaseTestCase
{
    private VelocityEngine velocity;
    private String path = TEST_COMPARE_DIR + "/resourceexists";

    public ResourceExistsTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        velocity = new VelocityEngine();
        // pass in an instance to Velocity
        velocity.addProperty("resource.loader", "myfile,string");
        velocity.setProperty("myfile.resource.loader.class", FileResourceLoader.class.getName());
        velocity.setProperty("myfile.resource.loader.path", path);
        velocity.setProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        velocity.setProperty(velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
    }

    public void testFileResourceExists() throws Exception
    {
        if (!velocity.resourceExists("testfile.vm"))
        {
            String msg = "testfile.vm was not found in path "+path;
            System.out.println(msg);
            fail(msg);
        }
        if (velocity.resourceExists("nosuchfile.vm"))
        {
            String msg = "nosuchfile.vm should not have been found in path "+path;
            System.out.println(msg);
            fail(msg);
        }
    }

    public void testStringResourceExists() throws Exception
    {
        assertFalse(velocity.resourceExists("foo.vm"));
        StringResourceLoader.getRepository().putStringResource("foo.vm", "Make it so!");
        assertTrue(velocity.resourceExists("foo.vm"));
    }
}
