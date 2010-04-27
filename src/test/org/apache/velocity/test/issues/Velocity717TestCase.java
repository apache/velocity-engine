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

import java.io.*;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.context.Context;
// import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;

/**
 * https://issues.apache.org/jira/browse/VELOCITY-717
 */
public class Velocity717TestCase extends BaseTestCase implements IncludeEventHandler
{
    public Velocity717TestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        Velocity.addProperty(Velocity.FILE_RESOURCE_LOADER_PATH,
                             TEST_COMPARE_DIR + "/includeevent");
        
        // this setting enables "namespaces" (see VelocimacroManager)        
        Velocity.addProperty(Velocity.VM_PERM_INLINE_LOCAL, "true");

        Velocity.init();
    }

    /**
     * Runs the test.
     */
    public void testIncludeEventHandlingWithNullReturn()
            throws Exception
    {
        Template template1 = RuntimeSingleton.getTemplate(
            getFileName(null, "test8", "vm"));

        Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));

        Context context = new VelocityContext();

        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(this);
        ec.attachToContext( context );

        template1.merge(context, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Sample handler with different behaviors for the different tests.
     */
    public String includeEvent( String includeResourcePath, String currentResourcePath, String directiveName)
    {
         return null;
    }
}