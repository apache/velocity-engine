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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Proxy for {@link java.sql.PreparedStatement} that guarantees getConnection will always return the DataSource
 * provided wrapped {@link java.sql.Connection} and not the underlying database specific connection.
 * Also overrides executeQuery to return a proxy for {@link java.sql.ResultSet} see {@link ResultSetProxy}
 */
public class PreparedStatementProxy implements InvocationHandler {
    private final PreparedStatement wrappedPreparedStatement;
    private PreparedStatement proxyPreparedStatement;
    private final Connection wrappedConnection;

    public PreparedStatementProxy(PreparedStatement wrappedPreparedStatement, Connection wrappedConnection) {
        this.wrappedPreparedStatement = wrappedPreparedStatement;
        this.wrappedConnection = wrappedConnection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if (method.getName().equals("getConnection")) {
            result = wrappedConnection;
        } else if (method.getName().equals("executeQuery")) {
            return ResultSetProxy.newInstance((ResultSet) method.invoke(wrappedPreparedStatement, args), proxyPreparedStatement);
        } else {
            result = method.invoke(wrappedPreparedStatement, args);
        }
        return result;
    }

    public static PreparedStatement newInstance(PreparedStatement ps, Connection con) throws SQLException {
        PreparedStatementProxy invocationHandler = new PreparedStatementProxy(ps, con);
        PreparedStatement proxyPreparedStatement = (PreparedStatement) Proxy.newProxyInstance(ps.getClass().getClassLoader(),
                ps.getClass().getInterfaces(),
                invocationHandler);
        invocationHandler.setProxyPreparedStatement(proxyPreparedStatement);
        return proxyPreparedStatement;
    }

    public void setProxyPreparedStatement(PreparedStatement proxyPreparedStatement) {
        this.proxyPreparedStatement = proxyPreparedStatement;
    }
}
