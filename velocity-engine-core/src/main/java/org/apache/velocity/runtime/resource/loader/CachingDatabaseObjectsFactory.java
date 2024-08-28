package org.apache.velocity.runtime. resource.loader;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.velocity.util.ExtProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Database objects factory which will keep a single connection to be able to cache statements preparation, by means
 * of appropriate pools.</p>
 * <p>This class requires the following optional dependency (maven syntax):</p>
 * <pre><code>
 *         &lt;dependency&gt;
 *             &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
 *             &lt;artifactId&gt;commons-pool2&lt;/artifactId&gt;
 *             &lt;version&gt;2.12.0&lt;/version&gt;
 *          &lt;/dependency&gt;
 * </code></pre>
 * <p>To use this class, you must add the following property to the example configuration described in
 * @link{org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader}
 * </p>
 * <pre><code>
 * resource.loader.ds.database_objects_factory.class = org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader<br>
 * </code></pre>
 * <p>The default size of each pool of prepared statements (there is one pool per statement) is 50. You can tune it
 * with:</p>
 * <pre><code>
 * resource.loader.ds.database_objects_factory. = org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader<br>
 * </code></pre>
 * @see org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader
 */

public class CachingDatabaseObjectsFactory implements DatabaseObjectsFactory {

    private static final String STATEMENTS_POOL_MAX_SIZE = "statements_pool_max_size";
    private static final int STATEMENTS_POOL_MAX_SIZE_DEFAULT = 50;

    private DataSource dataSource;
    private Connection connection;
    private int poolsMaxSize;
    private Map<String, GenericObjectPool<PreparedStatement>> statementsCache = new HashMap<>();

    private class PreparedStatementFactory  extends BasePooledObjectFactory<PreparedStatement>
    {
        private final String sql;

        PreparedStatementFactory(String sql)
        {
            this.sql = sql;
        }

        @Override
        public PreparedStatement create() throws Exception {
            checkConnection();
            return connection.prepareStatement(sql);
        }

        @Override
        public PooledObject<PreparedStatement> wrap(PreparedStatement obj) {
            return new DefaultPooledObject<>(obj);
        }

        @Override
        public void destroyObject(final PooledObject<PreparedStatement> p) throws Exception  {
            p.getObject().close();
        }
    }

    /**
     * Initialize the factory with the DataSourceResourceLoader properties
     * @param dataSource data source
     */
    @Override
    public void init(DataSource dataSource, ExtProperties properties) throws SQLException
    {
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
        this.poolsMaxSize = Optional.ofNullable(properties.getInt(STATEMENTS_POOL_MAX_SIZE)).orElse(STATEMENTS_POOL_MAX_SIZE_DEFAULT);
    }

    /**
     * Prepare a statement
     * @param sql Statement SQL
     * @return prepared statement
     */
    @Override
    public synchronized PreparedStatement prepareStatement(String sql) throws SQLException
    {
        GenericObjectPool<PreparedStatement> pool = statementsCache.computeIfAbsent(sql, (String key) -> {
            GenericObjectPoolConfig<PreparedStatement> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(poolsMaxSize);
            return new GenericObjectPool<>(
                    new PreparedStatementFactory(sql),
                    poolConfig
            );
        });
        try
        {
            return pool.borrowObject();
        }
        catch (SQLException sqle)
        {
            throw sqle;
        }
        catch (Exception e)
        {
            throw new SQLException("could not prepare statement", e);
        }
    }

    private void checkConnection() throws SQLException
    {
        if (!connection.isValid(0))
        {
            // refresh connection
            connection = dataSource.getConnection();
            statementsCache = new HashMap<>();
        }
    }

    /**
     * Releases a prepared statement
     * @param sql original sql query
     * @param stmt statement
     */
    @Override
    public void releaseStatement(String sql, PreparedStatement stmt) throws SQLException
    {
        GenericObjectPool<PreparedStatement> pool = statementsCache.get(sql);
        if (pool == null)
        {
            throw new SQLException("statement is not pooled");
        }
        pool.returnObject(stmt);
    }

    @Override
    public void destroy()
    {
        statementsCache.values().forEach(pool ->
        {
            pool.close();
            pool.clear();
        });
        try
        {
            connection.close();
        }
        catch (SQLException sqle) {}
    };
}
