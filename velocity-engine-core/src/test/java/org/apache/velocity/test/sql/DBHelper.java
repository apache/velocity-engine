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

import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DBHelper
{
    private Connection connection = null;

    public DBHelper(String driverClass, String uri, String login, String password, String loadFile) throws Exception
    {
        Class.forName(driverClass);

        this.connection = DriverManager.getConnection(uri, login, password);

        if (StringUtils.isNotEmpty(loadFile))
        {
            loadSqlFile(loadFile);
        }
    }

    public Connection getConnection()
    {
        return connection;
    }

    public void close()
    {

        try
        {
            connection.close();
        }
        catch (Exception e)
        {
            System.out.println("While closing Connection" + e.getMessage());
        }
    }

    private void loadSqlFile(String fileName) throws Exception
    {
        Statement statement = null;

        try
        {
            statement = connection.createStatement();

            String commands = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);

            for (int targetPos = commands.indexOf(';'); targetPos > -1;
                    targetPos = commands.indexOf(';'))
            {
                String cmd = commands.substring(0, targetPos + 1);
                statement.execute(cmd);
                commands = commands.substring(targetPos + 2);
            }
        }
        finally
        {
            if (statement != null)
            {
                statement.close();
            }
        }
    }
}
