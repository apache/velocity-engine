package org.apache.velocity.test.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

    public void setLogWriter(PrintWriter logWriter) throws SQLException {
	this.logWriter = logWriter;
    }

    public void setLoginTimeout(int loginTimeout) throws SQLException {
	this.loginTimeout = loginTimeout;
    }

}
