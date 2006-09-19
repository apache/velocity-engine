package org.apache.velocity.test;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * This class tests strange Velocimacro issues.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class VelocimacroTestCase extends TestCase
{
    private String template1 = "#macro(foo $a)$a#end #macro(bar $b)#foo($b)#end #foreach($i in [1..3])#bar($i)#end";
    private String result1 = "  123";

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
        Velocity.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
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
}
