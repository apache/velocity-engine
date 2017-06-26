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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DBHelper
{
    private String driverClass = null;
    private Connection connection = null;

    public DBHelper(String driverClass, String uri, String login, String password, String loadFile) throws Exception
    {
        this.driverClass = driverClass;
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

    // avoid ';' inside BEGIN/END blocks
    private static int nextSemiColon(final String cmd)
    {
        int start = 0;
        int ret = -1;
        while (true)
        {
            ret = cmd.indexOf(';', start);
            if (ret == -1) break;
            int begin = cmd.lastIndexOf("BEGIN", ret);
            int end = cmd.lastIndexOf("END;", ret);
            if (begin == -1) break;
            if (end > begin) break;
            start = ret + 1;
        }
        return ret;
    }

    private void loadSqlFile(String fileName) throws Exception
    {
        Statement statement = null;

        try
        {
            String commands = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            // manually eat comments, some engines don't like them
            Pattern removeComments = Pattern.compile("^--.*$", Pattern.MULTILINE);
            Matcher matcher = removeComments.matcher(commands);
            commands = matcher.replaceAll("");
            for (int targetPos = nextSemiColon(commands); targetPos > -1; targetPos = nextSemiColon(commands))
            {
                statement = connection.createStatement();
                String cmd = commands.substring(0, targetPos + 1);
                // Oracle doesn't like semi-colons at the end, except for BEGIN/END blocks...
                // nor does Derby
                if (driverClass.equals("oracle.jdbc.OracleDriver") && !cmd.endsWith("END;") ||
                    driverClass.equals("org.apache.derby.jdbc.EmbeddedDriver"))
                {
                    cmd = cmd.substring(0, cmd.length() - 1);
                }
                statement.executeUpdate(cmd);
                commands = commands.substring(targetPos + 2);
                statement.close();
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
