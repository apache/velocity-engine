package org.apache.velocity.test.util.introspection;

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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.test.misc.TestLogger;


import java.io.StringWriter;

/**
 * Tests DeprecatedCheckUberspector
 */
public class DeprecatedCheckUberspectorsTestCase extends BaseTestCase {

    public DeprecatedCheckUberspectorsTestCase(String name)
    	throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DeprecatedCheckUberspectorsTestCase.class);
    }

    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty(Velocity.RUNTIME_LOG_INSTANCE, new TestLogger(false, true));
        engine.addProperty(Velocity.UBERSPECT_CLASSNAME, "org.apache.velocity.util.introspection.UberspectImpl");
        engine.addProperty(Velocity.UBERSPECT_CLASSNAME, "org.apache.velocity.util.introspection.DeprecatedCheckUberspector");
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("obj1", new StandardObject());
        context.put("obj2", new DeprecatedObject());
    }

    public void testDeprecatedCheck()
    	throws Exception
    {
        engine.init(); // make sure the engine is initialized, so that we get the logger we configured
        TestLogger logger =(TestLogger)engine.getLog();
        logger.startCapture(); // reset log capture
        StringWriter writer = new StringWriter();
        engine.evaluate(context, writer, "test", "$obj1.foo() $obj1.bar $obj2.foo() $obj2.bar");
        String log = logger.getLog();
        String lines[] = log.split("\\r?\\n");
        assertEquals(lines[0], "  [info] Deprecated usage of method [org.apache.velocity.test.util.introspection.DeprecatedCheckUberspectorsTestCase.StandardObject.foo] in test@1,7");
        assertEquals(lines[1], "  [info] Deprecated usage of getter [org.apache.velocity.test.util.introspection.DeprecatedCheckUberspectorsTestCase.StandardObject.getBar] in test@1,19");
        assertEquals(lines[2], "  [info] Deprecated usage of method [org.apache.velocity.test.util.introspection.DeprecatedCheckUberspectorsTestCase.DeprecatedObject.foo] in test@1,29");
        assertEquals(lines[3], "  [info] Deprecated usage of getter [org.apache.velocity.test.util.introspection.DeprecatedCheckUberspectorsTestCase.DeprecatedObject.getBar] in test@1,41");
    }

    public static class StandardObject
    {
        @Deprecated
        public String foo()
        {
            return "foo";
        }

        @Deprecated
        public String getBar()
        {
            return "bar";
        }
    }

    @Deprecated
    public static class DeprecatedObject
    {
        public String foo()
        {
            return "foo";
        }

        public String getBar()
        {
            return "bar";
        }
    }
}
