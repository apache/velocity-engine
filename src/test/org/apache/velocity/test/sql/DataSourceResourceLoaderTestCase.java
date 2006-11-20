package org.apache.velocity.test.sql;

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
import java.io.Writer;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader;


public class DataSourceResourceLoaderTestCase
        extends BaseSQLTest
{
    /**
     * Comparison file extension.
     */
    private static final String CMP_FILE_EXT = "cmp";

    /**
     * Comparison file extension.
     */
    private static final String RESULT_FILE_EXT = "res";

    /**
     * Path to template file.  This will get combined with the
     * application directory to form an absolute path
     */
    private final static String DATA_PATH = TEST_COMPARE_DIR + "/ds";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/ds";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/ds/templates";


    public DataSourceResourceLoaderTestCase(final String name)
    	throws Exception
    {
        super(name, DATA_PATH);
    }

    public static Test suite()
    {
        return new TestSuite(DataSourceResourceLoaderTestCase.class);
    }

    public void setUp()
            throws Exception
    {

        assureResultsDirectoryExists(RESULTS_DIR);

	DataSource ds = new HsqlDataSource("jdbc:hsqldb:.");

        DataSourceResourceLoader rl = new DataSourceResourceLoader();
        rl.setDataSource(ds);

        // pass in an instance to Velocity
        Velocity.addProperty( "resource.loader", "ds" );
        Velocity.setProperty( "ds.resource.loader.instance", rl );

        Velocity.setProperty( "ds.resource.loader.resource.table",           "velocity_template");
        Velocity.setProperty( "ds.resource.loader.resource.keycolumn",       "id");
        Velocity.setProperty( "ds.resource.loader.resource.templatecolumn",  "def");
        Velocity.setProperty( "ds.resource.loader.resource.timestampcolumn", "timestamp");

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());

        Velocity.init();
    }

    /**
     * Tests loading and rendering of a simple template. If that works, we are able to get data
     * from the database.
     */
    public void testSimpleTemplate()
            throws Exception
    {
        Template t = executeTest("testTemplate1");
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
    }

    /**
     * Now we have a more complex example. Run a very simple tool.
     * from the database.
     */
    public void testRenderTool()
            throws Exception
    {
	Template t = executeTest("testTemplate2");
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
    }

    /**
     * Will a NULL timestamp choke the loader?
     */
    public void testNullTimestamp()
            throws Exception
    {
        Template t = executeTest("testTemplate3");
        assertEquals("Timestamp is not 0", 0, t.getLastModified());
    }

    /**
     * Does it load the global Macros from the DB?
     */
    public void testMacroInvocation()
            throws Exception
    {
        Template t = executeTest("testTemplate4");
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
    }

    protected Template executeTest(final String templateName)
    	throws Exception
    {
        Template template = RuntimeSingleton.getTemplate(templateName);

        FileOutputStream fos =
                new FileOutputStream (
                        getFileName(RESULTS_DIR, templateName, RESULT_FILE_EXT));

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        VelocityContext context = new VelocityContext();
        context.put("tool", new DSRLTCTool());

        template.merge(context, writer);
        writer.flush();
        writer.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, templateName,
                        RESULT_FILE_EXT, CMP_FILE_EXT))
        {
            fail("Output incorrect for Template: " + templateName);
        }

        return template;
    }

    public static final class DSRLTCTool
    {
	public int add(final int a, final int b)
	{
	    return a + b;
	}

	public String getMessage()
	{
	    return "And the result is:";
	}
    }
}
