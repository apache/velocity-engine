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

import org.apache.velocity.test.BaseTestCase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A base class to implement tests that need a running
 * Velocity engine and an initialized HSQLDB Database.
 * It can also be used to test against other database engines
 * by means of the proper environment parameters, see velocity-engine-core pom.xml file.
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public abstract class BaseSQLTest
        extends BaseTestCase
{
    private static DBHelper dbHelper = null;

    protected String TEST_JDBC_DRIVER_CLASS = System.getProperty("test.jdbc.driver.className");
    protected String TEST_JDBC_URI = System.getProperty("test.jdbc.uri");
    protected String TEST_JDBC_LOGIN = System.getProperty("test.jdbc.login");
    protected String TEST_JDBC_PASSWORD = System.getProperty("test.jdbc.password");

    /**
     * String (not containing any VTL) used to test unicode
     */
    protected String UNICODE_TEMPLATE = "\\u00a9 test \\u0410 \\u0411";

    /**
     * Name of template for testing unicode.
     */
    protected String UNICODE_TEMPLATE_NAME = "testUnicode";


    public BaseSQLTest(String name, String path)
            throws Exception
    {
        super(name);

        if (dbHelper == null)
        {
            dbHelper = new DBHelper(TEST_JDBC_DRIVER_CLASS, TEST_JDBC_URI, TEST_JDBC_LOGIN, TEST_JDBC_PASSWORD,path + "/create-db.sql");
            setUpUnicode();
        }
    }

    private void setUpUnicode()
        throws Exception
    {
        String insertString = "insert into velocity_template_varchar (vt_id, vt_timestamp, vt_def) VALUES " +
            "( '" + UNICODE_TEMPLATE_NAME + "', current_timestamp, '" + UNICODE_TEMPLATE + "');";
        executeSQL(insertString);
        insertString = "insert into velocity_template_clob (vt_id, vt_timestamp, vt_def) VALUES " +
            "( '" + UNICODE_TEMPLATE_NAME + "', current_timestamp, '" + UNICODE_TEMPLATE + "');";
        executeSQL(insertString);
    }


    public void executeSQL(String sql)
            throws SQLException
    {
        Connection connection = dbHelper.getConnection();
        Statement statement = connection.createStatement();
        // Oracle and Derby do not want any final ';'
        if ((TEST_JDBC_DRIVER_CLASS.equals("oracle.jdbc.OracleDriver")
            || TEST_JDBC_DRIVER_CLASS.equals("org.apache.derby.jdbc.EmbeddedDriver")) && sql.endsWith(";"))
        {
            sql = sql.substring(0, sql.length() - 1);
        }
        statement.executeUpdate(sql);
    }
}
