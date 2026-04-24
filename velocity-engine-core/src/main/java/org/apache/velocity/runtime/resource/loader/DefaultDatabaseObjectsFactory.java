package org.apache.velocity.runtime.resource.loader;

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

import org.apache.velocity.util.ExtProperties;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Database objects factory which obtains a new connection from the data source and
 * prepares a statement at each call.
 *
 * <p>The returned {@link PreparedStatement} is a proxy that remembers the connection
 * it was created from and returns it via {@link PreparedStatement#getConnection()}.
 * This keeps the {@link #releaseStatement(String, PreparedStatement)} contract
 * correct across JDBC pool implementations — in particular Tomcat JDBC Pool, whose
 * default facade returns the unwrapped driver connection and would bypass the pool
 * wrapper on close.</p>
 *
 * @deprecated Since 2.5, use {@link DefaultPreparedStatementsFactory} via the
 *             {@link PreparedStatementsFactory} SPI.
 */
@Deprecated
public class DefaultDatabaseObjectsFactory implements DatabaseObjectsFactory {

    private DataSource dataSource;

    /**
     * Initialize the factory with the DataSourceResourceLoader properties
     * @param dataSource data source
     */
    @Override
    public void init(DataSource dataSource, ExtProperties properties)
    {
        this.dataSource = dataSource;
    }

    /**
     * Prepare a statement
     * @param sql Statement SQL
     * @return prepared statement
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        Connection connection = dataSource.getConnection();
        try
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            return TrackedPreparedStatement.wrap(statement, connection);
        }
        catch (SQLException | RuntimeException e)
        {
            try { connection.close(); } catch (SQLException ignored) {}
            throw e;
        }
    }

    /**
     * Releases a prepared statement
     * @param sql original sql query
     * @param stmt statement
     */
    @Override
    public void releaseStatement(String sql, PreparedStatement stmt) throws SQLException
    {
        Connection connection = stmt.getConnection();
        try
        {
            stmt.close();
        }
        finally
        {
            connection.close();
        }
    }

    /**
     * Dynamic-proxy {@link PreparedStatement} wrapper that overrides
     * {@link PreparedStatement#getConnection()} to return the connection the
     * statement was created from, rather than whatever the underlying driver or
     * pool happens to expose. Kept alive only to make the deprecated
     * {@link DatabaseObjectsFactory} contract correct across pools.
     */
    private static final class TrackedPreparedStatement implements InvocationHandler
    {
        private final PreparedStatement delegate;
        private final Connection connection;

        private TrackedPreparedStatement(PreparedStatement delegate, Connection connection)
        {
            this.delegate = delegate;
            this.connection = connection;
        }

        static PreparedStatement wrap(PreparedStatement delegate, Connection connection)
        {
            return (PreparedStatement) Proxy.newProxyInstance(
                TrackedPreparedStatement.class.getClassLoader(),
                new Class<?>[] { PreparedStatement.class },
                new TrackedPreparedStatement(delegate, connection));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if ("getConnection".equals(method.getName()) && method.getParameterCount() == 0)
            {
                return connection;
            }
            try
            {
                return method.invoke(delegate, args);
            }
            catch (InvocationTargetException ite)
            {
                throw ite.getCause();
            }
        }
    }

    /**
     * Adapts any {@link DatabaseObjectsFactory} (including user subclasses) to the
     * new {@link PreparedStatementsFactory} SPI. Used by {@link DataSourceResourceLoader}
     * when a legacy factory is configured. Gone with the deprecated interface in the
     * next major.
     */
    static final class LegacyAdapter implements PreparedStatementsFactory
    {
        private final DatabaseObjectsFactory legacy;

        LegacyAdapter(DatabaseObjectsFactory legacy)
        {
            this.legacy = legacy;
        }

        @Override
        public void init(DataSource dataSource, ExtProperties properties) throws SQLException
        {
            legacy.init(dataSource, properties);
        }

        @Override
        public void setLogger(Logger log)
        {
            legacy.setLogger(log);
        }

        @Override
        public PreparedStatementsFactory.StatementHolder prepare(String sql) throws SQLException
        {
            PreparedStatement statement = legacy.prepareStatement(sql);
            return new PreparedStatementsFactory.StatementHolder(statement, () -> legacy.releaseStatement(sql, statement));
        }

        @Override
        public void clear()
        {
            legacy.clear();
        }
    }
}
