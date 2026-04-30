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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.ExtProperties;

import javax.sql.DataSource;

/**
 * Verifies the factory-resolution precedence in {@link DataSourceResourceLoader#init}:
 * prepared_statements_factory.instance {@literal >} prepared_statements_factory.class
 * {@literal >} deprecated database_objects_factory.instance
 * {@literal >} deprecated database_objects_factory.class {@literal >} default.
 */
public class DataSourceResourceLoaderFactoryConfigTestCase extends BaseSQLTest
{
    private static final String DATA_PATH = TEST_COMPARE_DIR + "/ds";

    public DataSourceResourceLoaderFactoryConfigTestCase(String name) throws Exception
    {
        super(name, DATA_PATH);
    }

    public static Test suite()
    {
        return new TestSuite(DataSourceResourceLoaderFactoryConfigTestCase.class);
    }

    @Override
    public void setUp()
    {
        SentinelPreparedStatementsFactory.reset();
        SentinelDatabaseObjectsFactory.reset();
    }

    public void testNewInstanceIsUsed() throws Exception
    {
        ExtProperties props = baseProps();
        props.setProperty("resource.loader.ds.prepared_statements_factory.instance",
            new SentinelPreparedStatementsFactory());
        loadTemplate(props);
        assertTrue("new-SPI sentinel should have been called",
            SentinelPreparedStatementsFactory.calls.get() > 0);
    }

    public void testNewClassIsUsed() throws Exception
    {
        ExtProperties props = baseProps();
        props.setProperty("resource.loader.ds.prepared_statements_factory.class",
            SentinelPreparedStatementsFactory.class.getName());
        loadTemplate(props);
        assertTrue("new-SPI sentinel should have been called",
            SentinelPreparedStatementsFactory.calls.get() > 0);
    }

    public void testLegacyInstanceIsUsed() throws Exception
    {
        ExtProperties props = baseProps();
        props.setProperty("resource.loader.ds.database_objects_factory.instance",
            new SentinelDatabaseObjectsFactory());
        loadTemplate(props);
        assertTrue("legacy-SPI sentinel should have been called",
            SentinelDatabaseObjectsFactory.calls.get() > 0);
    }

    public void testLegacyClassIsUsed() throws Exception
    {
        ExtProperties props = baseProps();
        props.setProperty("resource.loader.ds.database_objects_factory.class",
            SentinelDatabaseObjectsFactory.class.getName());
        loadTemplate(props);
        assertTrue("legacy-SPI sentinel should have been called",
            SentinelDatabaseObjectsFactory.calls.get() > 0);
    }

    public void testDefaultIsUsedWhenNothingConfigured() throws Exception
    {
        loadTemplate(baseProps());
        assertEquals("no sentinel should be used", 0, SentinelPreparedStatementsFactory.calls.get());
        assertEquals("no sentinel should be used", 0, SentinelDatabaseObjectsFactory.calls.get());
    }

    public void testNewClassTakesPrecedenceOverLegacyClass() throws Exception
    {
        ExtProperties props = baseProps();
        props.setProperty("resource.loader.ds.prepared_statements_factory.class",
            SentinelPreparedStatementsFactory.class.getName());
        props.setProperty("resource.loader.ds.database_objects_factory.class",
            SentinelDatabaseObjectsFactory.class.getName());
        loadTemplate(props);
        assertTrue("new-SPI sentinel should be used", SentinelPreparedStatementsFactory.calls.get() > 0);
        assertEquals("legacy-SPI sentinel should NOT be used", 0, SentinelDatabaseObjectsFactory.calls.get());
    }

    private ExtProperties baseProps() throws Exception
    {
        DataSource ds = new TestDataSource(TEST_JDBC_DRIVER_CLASS, TEST_JDBC_URI, TEST_JDBC_LOGIN, TEST_JDBC_PASSWORD);
        DataSourceResourceLoader rl = new DataSourceResourceLoader();
        rl.setDataSource(ds);

        ExtProperties props = new ExtProperties();
        props.addProperty("resource.loader", "ds");
        props.setProperty("resource.loader.ds.instance", rl);
        props.setProperty("resource.loader.ds.resource.table", "velocity_template_varchar");
        props.setProperty("resource.loader.ds.resource.key_column", "vt_id");
        props.setProperty("resource.loader.ds.resource.template_column", "vt_def");
        props.setProperty("resource.loader.ds.resource.timestamp_column", "vt_timestamp");
        props.setProperty("resource.loader.ds.cache", "false");
        props.setProperty(Velocity.RUNTIME_LOG_INSTANCE, new TestLogger(false, false));
        return props;
    }

    private void loadTemplate(ExtProperties props)
    {
        RuntimeInstance engine = new RuntimeInstance();
        engine.setConfiguration(props);
        engine.init();
        engine.getTemplate("testTemplate1");
    }
}
