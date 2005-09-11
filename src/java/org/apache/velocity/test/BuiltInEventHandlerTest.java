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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.implement.EscapeHtmlReference;
import org.apache.velocity.app.event.implement.EscapeJavaScriptReference;
import org.apache.velocity.app.event.implement.EscapeReference;
import org.apache.velocity.app.event.implement.EscapeSqlReference;
import org.apache.velocity.app.event.implement.EscapeXmlReference;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Tests the operation of the built in event handlers.
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id: EventCartridge.java,v 1.5 2004/03/19 17:13:33 dlr Exp $
 */
public class BuiltInEventHandlerTest extends BaseTestCase {

   
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
   private final static String FILE_RESOURCE_LOADER_PATH = "../test/includeevent";

   /**
    * Results relative to the build directory.
    */
   private static final String RESULTS_DIR = "../test/includeevent/results";

   /**
    * Results relative to the build directory.
    */
   private static final String COMPARE_DIR = "../test/includeevent/compare";

    /**
     * Default constructor.
     */
    public BuiltInEventHandlerTest()
    {
        super("BuiltInEventHandlerTestCase");
    }

    public BuiltInEventHandlerTest(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
       return new TestSuite(BuiltInEventHandlerTest.class);
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
    }

    /**
     * test that escape reference handler works with match restrictions
     * @throws Exception
     */
    public void testEscapeReferenceMatch() throws Exception
    {
        // set up HTML match on everything, JavaScript match on _js*
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference,org.apache.velocity.app.event.implement.EscapeJavaScriptReference");
        ve.setProperty("eventhandler.escape.javascript.match", "/.*_js*/");
        ve.init();
    
        Writer writer;
        
        // Html no JavaScript
        writer = new StringWriter();
        ve.evaluate(newEscapeContext(),writer,"test","$test1");
        assertEquals("Jimmy's &lt;b&gt;pizza&lt;/b&gt;",writer.toString());        

        // JavaScript and HTML
        writer = new StringWriter();
        ve.evaluate(newEscapeContext(),writer,"test","$test1_js");
        assertEquals("Jimmy\\'s &lt;b&gt;pizza&lt;/b&gt;",writer.toString());        
    
        // JavaScript and HTML
        writer = new StringWriter();
        ve.evaluate(newEscapeContext(),writer,"test","$test1_js_test");
        assertEquals("Jimmy\\'s &lt;b&gt;pizza&lt;/b&gt;",writer.toString());        
    
        // JavaScript and HTML (method call)
        writer = new StringWriter();
        ve.evaluate(newEscapeContext(),writer,"test","$test1_js.substring(0,7)");
        assertEquals("Jimmy\\'s",writer.toString());        
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
        
        try {
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


        } catch (Exception E)
        {
            fail("Shouldn't have thrown exception. " + E);
        }
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
    }


}
