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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.EscapeHtmlReference;
import org.apache.velocity.app.event.implement.EscapeJavaScriptReference;
import org.apache.velocity.app.event.implement.EscapeReference;
import org.apache.velocity.app.event.implement.EscapeSqlReference;
import org.apache.velocity.app.event.implement.EscapeXmlReference;
import org.apache.velocity.app.event.implement.InvalidReferenceInfo;
import org.apache.velocity.app.event.implement.ReportInvalidReferences;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Tests the operation of the built in event handlers.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class BuiltInEventHandlerTestCase extends BaseTestCase {


    /**
    * VTL file extension.
    */
   private static final String TMPL_FILE_EXT = "vm";

   /**
    * Comparison file extension.
    */
   private static final String CMP_FILE_EXT = "cmp";

   /**
    * Comparison file extension.
    */
   private static final String RESULT_FILE_EXT = "res";

   /**
    * Path for templates. This property will override the
    * value in the default velocity properties file.
    */
   private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/includeevent";

   /**
    * Results relative to the build directory.
    */
   private static final String RESULTS_DIR = TEST_RESULT_DIR + "/includeevent";

   /**
    * Results relative to the build directory.
    */
   private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/includeevent/compare";

    /**
     * Default constructor.
     */
    public BuiltInEventHandlerTestCase(String name)
    {
        super(name);
    }

    public void setUp()
    {
        assureResultsDirectoryExists(RESULTS_DIR);
    }

    public static Test suite()
    {
       return new TestSuite(BuiltInEventHandlerTestCase.class);
    }



    /**
     * Test reporting of invalid syntax
     * @throws Exception
     */
    public void testReportInvalidReferences1() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ReportInvalidReferences reporter = new ReportInvalidReferences();
        ve.init();

        VelocityContext context = new VelocityContext();
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(reporter);
        ec.attachToContext(context);

        context.put("a1","test");
        context.put("b1","test");
        Writer writer = new StringWriter();

        ve.evaluate(context,writer,"test","$a1 $c1 $a1.length() $a1.foobar()");

        List errors = reporter.getInvalidReferences();
        assertEquals(2,errors.size());
        assertEquals("$c1",((InvalidReferenceInfo) errors.get(0)).getInvalidReference());
        assertEquals("$a1.foobar()",((InvalidReferenceInfo) errors.get(1)).getInvalidReference());

        System.out.println("Caught invalid references (local configuration).");
    }

    public void testReportInvalidReferences2() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("eventhandler.invalidreference.exception","true");
        ReportInvalidReferences reporter = new ReportInvalidReferences();
        ve.init();

        VelocityContext context = new VelocityContext();
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(reporter);
        ec.attachToContext(context);

        context.put("a1","test");
        context.put("b1","test");
        Writer writer = new StringWriter();

        ve.evaluate(context,writer,"test","$a1 no problem");

        try {
            ve.evaluate(context,writer,"test","$a1 $c1 $a1.length() $a1.foobar()");
            fail ("Expected exception.");
        } catch (RuntimeException E) {}


        System.out.println("Caught invalid references (global configuration).");

    }

    /**
     * Test escaping
     * @throws Exception
     */
    public void testEscapeHtml() throws Exception
    {
        EscapeReference esc = new EscapeHtmlReference();
        assertEquals("test string&amp;another&lt;b&gt;bold&lt;/b&gt;test",esc.referenceInsert("","test string&another<b>bold</b>test"));
        assertEquals("&lt;&quot;&gt;",esc.referenceInsert("","<\">"));
        assertEquals("test string",esc.referenceInsert("","test string"));

        System.out.println("Correctly escaped HTML");

    }

    /**
     * Test escaping
     * @throws Exception
     */
    public void testEscapeXml() throws Exception
    {
        EscapeReference esc = new EscapeXmlReference();
        assertEquals("test string&amp;another&lt;b&gt;bold&lt;/b&gt;test",esc.referenceInsert("","test string&another<b>bold</b>test"));
        assertEquals("&lt;&quot;&gt;",esc.referenceInsert("","<\">"));
        assertEquals("&apos;",esc.referenceInsert("","'"));
        assertEquals("test string",esc.referenceInsert("","test string"));

        System.out.println("Correctly escaped XML");

    }

    /**
     * Test escaping
     * @throws Exception
     */
    public void testEscapeSql() throws Exception
    {
        EscapeReference esc = new EscapeSqlReference();
        assertEquals("Jimmy''s Pizza",esc.referenceInsert("","Jimmy's Pizza"));
        assertEquals("test string",esc.referenceInsert("","test string"));

        System.out.println("Correctly escaped SQL");

    }

    /**
     * Test escaping
     * @throws Exception
     */
    public void testEscapeJavaScript() throws Exception
    {
        EscapeReference esc = new EscapeJavaScriptReference();
        assertEquals("Jimmy\\'s Pizza",esc.referenceInsert("","Jimmy's Pizza"));
        assertEquals("test string",esc.referenceInsert("","test string"));


        System.out.println("Correctly escaped Javascript");
    }

    /**
     * test that escape reference handler works with no match restrictions
     * @throws Exception
     */
    public void testEscapeReferenceMatchAll() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference");
        ve.init();

        Context context;
        Writer writer;

        // test normal reference
        context = new VelocityContext();
        writer = new StringWriter();
        context.put("bold","<b>");
        ve.evaluate(context,writer,"test","$bold test & test");
        assertEquals("&lt;b&gt; test & test",writer.toString());

        // test method reference
        context = new VelocityContext();
        writer = new StringWriter();
        context.put("bold","<b>");
        ve.evaluate(context,writer,"test","$bold.substring(0,1)");
        assertEquals("&lt;",writer.toString());

        System.out.println("Escape matched all references (global configuration)");

    }

    /**
     * test that escape reference handler works with match restrictions
     * @throws Exception
     */
    public void testEscapeReferenceMatch() throws Exception
    {
        try
        {
            // set up HTML match on everything, JavaScript match on _js*
            VelocityEngine ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference,org.apache.velocity.app.event.implement.EscapeJavaScriptReference");
            ve.setProperty("eventhandler.escape.javascript.match", "/.*_js.*/");
            ve.init();

            System.out.println("Successfully engine init()");

            Writer writer;

            // Html no JavaScript
            writer = new StringWriter();
            ve.evaluate(newEscapeContext(),writer,"test","$test1");
            System.out.println("Escaping test1: "+writer.toString());
            assertEquals("Jimmy's &lt;b&gt;pizza&lt;/b&gt;",writer.toString());
            System.out.println("Successfully escaped test1: ");

            // comment out (temporarily) bad test
            /**

            // JavaScript and HTML
            writer = new StringWriter();
            ve.evaluate(newEscapeContext(),writer,"test","$test1_js");
            System.out.println("Escaping test1_js: "+writer.toString());
            assertEquals("Jimmy\\'s &lt;b&gt;pizza&lt;/b&gt;",writer.toString());
            System.out.println("Successfully escaped test1_js");

            // JavaScript and HTML
            writer = new StringWriter();
            ve.evaluate(newEscapeContext(),writer,"test","$test1_js_test");
            System.out.println("Escaping test1_js_test: "+writer.toString());
            assertEquals("Jimmy\\'s &lt;b&gt;pizza&lt;/b&gt;",writer.toString());
            System.out.println("Successfully escaped test1_js_test");

            // JavaScript and HTML (method call)
            writer = new StringWriter();
            ve.evaluate(newEscapeContext(),writer,"test","$test1_js.substring(0,7)");
            System.out.println("Escaping test1_js.substring(0,7): "+writer.toString());
            assertEquals("Jimmy\\'s",writer.toString());
            System.out.println("Successfully escaped test1_js.substring(0,7)");

               **/
            System.out.println("Escape selected references (global configuration)");

        }

        catch (AssertionFailedError e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();

            throw e;
        }

    }

    private Context newEscapeContext()
    {
        Context context = new VelocityContext();
        context.put("test1","Jimmy's <b>pizza</b>");
        context.put("test1_js","Jimmy's <b>pizza</b>");
        context.put("test1_js_test","Jimmy's <b>pizza</b>");
        return context;
    }

    public void testPrintExceptionHandler() throws Exception
    {
        VelocityEngine ve1 = new VelocityEngine();
        ve1.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, "org.apache.velocity.app.event.implement.PrintExceptions");
        ve1.init();

        VelocityEngine ve2 = new VelocityEngine();
        ve2.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, "org.apache.velocity.app.event.implement.PrintExceptions");
        ve2.setProperty("eventhandler.methodexception.message","true");
        ve2.init();

        VelocityEngine ve3 = new VelocityEngine();
        ve3.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, "org.apache.velocity.app.event.implement.PrintExceptions");
        ve3.setProperty("eventhandler.methodexception.stacktrace","true");
        ve3.init();

        Context context;
        StringWriter writer;

        context = new VelocityContext();
        context.put("list",new ArrayList());

        // exception only
        writer = new StringWriter();
        ve1.evaluate(context,writer,"test","$list.get(0)");
        assertTrue(writer.toString().indexOf("IndexOutOfBoundsException") != -1);
        assertTrue(writer.toString().indexOf("Index: 0, Size: 0") == -1);
        assertTrue(writer.toString().indexOf("ArrayList") == -1);

        // message
        writer = new StringWriter();
        ve2.evaluate(context,writer,"test","$list.get(0)");
        assertTrue(writer.toString().indexOf("IndexOutOfBoundsException") != -1);
        assertTrue(writer.toString().indexOf("Index: 0, Size: 0") != -1);
        assertTrue(writer.toString().indexOf("ArrayList") == -1);

        // stack trace
        writer = new StringWriter();
        ve3.evaluate(context,writer,"test","$list.get(0)");
        assertTrue(writer.toString().indexOf("IndexOutOfBoundsException") != -1);
        assertTrue(writer.toString().indexOf("ArrayList") != -1);

        System.out.println("PrintException handler successful.");

    }

    public void testIncludeNotFound() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, "org.apache.velocity.app.event.implement.IncludeNotFound");
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        ve.init();

        Template template;
        FileOutputStream fos;
        Writer fwriter;
        Context context;

        template = ve.getTemplate( getFileName(null, "test6", TMPL_FILE_EXT) );

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, "test6", RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        context = new VelocityContext();
        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "test6", RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }

        System.out.println("IncludeNotFound handler successful.");

    }

    public void testIncludeRelativePath() throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, "org.apache.velocity.app.event.implement.IncludeRelativePath");
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        ve.init();

        Template template;
        FileOutputStream fos;
        Writer fwriter;
        Context context;

        template = ve.getTemplate( getFileName(null, "subdir/test2", TMPL_FILE_EXT) );

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, "test2", RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        context = new VelocityContext();
        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "test2", RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }

        System.out.println("IncludeRelativePath handler successful.");

    }
}
