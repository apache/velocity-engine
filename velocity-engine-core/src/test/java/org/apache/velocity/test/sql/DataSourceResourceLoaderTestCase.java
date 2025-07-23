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

import com.zaxxer.hikari.HikariDataSource;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.ExtProperties;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;


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

    /* engine with VARCHAR templates data source */
    private RuntimeInstance varcharTemplatesEngine = null;

    /* engine with VARCHAR templates data source for testing connection counts with DBCP2 data source*/
    private RuntimeInstance varcharTemplatesDBCP2ConnectionCountTestEngine = null;
    private BasicDataSource dbcp2ConnectionCountDataSource = null;

    /* engine with VARCHAR templates data source for testing connection counts with Tomcat JDBC data source*/
    private RuntimeInstance varcharTemplatesTomcatJDBCConnectionCountTestEngine = null;
    private org.apache.tomcat.jdbc.pool.DataSource tomcatJDBCConnectionCountDataSource = null;

    /* engine with VARCHAR templates data source for testing connection counts with Tomcat JDBC data source*/
    private RuntimeInstance varcharTemplatesHikariConnectionCountTestEngine = null;
    private HikariDataSource hikariConnectionCountDataSource = null;

    /* engine with CLOB templates data source */
    private RuntimeInstance clobTemplatesEngine = null;

    public DataSourceResourceLoaderTestCase(final String name)
    	throws Exception
    {
        super(name, DATA_PATH);
    }

    public static Test suite()
    {
        return new TestSuite(DataSourceResourceLoaderTestCase.class);
    }

    @Override
    public void setUp()
            throws Exception
    {

        assureResultsDirectoryExists(RESULTS_DIR);

        DataSource ds1 = new TestDataSource(TEST_JDBC_DRIVER_CLASS, TEST_JDBC_URI, TEST_JDBC_LOGIN, TEST_JDBC_PASSWORD);
        DataSourceResourceLoader rl1 = new DataSourceResourceLoader();
        rl1.setDataSource(ds1);
        ExtProperties props = getResourceLoaderProperties();
        props.setProperty( "ds.resource.loader.instance", rl1);
        props.setProperty( "ds.resource.loader.resource.table", "velocity_template_varchar");
        varcharTemplatesEngine = new RuntimeInstance();
        varcharTemplatesEngine.setConfiguration(props);
        varcharTemplatesEngine.init();

        DataSource ds2 = new TestDataSource(TEST_JDBC_DRIVER_CLASS, TEST_JDBC_URI, TEST_JDBC_LOGIN, TEST_JDBC_PASSWORD);
        DataSourceResourceLoader rl2 = new DataSourceResourceLoader();
        rl2.setDataSource(ds2);
        ExtProperties props2 = (ExtProperties)props.clone();
        props2.setProperty( "ds.resource.loader.instance", rl2);
        props2.setProperty( "ds.resource.loader.resource.table",  "velocity_template_clob");
        clobTemplatesEngine = new RuntimeInstance();
        clobTemplatesEngine.setConfiguration(props2);
        clobTemplatesEngine.init();

        BasicDataSource ds3 = new BasicDataSource();
        ds3.setDriverClassName(TEST_JDBC_DRIVER_CLASS);
        ds3.setUrl(TEST_JDBC_URI);
        ds3.setUsername(TEST_JDBC_LOGIN);
        ds3.setPassword(TEST_JDBC_PASSWORD);
        ds3.setMaxTotal(10);
        DataSourceResourceLoader rl3 = new DataSourceResourceLoader();
        rl3.setDataSource(ds3);
        ExtProperties props3 = getResourceLoaderProperties();
        props3.setProperty( "ds.resource.loader.instance", rl3);
        props3.setProperty( "ds.resource.loader.resource.table", "velocity_template_varchar");
        varcharTemplatesDBCP2ConnectionCountTestEngine = new RuntimeInstance();
        varcharTemplatesDBCP2ConnectionCountTestEngine.setConfiguration(props3);
        varcharTemplatesDBCP2ConnectionCountTestEngine.init();
        dbcp2ConnectionCountDataSource = ds3;

        org.apache.tomcat.jdbc.pool.DataSource ds4 = new org.apache.tomcat.jdbc.pool.DataSource();
        ds4.setDriverClassName(TEST_JDBC_DRIVER_CLASS);
        ds4.setUrl(TEST_JDBC_URI);
        ds4.setUsername(TEST_JDBC_LOGIN);
        ds4.setPassword(TEST_JDBC_PASSWORD);
        ds4.setMaxActive(10);
        DataSourceResourceLoader rl4 = new DataSourceResourceLoader();
        rl4.setDataSource(ds4);
        ExtProperties props4 = getResourceLoaderProperties();
        props4.setProperty( "ds.resource.loader.instance", rl4);
        props4.setProperty( "ds.resource.loader.resource.table", "velocity_template_varchar");
        varcharTemplatesTomcatJDBCConnectionCountTestEngine = new RuntimeInstance();
        varcharTemplatesTomcatJDBCConnectionCountTestEngine.setConfiguration(props4);
        varcharTemplatesTomcatJDBCConnectionCountTestEngine.init();
        tomcatJDBCConnectionCountDataSource = ds4;

        HikariDataSource ds5 = new HikariDataSource();
        ds5.setDriverClassName(TEST_JDBC_DRIVER_CLASS);
        ds5.setJdbcUrl(TEST_JDBC_URI);
        ds5.setUsername(TEST_JDBC_LOGIN);
        ds5.setPassword(TEST_JDBC_PASSWORD);
        ds5.setMaximumPoolSize(10);
        DataSourceResourceLoader rl5 = new DataSourceResourceLoader();
        rl5.setDataSource(ds5);
        ExtProperties props5 = getResourceLoaderProperties();
        props5.setProperty( "ds.resource.loader.instance", rl5);
        props5.setProperty( "ds.resource.loader.resource.table", "velocity_template_varchar");
        varcharTemplatesHikariConnectionCountTestEngine = new RuntimeInstance();
        varcharTemplatesHikariConnectionCountTestEngine.setConfiguration(props5);
        varcharTemplatesHikariConnectionCountTestEngine.init();
        hikariConnectionCountDataSource = ds5;

    }

    protected ExtProperties getResourceLoaderProperties()
    {
        ExtProperties props = new ExtProperties();
        props.addProperty( "resource.loader", "ds" );
        props.setProperty( "ds.resource.loader.resource.keycolumn",       "vt_id");
        props.setProperty( "ds.resource.loader.resource.templatecolumn",  "vt_def");
        props.setProperty( "ds.resource.loader.resource.timestampcolumn", "vt_timestamp");
        props.setProperty(Velocity.RUNTIME_LOG_INSTANCE, new TestLogger(false, false));
        return props;
    }

    /**
     * Tests loading and rendering of a simple template. If that works, we are able to get data
     * from the database.
     */
    
    public void testSimpleTemplate()
            throws Exception
    {
        Template t = executeTest("testTemplate1", varcharTemplatesEngine);
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
        t = executeTest("testTemplate1", clobTemplatesEngine);
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
    }
    /**
     * Tests loading and rendering of a simple template and checks that there are no connection leaks. Uses DBCP2 Data data source
     */
    public void testDBCP2DataSourceForConnectionLeaks()
            throws Exception
    {
        executeTest("testTemplate1", varcharTemplatesDBCP2ConnectionCountTestEngine);
        try {
            varcharTemplatesDBCP2ConnectionCountTestEngine.getTemplate("fakeTemplate");
            fail("Should have thrown exception ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            //continue
        }
        assertEquals("Open connection count is greater then 0", 0, this.dbcp2ConnectionCountDataSource.getConnectionPool().getNumActive());
    }

    /**
     * Tests loading and rendering of a simple template and checks that there are no connection leaks. Uses Tomcat JDBC data source
     */
    public void testTomcatJDBCDataSourceForConnectionLeaks()
            throws Exception
    {
        executeTest("testTemplate1", varcharTemplatesTomcatJDBCConnectionCountTestEngine);
        try {
            varcharTemplatesTomcatJDBCConnectionCountTestEngine.getTemplate("fakeTemplate");
            fail("Should have thrown exception ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            //continue
        }
        assertEquals("Open connection count is greater then 0", 0, this.tomcatJDBCConnectionCountDataSource.getActive());
    }

    /**
     * Tests loading and rendering of a simple template and checks that there are no connection leaks. Uses Tomcat JDBC data source
     */
    public void testHikariCPDataSourceForConnectionLeaks()
            throws Exception
    {
        executeTest("testTemplate1", varcharTemplatesHikariConnectionCountTestEngine);
        try {
            varcharTemplatesHikariConnectionCountTestEngine.getTemplate("fakeTemplate");
            fail("Should have thrown exception ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            //continue
        }
        assertEquals("Open connection count is greater then 0", 0, this.hikariConnectionCountDataSource.getHikariPoolMXBean().getActiveConnections());
    }

    public void testUnicode(RuntimeInstance engine)
        throws Exception
    {
        Template template = engine.getTemplate(UNICODE_TEMPLATE_NAME);

        Writer writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        template.merge(context, writer);
        writer.flush();
        writer.close();

        String outputText = writer.toString();

        if (!normalizeNewlines(UNICODE_TEMPLATE).equals(
                normalizeNewlines( outputText ) ))
        {
            fail("Output incorrect for Template: " + UNICODE_TEMPLATE_NAME);
        }
    }

    /**
     * Now we have a more complex example. Run a very simple tool.
     * from the database.
     */

    public void testRenderTool()
            throws Exception
    {
        Template t = executeTest("testTemplate2", varcharTemplatesEngine);
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
        t = executeTest("testTemplate2", clobTemplatesEngine);
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
    }

    /**
     * Will a NULL timestamp choke the loader?
     */

    public void testNullTimestamp()
            throws Exception
    {
        Template t = executeTest("testTemplate3", varcharTemplatesEngine);
        assertEquals("Timestamp is not 0", 0, t.getLastModified());
        t = executeTest("testTemplate3", clobTemplatesEngine);
        assertEquals("Timestamp is not 0", 0, t.getLastModified());
    }

    /**
     * Does it load the global Macros from the DB?
     */

    public void testMacroInvocation()
            throws Exception
    {
        Template t = executeTest("testTemplate4", varcharTemplatesEngine);
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
        t = executeTest("testTemplate4", clobTemplatesEngine);
        assertFalse("Timestamp is 0", 0 == t.getLastModified());
    }

    protected Template executeTest(final String templateName, RuntimeInstance engine)
    	throws Exception
    {
        Template template = engine.getTemplate(templateName);

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
