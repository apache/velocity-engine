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
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-742.
 */
public class Velocity742TestCase extends BaseTestCase
{
    public Velocity742TestCase(String name)
    {
        super(name);
    }

    protected void setUpEngine(VelocityEngine engine)
    {
        // we need to call init here because otherwise it is not called until assertEvalEquals
        // and therefore the removeDirective call is ignored.
        engine.init();
    }

    public void testDisableAndRestoreDirective()
    {
        String s = "#include('doesnotexist.vm') directive is disabled";

        // first remove  the #include directive and see that is treated as normal text
        engine.removeDirective("include");
        assertEvalEquals(s, s);

        // now reload the directive and see that the include directive works again and
        // Velocity throws ResourceNotFoundException because it can't find the template
        engine.loadDirective("org.apache.velocity.runtime.directive.Include");
        assertEvalException(s, ResourceNotFoundException.class);
    }
}
