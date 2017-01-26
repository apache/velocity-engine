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
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.test.misc.TestLogger;

/**
 * Test the resource exists method
 *
 * @version $Id: ResourceExistsTestCase.java 687191 2008-08-19 23:02:41Z nbubna $
 */
public class ResourceExistsTestCase extends BaseTestCase
{
    private VelocityEngine velocity;
    private String path = TEST_COMPARE_DIR + "/resourceexists";
    private TestLogger logger = new TestLogger();

    public ResourceExistsTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
try {
        velocity = new VelocityEngine();
        velocity.setProperty("resource.loader", "file,string");
        velocity.setProperty("file.resource.loader.path", path);
        velocity.setProperty("string.resource.loader.class", StringResourceLoader.class.getName());

        // actual instance of logger
        logger.on();
        velocity.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, logger);
        velocity.setProperty("runtime.log.logsystem.test.level", "debug");
} catch (Exception e) {
    System.out.println("exception via gump: "+e);
    e.printStackTrace();
    System.out.println("log: "+logger.getLog());
}
    }

    public void testFileResourceExists() throws Exception
    {
try {
        if (!velocity.resourceExists("testfile.vm"))
        {
            String msg = "testfile.vm was not found in path "+path;
            System.out.println(msg);
            System.out.println("Log was: "+logger.getLog());
            path = path+"/testfile.vm";
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                System.out.println("file system found "+path);
            } else {
                System.out.println(file+" could not be found as a file");
            }
            fail(msg);
        }
        if (velocity.resourceExists("nosuchfile.vm"))
        {
            String msg = "nosuchfile.vm should not have been found in path "+path;
            System.out.println(msg);
            fail(msg);
        }
} catch (Exception e) {
    System.out.println("exception via gump: "+e);
    e.printStackTrace();
    System.out.println("log: "+logger.getLog());
}
    }

    public void testStringResourceExists() throws Exception
    {
try {
        assertFalse(velocity.resourceExists("foo.vm"));
        StringResourceLoader.getRepository().putStringResource("foo.vm", "Make it so!");
        assertTrue(velocity.resourceExists("foo.vm"));
} catch (Exception e) {
    System.out.println("exception via gump: "+e);
    e.printStackTrace();
    System.out.println("log: "+logger.getLog());
}
    }
}
