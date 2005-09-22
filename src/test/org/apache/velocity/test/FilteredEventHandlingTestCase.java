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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Tests event handling for all event handlers when multiple event handlers are
 * assigned for each type.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: EventHandlingTestCase.java,v 1.7 2004/03/19 17:13:38 dlr Exp $
 */
public class FilteredEventHandlingTestCase extends BaseTestCase implements LogSystem
{
   
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
   private final static String FILE_RESOURCE_LOADER_PATH = "test/includeevent";

   /**
    * Results relative to the build directory.
    */
   private static final String RESULTS_DIR = "target/test/includeevent";

   /**
    * Results relative to the build directory.
    */
   private static final String COMPARE_DIR = "test/includeevent/compare";

   
    private String logString = null;

    /**
     * Default constructor.
     */
    public FilteredEventHandlingTestCase()
    {
        super("FilteredEventHandlingTestCase");
    }


    /**
     * Required by LogSystem
     */
    public void init( RuntimeServices rs )
    {
        /* don't need it...*/
    }

    public static junit.framework.Test suite ()
    {
        return new FilteredEventHandlingTestCase();
    }

    public void runTest() throws Exception
    {
        String handler1 = "org.apache.velocity.test.eventhandler.Handler1";
        String handler2 = "org.apache.velocity.test.eventhandler.Handler2";
        String sequence1 = handler1 + "," + handler2;
        String sequence2 = handler2 + "," + handler1;

        assureResultsDirectoryExists(RESULTS_DIR);

        /**
         * Set up two VelocityEngines that will apply the handlers in both orders
         */
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this);
        ve.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, sequence1);
        ve.setProperty(RuntimeConstants.EVENTHANDLER_NULLSET, sequence1);
        ve.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, sequence1);
        ve.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, sequence1);
        ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);        
        ve.init();

        VelocityEngine ve2 = new VelocityEngine();
        ve2.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, this);
        ve2.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, sequence2);
        ve2.setProperty(RuntimeConstants.EVENTHANDLER_NULLSET, sequence2);
        ve2.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION, sequence2);
        ve2.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, sequence2);
        ve2.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);        
        ve2.init();
        
        VelocityContext context;
        StringWriter w;
        
        
        // check reference insertion with both sequences
        context = new VelocityContext();
        w = new StringWriter();
        context.put("test","abc");
        ve.evaluate( context, w, "test", "$test" );        
        if ( !w.toString().equals( "ABCABC" ))
        {
            fail( "Reference insertion test 1");
        }
        
        context = new VelocityContext();
        w = new StringWriter();
        context.put("test","abc");
        ve2.evaluate( context, w, "test", "$test" );        
        if ( !w.toString().equals( "ABCabc" ))
        {
            fail( "Reference insertion test 2");
        }
        
        // check method exception with both sequences

        // sequence 1
        context = new VelocityContext();
        w = new StringWriter();
        context.put("test",new ArrayList());
        try {
            ve.evaluate( context, w, "test", "$test.get(0)");
            fail ( "Method exception event test 1" );
        } catch( MethodInvocationException mee )
        {
            // correct if exception is raised
        }
        catch( Exception e )
        {
            fail ( "Method exception event test 1" );
        }
        
        // sequence2
        context = new VelocityContext();
        w = new StringWriter();
        context.put("test",new ArrayList());
        try {
            ve2.evaluate( context, w, "test", "$test.get(0)");
        } 
        catch( Exception e )
        {
            fail ( "Method exception event test 2" );
        }


        // check log on null set with both sequences
        // sequence 1
        context = new VelocityContext();
        w = new StringWriter();
        logString = null;
        ve.evaluate( context, w, "test", "#set($test1 = $test2)" );        
        if ( logString != null)
        {
            fail( "log null set test 1");
        }
        
        // sequence 2
        context = new VelocityContext();
        w = new StringWriter();
        logString = null;
        ve2.evaluate( context, w, "test", "#set($test1 = $test2)" );        
        if ( logString != null)
        {
            fail( "log null set test 2");
        }
        

        // check include event handler with both sequences

        // sequence 1
        Template template;
        FileOutputStream fos;
        Writer fwriter;
        
        template = ve.getTemplate( getFileName(null, "test4", TMPL_FILE_EXT) );

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, "test4", RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        context = new VelocityContext();
        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "test4", RESULT_FILE_EXT, CMP_FILE_EXT)) 
        {
            fail("Output incorrect.");
        }
    
        // sequence 2
        template = ve2.getTemplate( getFileName(null, "test5", TMPL_FILE_EXT) );

        fos = new FileOutputStream (
                getFileName(RESULTS_DIR, "test5", RESULT_FILE_EXT));

        fwriter = new BufferedWriter( new OutputStreamWriter(fos) );

        context = new VelocityContext();
        template.merge(context, fwriter);
        fwriter.flush();
        fwriter.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "test5", RESULT_FILE_EXT, CMP_FILE_EXT)) 
        {
            fail("Output incorrect.");
        }

    }
    
    
    

    /**
     *  handler for LogSystem interface
     */
    public void logVelocityMessage(int level, String message)
    {
        logString = message;
    }

}
