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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Stock {@link PreparedStatementsFactory}: one fresh connection per {@link #prepare(String)},
 * closed with the holder. Pooling and statement caching, if needed, belong to the configured
 * {@link DataSource} / JDBC driver.
 *
 * @since 2.5
 */
public class DefaultPreparedStatementsFactory implements PreparedStatementsFactory
{
    private DataSource dataSource;

    @Override
    public void init(DataSource dataSource, ExtProperties properties)
    {
        this.dataSource = dataSource;
    }

    @Override
    public PreparedStatementsFactory.StatementHolder prepare(String sql) throws SQLException
    {
        Connection connection = dataSource.getConnection();
        try
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            return new PreparedStatementsFactory.StatementHolder(statement, () ->
            {
                try { statement.close(); }
                finally { connection.close(); }
            });
        }
        catch (SQLException | RuntimeException e)
        {
            try { connection.close(); } catch (SQLException ignored) {}
            throw e;
        }
    }
}
