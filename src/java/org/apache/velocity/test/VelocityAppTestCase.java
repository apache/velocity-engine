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
import java.io.StringWriter;

import java.util.Vector;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.app.Velocity;

import junit.framework.TestCase;

/**
 * This class is intended to test the app.Velocity.java class.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: VelocityAppTestCase.java,v 1.3.14.1 2004/03/03 23:23:04 geirm Exp $
 */
public class VelocityAppTestCase extends BaseTestCase implements TemplateTestBase
{
    private StringWriter compare1 = new StringWriter();
    private String input1 = "My name is $name -> $Floog";
    private String result1 = "My name is jason -> floogie woogie";

    public VelocityAppTestCase()
    {
        super("VelocityAppTestCase");

        try
        {
            Velocity.setProperty(
	           Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
	        
            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup VelocityAppTestCase!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static junit.framework.Test suite()
    {
        return new VelocityAppTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        VelocityContext context = new VelocityContext();
        context.put("name", "jason");
        context.put("Floog", "floogie woogie");

        try
        {
            Velocity.evaluate(context, compare1, "evaltest", input1);

/*
            FIXME: Not tested right now.
            
            StringWriter result2 = new StringWriter();
            Velocity.mergeTemplate("mergethis.vm",  context, result2);

            StringWriter result3 = new StringWriter();
            Velocity.invokeVelocimacro("floog", "test", new String[2], 
                                        context, result3);
*/
            if (!result1.equals(compare1.toString()))
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
