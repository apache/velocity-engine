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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * DataSource that wraps another and tracks how many connections it currently has
 * handed out but not yet closed. Used by leak tests to assert the count returns
 * to zero after each call path.
 */
public class CountingDataSource implements DataSource
{
    private final DataSource delegate;
    private final AtomicInteger active = new AtomicInteger();

    public CountingDataSource(DataSource delegate)
    {
        this.delegate = delegate;
    }

    public int getActiveCount()
    {
        return active.get();
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return track(delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        return track(delegate.getConnection(username, password));
    }

    private Connection track(Connection real)
    {
        active.incrementAndGet();
        return (Connection) Proxy.newProxyInstance(
            CountingDataSource.class.getClassLoader(),
            new Class<?>[] { Connection.class },
            new InvocationHandler()
            {
                private boolean closed = false;

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    if ("close".equals(method.getName()) && method.getParameterCount() == 0)
                    {
                        if (!closed)
                        {
                            closed = true;
                            try { real.close(); }
                            finally { active.decrementAndGet(); }
                        }
                        return null;
                    }
                    if ("isClosed".equals(method.getName()) && method.getParameterCount() == 0)
                    {
                        return closed || (Boolean) method.invoke(real, args);
                    }
                    try { return method.invoke(real, args); }
                    catch (InvocationTargetException ite) { throw ite.getCause(); }
                }
            });
    }

    @Override public PrintWriter getLogWriter() { return null; }
    @Override public int getLoginTimeout() { return 0; }
    @Override public void setLogWriter(PrintWriter out) {}
    @Override public void setLoginTimeout(int seconds) {}
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("Not implemented"); }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
}
