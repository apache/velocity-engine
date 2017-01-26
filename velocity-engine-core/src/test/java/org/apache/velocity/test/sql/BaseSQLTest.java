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
 * Velocity engine and an initialized Hsql Database. Yeah, I should probably
 * use Derby at some point...
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public abstract class BaseSQLTest
        extends BaseTestCase
{
    private static HsqlDB hsqlDB = null;

    public BaseSQLTest(String name, String path)
            throws Exception
    {
        super(name);

        if (hsqlDB == null)
        {
            hsqlDB = new HsqlDB("jdbc:hsqldb:.", path + "/create-db.sql");
        }
    }

    public void executeSQL(String sql)
    throws SQLException
    {
        Connection connection = hsqlDB.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }
}
