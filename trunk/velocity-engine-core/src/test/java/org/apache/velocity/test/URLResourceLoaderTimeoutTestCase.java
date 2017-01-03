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

import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.URLResourceLoader;
import org.apache.velocity.test.misc.TestLogger;

/**
 * This class tests support for custom timeouts in URLResourceLoader.
 */
public class URLResourceLoaderTimeoutTestCase extends BaseTestCase
{
    private static boolean isJava5plus;
    static
    {
        try
        {
            Class.forName("java.lang.annotation.Annotation");
            isJava5plus = true;
        }
        catch (ClassNotFoundException cnfe)
        {
            isJava5plus = false;
        }
    }
    private TestLogger logger = new TestLogger();
    private URLResourceLoader loader = new URLResourceLoader();
    private int timeout = 2000;

    public URLResourceLoaderTimeoutTestCase(String name)
    {
       super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty("resource.loader", "url");
        engine.setProperty("url.resource.loader.instance", loader);
        engine.setProperty("url.resource.loader.timeout", new Integer(timeout));

        // actual instance of logger
        logger.on();
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, logger);
        engine.setProperty("runtime.log.logsystem.test.level", "debug");
        engine.init();
    }

    public void testTimeout()
    {
        if (isJava5plus)
        {
            System.out.println("Testing a 1.5+ JDK");
            assertEquals(timeout, loader.getTimeout());
        }
        else
        {
            System.out.println("Testing a pre-1.5 JDK");
            assertEquals(-1, loader.getTimeout());
        }
    }

}
