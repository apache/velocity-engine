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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogger;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *  More specific parser tests where just templating
 *  isn't enough.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ParserTestCase extends TestCase
{
    public ParserTestCase(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
       return new TestSuite(ParserTestCase.class);
    }

    /**
     *  Test to make sure that using '=' in #if() throws a PEE
     */
    public void testEquals()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        TestLogger logger = new TestLogger();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, logger);
        ve.init();

        /*
         *  this should parse fine -> uses ==
         */

        String template = "#if($a == $b) foo #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

        /*
         *  this should throw an exception
         */

        template = "#if($a = $b) foo #end";

        try
        {
            ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);
            fail("Could evaluate template with errors!");
        }
        catch(ParseErrorException pe)
        {
            // Do nothing
        }
    }

    /**
     *  Test to see if we force the first arg to #macro() to be a word
     */
    public void testMacro()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        TestLogger logger = new TestLogger();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, logger);
        ve.init();

        /*
         * this should work
         */

        String template = "#macro(foo) foo #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

         /*
          *  this should throw an exception
          */

        template = "#macro($x) foo #end";

        try
        {
            ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);
            fail("Could evaluate macro with errors!");
        }
        catch(ParseErrorException pe)
        {
            // Do nothing
        }
    }

    /**
     *  Test to see if don't tolerage passing word tokens in anything but the
     *  0th arg to #macro() and the 1th arg to foreach()
     */
    public void testArgs()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        TestLogger logger = new TestLogger();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, logger);
        ve.init();

        /*
         * this should work
         */

        String template = "#macro(foo) foo #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

         /*
          *  this should work - spaces intentional
          */

        template = "#foreach(  $i     in  $woogie   ) end #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

        /*
         *  this should bomb
         */

       template = "#macro(   foo $a) $a #end #foo(woogie)";

        try
        {
            ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);
            fail("Evaluation of macro with errors succeeded!");
        }
        catch(ParseErrorException pe)
        {
            // Do nothing
        }
    }

    /**
     *  Test to see if we toString is called multiple times on references.
     */
    public void testASTReferenceToStringOnlyCalledOnce()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        TestLogger logger = new TestLogger();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, logger);
        ve.init();

        String template = "$counter";

        ToStringCounter counter = new ToStringCounter();
        Map m = new HashMap();
        m.put("counter", counter);

        ve.evaluate(new VelocityContext(m), new StringWriter(), "foo", template);

        assertEquals(1, counter.timesCalled);
    }

    public static class ToStringCounter {
        public int timesCalled = 0;
        public String toString() {
            this.timesCalled++;
            return "foo";
        }
    }

}
