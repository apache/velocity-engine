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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetProxy implements InvocationHandler {
    private final ResultSet wrappedResultSet;
    private final PreparedStatement wrappedPreparedStatement;

    public ResultSetProxy(ResultSet wrappedResultSet, PreparedStatement wrappedPreparedStatement) {
        this.wrappedResultSet = wrappedResultSet;
        this.wrappedPreparedStatement = wrappedPreparedStatement;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if (method.getName().equals("getStatement")) {
            result = wrappedPreparedStatement;
        } else {
            result = method.invoke(wrappedResultSet, args);
        }
        return result;
    }

    public static ResultSet newInstance(ResultSet rs, PreparedStatement ps) throws SQLException {
        return (ResultSet) Proxy.newProxyInstance(rs.getClass().getClassLoader(),
                rs.getClass().getInterfaces(),
                new ResultSetProxy(rs, ps));
    }

}
