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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Test parser exception is generated with appropriate info.
 *
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id$
 */
public class ParseExceptionTestCase extends BaseTestCase
{
    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = "parseexception";


    /**
     * Default constructor.
     * @param name name of test
     */
    public ParseExceptionTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        Velocity.reset();
        super.setUp();
    }

    /**
     * Tests that parseException has useful info when called by template.marge()
     * @throws Exception
     */
    public void testParseExceptionFromTemplate ()
            throws Exception
    {

        VelocityEngine ve = new VelocityEngine();

        ve.setProperty("file.resource.loader.cache", "true");
        ve.setProperty("file.resource.loader.path", TemplateTestBase.TEST_COMPARE_DIR + "/" + FILE_RESOURCE_LOADER_PATH);
        ve.init();


        Writer writer = new StringWriter();

        VelocityContext context = new VelocityContext();

        try
        {
            Template template = ve.getTemplate("badtemplate.vm");
            template.merge(context, writer);
            fail("Should have thown a ParseErrorException");
        }
        catch (ParseErrorException e)
        {
            assertEquals("badtemplate.vm",e.getTemplateName());
            assertEquals(5,e.getLineNumber());
            assertEquals(9,e.getColumnNumber());
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }

    /**
     * Tests that parseException has useful info when thrown in VelocityEngine.evaluate()
     * @throws Exception
     */
    public void testParseExceptionFromEval ()
            throws Exception
    {
         assertEvalExceptionAt("   #set($abc)   ", 1, 13);
    }

    /**
     * Tests that parseException has useful info when thrown in VelocityEngine.evaluate()
     * and the problem comes from a macro definition
     * @throws Exception
     */
    public void testParseExceptionFromMacroDef ()
            throws Exception
    {
        assertEvalExceptionAt("#macro($blarg) foo #end", 1, 7);
    }

    /**
     * Tests that parseException has useful info when thrown in VelocityEngine.evaluate()
     * and the problem comes from a macro definition
     * @throws Exception
     */
    public void testParseExceptionFromMacroDefBody ()
            throws Exception
    {
        assertEvalExceptionAt("#macro(aa $blarg) #set(!! = bb) #end #aa('aa')", 1, 24);
    }

    /**
     * Tests that parseException has useful info when thrown in VelocityEngine.evaluate()
     * and the problem comes from a macro invocation
     * @throws Exception
     */
    public void testParseExceptionFromMacroInvoke ()
    {
        assertEvalExceptionAt("#macro(   foo $a) $a #end #foo(woogie)", 1, 32);
    }


    /**
     * Tests that parseException has useful info with macro calls with
     * invalid number of arguments
     * @throws Exception
     */
    public void testParseExceptionMacroInvalidArgumentCount ()
            throws Exception
    {
        engine.setProperty(RuntimeConstants.VM_ARGUMENTS_STRICT,"true");
        assertEvalExceptionAt("#macro(foo $a) $a #end #foo('test1' 'test2')", 1, 24);
    }


    /**
     * Tests that parseException has useful info with macro calls with
     * invalid number of arguments
     * @throws Exception
     */
    public void testParseExceptionMacroInvalidArgumentCountNoException ()
            throws Exception
    {
       assertEvalEquals("test1", "#macro(foo $a)$a#end#foo('test1' 'test2')");
    }

    /**
     * Minus is not any more allowed inside a symbol (reference, property or method).
     * @throws Exception
     */
    public void testParseExceptionMinusSignDissalowed()
        throws Exception
    {
        assertEvalExceptionAt("${foo-bar}", 1, 6);
    }
}
