package org.apache.velocity.test.misc;

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

import java.util.Properties;

/**
 * A VelocityEngine for tests: strict math is the next major version's only mode and the 2.5
 * default's deprecation warning is noise in a test log, so this engine enables it at init
 * unless the test set <code>runtime.strict_math</code> explicitly (a test about lenient math
 * sets it to false and keeps it).
 */
public class TestVelocityEngine extends VelocityEngine
{
    public TestVelocityEngine()
    {
        super();
        strictMathUnlessSet();
    }

    public TestVelocityEngine(Properties p)
    {
        super(p);
        strictMathUnlessSet();
    }

    public TestVelocityEngine(String propsFilename)
    {
        super(propsFilename);
        strictMathUnlessSet();
    }

    /**
     * The runtime initializes itself lazily on first use, bypassing any <code>init</code> override,
     * so the default is applied at construction and re-applied whenever a property set replaces
     * the user properties wholesale.
     */
    private void strictMathUnlessSet()
    {
        if (getProperty(RuntimeConstants.STRICT_MATH) == null)
        {
            setProperty(RuntimeConstants.STRICT_MATH, "true");
        }
    }

    @Override
    public void setProperties(Properties configuration)
    {
        super.setProperties(configuration);
        strictMathUnlessSet();
    }

    @Override
    public void setProperties(String propsFilename)
    {
        super.setProperties(propsFilename);
        strictMathUnlessSet();
    }

    @Override
    public void init(Properties p)
    {
        setProperties(p);
        init();
    }

    @Override
    public void init(String propsFilename)
    {
        setProperties(propsFilename);
        init();
    }
}
