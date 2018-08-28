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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.hsqldb.jdbcDriver;

public class HsqlDataSource implements DataSource {

    private final String url;

    private PrintWriter logWriter = null;

    private int loginTimeout = 0;

    public HsqlDataSource(final String url) throws Exception {
	this.url = url;
	Class.forName(jdbcDriver.class.getName());
    }

    public Connection getConnection() throws SQLException {
	return DriverManager.getConnection(url, "sa", "");
    }

    public Connection getConnection(final String username, final String password)
	    throws SQLException {
	return DriverManager.getConnection(url, username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
	return logWriter;
    }

    public int getLoginTimeout() throws SQLException {
	return loginTimeout;
    }

    public void setLogWriter(final PrintWriter logWriter) throws SQLException {
	this.logWriter = logWriter;
    }

    public void setLoginTimeout(final int loginTimeout) throws SQLException {
	this.loginTimeout = loginTimeout;
    }

    public boolean isWrapperFor(final Class iface) throws SQLException {
	return false;
    }

    public Object unwrap(final Class iface) throws SQLException {
	throw new SQLException("Not implemented");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Not implemented");
    }
}
