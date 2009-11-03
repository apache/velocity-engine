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

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.test.misc.TestLogChute;
import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;

/**
 * Tests uberspectors chaining
 */
public class ChainedUberspectorsTestCase extends BaseTestCase 
{

    VelocityEngine engine;
    
    public ChainedUberspectorsTestCase(String name)
    	throws Exception
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ChainedUberspectorsTestCase.class);
    }

    public void setUp()
            throws Exception
    {
        engine = new VelocityEngine();
        
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, TestLogChute.class.getName());
        engine.addProperty(RuntimeConstants.UBERSPECT_CLASSNAME,"org.apache.velocity.util.introspection.UberspectImpl");
        engine.addProperty(RuntimeConstants.UBERSPECT_CLASSNAME,"org.apache.velocity.test.util.introspection.ChainedUberspectorsTestCase$ChainedUberspector");
        engine.addProperty(RuntimeConstants.UBERSPECT_CLASSNAME,"org.apache.velocity.test.util.introspection.ChainedUberspectorsTestCase$LinkedUberspector");
	    engine.init();
    }

    public void tearDown()
    {
    }

    public void testChaining()
    	throws Exception
    {
        VelocityContext context = new VelocityContext();
        context.put("foo",new Foo());
        StringWriter writer = new StringWriter();

        engine.evaluate(context,writer,"test","$foo.zeMethod()");
        assertEquals(writer.toString(),"ok");

        engine.evaluate(context,writer,"test","#set($foo.foo = 'someValue')");

        writer = new StringWriter();
        engine.evaluate(context,writer,"test","$foo.bar");
        assertEquals(writer.toString(),"someValue");

        writer = new StringWriter();
        engine.evaluate(context,writer,"test","$foo.foo");
        assertEquals(writer.toString(),"someValue");
    }

    // replaces getFoo by getBar
    public static class ChainedUberspector extends AbstractChainableUberspector
    {
        public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info info)
            throws Exception
        {
            identifier = identifier.replaceAll("foo","bar");
            return inner.getPropertySet(obj,identifier,arg,info);
        }
    }

    // replaces setFoo by setBar
    public static class LinkedUberspector extends UberspectImpl
    {
        public VelPropertyGet getPropertyGet(Object obj, String identifier, Info info)
            throws Exception
        {
            identifier = identifier.replaceAll("foo","bar");
            return super.getPropertyGet(obj,identifier,info);
        }
    }

    public static class Foo
    {
        private String bar;

        public String zeMethod() { return "ok"; }
        public String getBar() { return bar; }
        public void setBar(String s) { bar = s; }
    }

}
