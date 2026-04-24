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
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Factory for creating connections and prepared statements.
 *
 * @deprecated Since 2.5, use {@link PreparedStatementsFactory} instead. This
 *             interface relies on recovering the connection from the statement
 *             via {@link PreparedStatement#getConnection()}, which is not
 *             portable across JDBC pool implementations. See
 *             {@link DataSourceResourceLoader} for the supported configuration.
 */
@Deprecated
public interface DatabaseObjectsFactory {

    /**
     * Initialize the factory with the DataSourceResourceLoader properties
     * @param dataSource data source
     */
    void init(DataSource dataSource, ExtProperties properties) throws SQLException;

    /**
     * Set the logger to be used by the factory
     * @param log
     */
    default void setLogger(Logger log) {}

    /**
     * Prepare a statement
     * @param sql Statement SQL
     * @return prepared statement
     */
    PreparedStatement prepareStatement(String sql) throws SQLException;

    /**
     * Releases a prepared statement
     * @param sql original sql query
     * @param stmt statement
     */
    void releaseStatement(String sql, PreparedStatement stmt) throws SQLException;

    /**
     * Free resources
     */
    default void clear() {};
}
