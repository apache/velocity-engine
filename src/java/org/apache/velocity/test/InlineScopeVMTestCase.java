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

import java.util.Properties;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.VelocimacroFactory;

import junit.framework.TestCase;

/**
 * Tests if the VM template-locality is working.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: InlineScopeVMTestCase.java,v 1.11.10.1 2004/03/03 23:23:04 geirm Exp $
 */
public class InlineScopeVMTestCase extends BaseTestCase implements TemplateTestBase
{
    /**
     * The name of this test case.
     */
    private static final String TEST_CASE_NAME = "InlineScopeVMTestCase";

    InlineScopeVMTestCase()
    {
        super(TEST_CASE_NAME);

        try
        {
            /*
             *  do our properties locally, and just override the ones we want
             *  changed
             */

            Velocity.setProperty( 
                Velocity.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, "true");
            
            Velocity.setProperty( 
                Velocity.VM_PERM_INLINE_LOCAL, "true");

            Velocity.setProperty( 
                Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
            
            Velocity.init();    
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup " + TEST_CASE_NAME);
            System.exit(1);
        } 
    }

    public static junit.framework.Test suite ()
    {
        return new InlineScopeVMTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            assureResultsDirectoryExists(RESULT_DIR);
            
            /*
             * Get the template and the output. Do them backwards. 
             * vm_test2 uses a local VM and vm_test1 doesn't
             */

            Template template2 = RuntimeSingleton.getTemplate(
                getFileName(null, "vm_test2", TMPL_FILE_EXT));
            
            Template template1 = RuntimeSingleton.getTemplate(
                getFileName(null, "vm_test1", TMPL_FILE_EXT));
           
            FileOutputStream fos1 = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "vm_test1", RESULT_FILE_EXT));

            FileOutputStream fos2 = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "vm_test2", RESULT_FILE_EXT));

            Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
            Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));
            
            /*
             *  put the Vector into the context, and merge both
             */

            VelocityContext context = new VelocityContext();

            template1.merge(context, writer1);
            writer1.flush();
            writer1.close();
            
            template2.merge(context, writer2);
            writer2.flush();
            writer2.close();

            if (!isMatch(RESULT_DIR,COMPARE_DIR,"vm_test1",
                    RESULT_FILE_EXT,CMP_FILE_EXT) ||
                !isMatch(RESULT_DIR,COMPARE_DIR,"vm_test2",
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
