package org.apache.velocity.test;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;

import junit.framework.TestCase;

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
 * @version $Id: ContextSafetyTestCase.java,v 1.9 2001/08/07 22:20:28 geirm Exp $
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
