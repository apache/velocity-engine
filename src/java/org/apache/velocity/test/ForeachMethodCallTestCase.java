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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.test.provider.ForeachMethodCallHelper;

/**
 * This class tests proper method execution during a Foreach loop with items
 * of varying class.
 *
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id: VelocimacroTestCase.java 75959 2004-03-19 17:13:40Z dlr $
 */
public class ForeachMethodCallTestCase extends TestCase 
{
    private String template1 = "#foreach ( $item in $col )$helper.getFoo($item) #end";
    private String result1 = "int 100 str STRVALUE ";
    
    public ForeachMethodCallTestCase()
    {
        super("ForeachMethodCallTestCase");

        try
        {
            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup ForeachMethodCallTestCase!");
            System.exit(1);
        }
    }

    public static junit.framework.Test suite()
    {
        return new ForeachMethodCallTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        VelocityContext context = new VelocityContext();
        List col = new ArrayList();
        col.add(new Integer(100));
        col.add("STRVALUE");
        context.put("helper", new ForeachMethodCallHelper());
        context.put("col", col);

        try
        {
            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, "test", template1);

            String out = writer.toString();

            if( !result1.equals( out ) )
            {
                fail("output incorrect.");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
