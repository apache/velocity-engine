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
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MacroOverflowException;
import org.apache.velocity.test.misc.TestLogger;

import java.io.StringWriter;

/**
 * This class tests strange Velocimacro issues.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class VelocimacroTestCase extends TestCase
{
    private String template1 = "#macro(foo $a)$a#end #macro(bar $b)#foo($b)#end #foreach($i in [1..3])#if($i == 3)#foo($i)#else#bar($i)#end#end";
    private String result1 = "  123";
    private String template2 = "#macro(bar $a)#set($a = $a + 1)$a#bar($a)#end#bar(0)";
    private String template3 = "#macro(baz $a)#set($a = $a + 1)$a#inner($a)#end#macro(inner $b)#baz($b)#end#baz(0)";
    private String template4 = "#macro(bad $a)#set($a = $a + 1)$a#inside($a)#end#macro(inside $b)#loop($b)#end#macro(loop $c)#bad($c)#end#bad(0)";

    public VelocimacroTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        /*
         *  setup local scope for templates
         */
        Velocity.reset();
        Velocity.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        Velocity.setProperty( Velocity.VM_MAX_DEPTH, 5);
        Velocity.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());
        Velocity.init();
    }

    public static Test suite()
    {
        return new TestSuite(VelocimacroTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testVelociMacro ()
            throws Exception
    {
        VelocityContext context = new VelocityContext();

        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "vm_chain1", template1);

        String out = writer.toString();

        if( !result1.equals( out ) )
        {
            fail("output incorrect.");
        }
    }

    /**
     * Test case for evaluating max calling depths of macros
     */
    public void testVelociMacroCallMax()
            throws Exception
    {
        VelocityContext context = new VelocityContext();
        StringWriter writer = new StringWriter();

        try
        {
            Velocity.evaluate(context, writer, "vm_chain2", template2);
            fail("Did not exceed max macro call depth as expected");
        }
        catch (MacroOverflowException e)
        {
            assertEquals("Max calling depth of 5 was exceeded in macro 'bar'"+
                            " with Call Stack:bar->bar->bar->bar->bar at vm_chain2[line 1, column 15]",
                         e.getMessage());
        }

        try
        {
            Velocity.evaluate(context, writer, "vm_chain3", template3);
            fail("Did not exceed max macro call depth as expected");
        }
        catch (MacroOverflowException e)
        {
            assertEquals("Max calling depth of 5 was exceeded in macro 'inner'"+
                            " with Call Stack:baz->inner->baz->inner->baz at vm_chain3[line 1, column 64]",
                         e.getMessage());
        }

        try
        {
            Velocity.evaluate(context, writer, "vm_chain4", template4);
            fail("Did not exceed max macro call depth as expected");
        }
        catch (MacroOverflowException e)
        {
            assertEquals("Max calling depth of 5 was exceeded in macro 'loop'"+
                            " with Call Stack:bad->inside->loop->bad->inside at vm_chain4[line 1, column 94]",
                         e.getMessage());
        }
    }
}
