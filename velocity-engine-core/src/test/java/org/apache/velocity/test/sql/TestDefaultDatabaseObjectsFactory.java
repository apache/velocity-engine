package org.apache.velocity.test.sql;

import org.apache.velocity.runtime.resource.loader.DefaultDatabaseObjectsFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestDefaultDatabaseObjectsFactory extends DefaultDatabaseObjectsFactory {
    public static int connectionCount = 0;

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement ps = super.prepareStatement(sql);
        connectionCount++;
        return ps;
    }

    @Override
    public void releaseStatement(String sql, PreparedStatement stmt) throws SQLException {

        super.releaseStatement(sql, stmt);
        connectionCount--;
    }
    public static int getConnectionCount() {
        return connectionCount;
    }
}
