package org.apache.velocity.runtime.resource.loader;

import org.apache.velocity.util.ExtProperties;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Factory for creating connections and prepared statements
 */

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
