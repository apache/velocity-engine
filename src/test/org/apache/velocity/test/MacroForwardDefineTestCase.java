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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.app.Velocity;

/**
 * Make sure that a forward referenced macro inside another macro definition does
 * not report an error in the log.
 * (VELOCITY-71).
 * 
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class MacroForwardDefineTestCase 
        extends BaseTestCase
{
   /**
    * Path for templates. This property will override the
    * value in the default velocity properties file.
    */
   private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/macroforwarddefine";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/macroforwarddefine";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/macroforwarddefine/compare";

    /**
     * Default constructor.
     */
    public MacroForwardDefineTestCase(String name)
    {
        super(name);
    }

    public void setUp()
        throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);
        
        InputStream stream = new FileInputStream(FILE_RESOURCE_LOADER_PATH + "/velocity.properties");
        Properties p = new Properties();
        p.load(stream);
        
        p.setProperty("file.resource.loader.path", FILE_RESOURCE_LOADER_PATH );
        p.setProperty("runtime.log", RESULTS_DIR + "/velocity.log");

        Velocity.init(p);
    }

    public static Test suite()
    {
       return new TestSuite(MacroForwardDefineTestCase.class);
    }

    public void testLogResult()
        throws Exception
    {
        if ( !isMatch(RESULTS_DIR, COMPARE_DIR, "velocity.log",
                        null, "cmp"))
        {
            fail("Output incorrect.");
        }
    }
}
