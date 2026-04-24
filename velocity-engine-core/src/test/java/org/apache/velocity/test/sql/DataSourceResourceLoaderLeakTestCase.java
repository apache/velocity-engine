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
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.ExtProperties;

import java.io.StringWriter;

/**
 * Asserts that DataSourceResourceLoader never leaks connections — the counting
 * DataSource's active count must return to zero after every call path, including
 * the failure paths that previously leaked (template-not-found, SQL failures).
 */
public class DataSourceResourceLoaderLeakTestCase extends BaseSQLTest
{
    private static final String DATA_PATH = TEST_COMPARE_DIR + "/ds";

    private CountingDataSource ds;
    private RuntimeInstance engine;

    public DataSourceResourceLoaderLeakTestCase(String name) throws Exception
    {
        super(name, DATA_PATH);
    }

    public static Test suite()
    {
        return new TestSuite(DataSourceResourceLoaderLeakTestCase.class);
    }

    @Override
    public void setUp() throws Exception
    {
        ds = new CountingDataSource(
            new TestDataSource(TEST_JDBC_DRIVER_CLASS, TEST_JDBC_URI, TEST_JDBC_LOGIN, TEST_JDBC_PASSWORD));

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

        engine = new RuntimeInstance();
        engine.setConfiguration(props);
        engine.init();
    }

    public void testSuccessfulLoadDoesNotLeak() throws Exception
    {
        Template t = engine.getTemplate("testTemplate1");
        StringWriter w = new StringWriter();
        t.merge(new VelocityContext(), w);
        w.close();
        assertEquals("active connections after successful merge", 0, ds.getActiveCount());
    }

    public void testMissingTemplateDoesNotLeak()
    {
        try
        {
            engine.getTemplate("does-not-exist");
            fail("expected ResourceNotFoundException");
        }
        catch (ResourceNotFoundException expected)
        {
        }
        assertEquals("active connections after template-not-found", 0, ds.getActiveCount());
    }

    public void testIsSourceModifiedDoesNotLeak() throws Exception
    {
        Template t = engine.getTemplate("testTemplate1");
        int baseline = ds.getActiveCount();
        assertFalse("should still be current", t.getResourceLoader().isSourceModified(t));
        assertEquals("active connections after isSourceModified", baseline, ds.getActiveCount());
    }
}
