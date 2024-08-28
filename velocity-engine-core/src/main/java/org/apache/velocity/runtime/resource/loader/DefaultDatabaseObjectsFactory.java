package org.apache.velocity.runtime.resource.loader;

import org.apache.velocity.util.ExtProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Database objects factory which will obtain a new connection from the data source and prepare needed statements
 * at each call
 */

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
        return connection.prepareStatement(sql);
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
}
