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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.introspection.IntrospectorCache;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ClassloaderChangeTestCase extends TestCase
{
    private VelocityEngine ve = null;
	private TestLogger logger = null;

    private static String OUTPUT = "Hello From Foo";

    /**
     * Default constructor.
     */
    public ClassloaderChangeTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        ve = new VelocityEngine();
        logger = new TestLogger(false, true);
        logger.setEnabledLevel(TestLogger.LOG_LEVEL_DEBUG);
        ve.setProperty(VelocityEngine.RUNTIME_LOG_INSTANCE, logger);
        ve.init();
    }

    public static Test suite ()
    {
        return new TestSuite(ClassloaderChangeTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testClassloaderChange()
        throws Exception
    {
        logger.on();

        VelocityContext vc = new VelocityContext();
        Object foo = null;

        /*
         *  first, we need a classloader to make our foo object
         */

        TestClassloader cl = new TestClassloader();
        Class<?> fooclass = cl.loadClass("Foo");
        foo = fooclass.newInstance();

        /*
         *  put it into the context
         */
        vc.put("foo", foo);

        /*
         *  and render something that would use it
         *  that will get it into the introspector cache
         */
        StringWriter writer = new StringWriter();
        ve.evaluate( vc, writer, "test", "$foo.doIt()");

        /*
         *  Check to make sure ok.  note the obvious
         *  dependency on the Foo class...
         */

        if ( !writer.toString().equals( OUTPUT ))
        {
            fail("Output from doIt() incorrect");
        }

        /*
         * and do it again :)
         */
        cl = new TestClassloader();
        fooclass = cl.loadClass("Foo");
        foo = fooclass.newInstance();

        vc.put("foo", foo);

        writer = new StringWriter();
        ve.evaluate( vc, writer, "test", "$foo.doIt()");

        if ( !writer.toString().equals( OUTPUT ))
        {
            fail("Output from doIt() incorrect");
        }

        if (!logger.getLog().contains(IntrospectorCache.CACHEDUMP_MSG))
        {
            fail("Didn't see introspector cache dump.");
        }
    }

    /**
     *  Simple (real simple...) classloader that depends
     *  on a Foo.class being located in the classloader
     *  directory under test
     */
    public static class TestClassloader extends ClassLoader
    {
        private final static String testclass =
            "classloader/Foo.class";

        private Class<?> fooClass = null;

        public TestClassloader()
                throws Exception
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream fis = getClass().getResourceAsStream("/" + testclass);
            IOUtils.copy(fis, os);
            fis.close();
            os.close();

            byte[] barr = os.toByteArray();

            fooClass = defineClass("classloader.Foo", barr, 0, barr.length);
        }


        public Class<?> findClass(String name)
        {
            return fooClass;
        }
    }
}
