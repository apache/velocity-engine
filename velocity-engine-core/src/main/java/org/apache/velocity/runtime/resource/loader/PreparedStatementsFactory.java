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
 * SPI for {@link DataSourceResourceLoader}, replacing the deprecated
 * {@link DatabaseObjectsFactory}. An implementation decides how statements are
 * prepared and what {@link StatementHolder#close()} does.
 *
 * @since 2.5
 */
public interface PreparedStatementsFactory
{
    void init(DataSource dataSource, ExtProperties properties) throws SQLException;

    default void setLogger(Logger log) {}

    StatementHolder prepare(String sql) throws SQLException;

    /** Equivalent to {@link StatementHolder#close()}; kept for callers that prefer an explicit idiom. */
    default void release(StatementHolder holder) throws SQLException
    {
        holder.close();
    }

    default void clear() {}

    /**
     * A prepared statement bundled with its release action. Implementations of
     * {@link PreparedStatementsFactory#prepare(String)} supply the action via any
     * {@link AutoCloseable}: close the connection, return to a cache, etc.
     */
    final class StatementHolder implements AutoCloseable
    {
        private final PreparedStatement statement;
        private final AutoCloseable onClose;

        public StatementHolder(PreparedStatement statement, AutoCloseable onClose)
        {
            this.statement = statement;
            this.onClose = onClose;
        }

        public PreparedStatement getStatement()
        {
            return statement;
        }

        @Override
        public void close() throws SQLException
        {
            try
            {
                onClose.close();
            }
            catch (SQLException | RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new SQLException("release action failed", e);
            }
        }
    }
}
