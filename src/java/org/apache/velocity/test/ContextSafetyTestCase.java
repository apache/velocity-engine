package org.apache.velocity.test;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.Writer;

import java.util.Vector;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;

/**
 * Tests if we are context safe : can we switch objects in the context
 * and re-merge the template safely.
 *
 * NOTE:
 * This class should not extend RuntimeTestCase because this test
 * is run from the VelocityTestSuite which in effect a runtime
 * test suite and the test suite initializes the Runtime. Extending
 * RuntimeTestCase causes the Runtime to be initialized twice.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ContextSafetyTestCase.java,v 1.10.4.1 2004/03/03 23:23:04 geirm Exp $
 */
public class ContextSafetyTestCase extends BaseTestCase implements TemplateTestBase
{
    public ContextSafetyTestCase()
    {
        super("ContextSafetyTestCase");
        
        try
        {
	        Velocity.setProperty(
	            Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
	        
             Velocity.init();
	    }
	    catch (Exception e)
	    {
            System.err.println("Cannot setup ContextSafetyTestCase!");
            e.printStackTrace();
            System.exit(1);
	    }
    }

    public static junit.framework.Test suite()
    {
        return new ContextSafetyTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        /*
         *  make a Vector and String array because
         *  they are treated differently in Foreach()
         */
        Vector v = new Vector();
        
        v.addElement( new String("vector hello 1") );
        v.addElement( new String("vector hello 2") );
        v.addElement( new String("vector hello 3") );
        
        String strArray[] = new String[3];
        
        strArray[0] = "array hello 1";
        strArray[1] = "array hello 2";
        strArray[2] = "array hello 3";
       
        VelocityContext context = new VelocityContext();
       
        try
        {
            assureResultsDirectoryExists(RESULT_DIR);
            
            /*
             *  get the template and the output
             */

            Template template = RuntimeSingleton.getTemplate(
                getFileName(null, "context_safety", TMPL_FILE_EXT));

            FileOutputStream fos1 = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "context_safety1", RESULT_FILE_EXT));

            FileOutputStream fos2 = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "context_safety2", RESULT_FILE_EXT));

            Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
            Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));
            
            /*
             *  put the Vector into the context, and merge
             */

            context.put("vector", v);  
            template.merge(context, writer1);
            writer1.flush();
            writer1.close();
            
            /*
             *  now put the string array into the context, and merge
             */

            context.put("vector", strArray);  
            template.merge(context, writer2);
            writer2.flush();
            writer2.close();

            if (!isMatch(RESULT_DIR,COMPARE_DIR,"context_safety1",
                    RESULT_FILE_EXT,CMP_FILE_EXT) ||
                !isMatch(RESULT_DIR,COMPARE_DIR,"context_safety2",
                    RESULT_FILE_EXT,CMP_FILE_EXT))
            {
                fail("Output incorrect.");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
