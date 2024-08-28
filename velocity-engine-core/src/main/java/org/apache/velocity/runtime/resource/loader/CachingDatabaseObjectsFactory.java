package org.apache.velocity.runtime. resource.loader;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.velocity.util.ExtProperties;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>Database objects factory which will keep a single connection to be able to cache statements preparation, by means
 * of appropriate pools.</p>
 * <p>This class requires the following optional dependency (maven syntax):</p>
 * <pre><code>
 *         &lt;dependency&gt;
 *             &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
 *             &lt;artifactId&gt;commons-pool2&lt;/artifactId&gt;
 *             &lt;version&gt;2.12.0&lt;/version&gt;
 *             &lt;scope&gt;runtime&lt;/scope&gt;
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
    private KeyedObjectPool<String, PreparedStatement> statementsPool;
    protected Logger log = null;

    private class PreparedStatementFactory  extends BaseKeyedPooledObjectFactory<String, PreparedStatement>
    {
        @Override
        public PreparedStatement create(String sql) throws Exception {
            checkConnection();
            return connection.prepareStatement(sql);
        }

        @Override
        public PooledObject<PreparedStatement> wrap(PreparedStatement obj) {
            return new DefaultPooledObject<>(obj);
        }

        @Override
        public boolean validateObject(String key, PooledObject<PreparedStatement> obj)
        {
            try
            {
                return !obj.getObject().isClosed() && obj.getObject().getConnection().isValid(0);
            }
            catch (SQLException sqle)
            {
                return false;
            }
        }

        @Override
        public void destroyObject(String key, PooledObject<PreparedStatement> p) throws Exception {
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
        this.poolsMaxSize = properties.getInt(STATEMENTS_POOL_MAX_SIZE, STATEMENTS_POOL_MAX_SIZE_DEFAULT);
        createStatementsPool();
    }

    private void createStatementsPool()
    {
        GenericKeyedObjectPoolConfig<PreparedStatement> poolConfig = new GenericKeyedObjectPoolConfig<>();
        poolConfig.setMaxTotal(poolsMaxSize);
        poolConfig.setTestOnBorrow(true);
        statementsPool = new GenericKeyedObjectPool<>(new PreparedStatementFactory(), poolConfig);
    }

    @Override
    public void setLogger(Logger log)
    {
        this.log = log;
    }

    /**
     * Prepare a statement
     * @param sql Statement SQL
     * @return prepared statement
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        try
        {
            return statementsPool.borrowObject(sql);
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

    protected synchronized void checkConnection() throws SQLException
    {
        if (!connection.isValid(0))
        {
            reset();
        }
    }

    private void reset() throws SQLException
    {
        clear();
        // refresh connection and statements pool
        connection = dataSource.getConnection();
        createStatementsPool();
    }

    /**
     * Releases a prepared statement
     * @param sql original sql query
     * @param stmt statement
     */
    @Override
    public void releaseStatement(String sql, PreparedStatement stmt) throws SQLException
    {
        try
        {
            statementsPool.returnObject(sql, stmt);
        }
        catch (Exception e)
        {
            if (log != null)
            {
                log.debug("could not return statement to the pool", e);
            }
        }
    }

    /**
     * Free resources
     */
    @Override
    public void clear()
    {
        statementsPool.close();
        try
        {
            statementsPool.clear();
        }
        catch (Exception e)
        {
            if (log != null)
            {
                log.debug("statements pool clearing gave an exception", e);
            }
        }
        try
        {
            connection.close();
        }
        catch (SQLException sqle)
        {
            log.debug("connection closing gave an exception", sqle);
        }
        finally
        {
            connection = null;
        }
    }
}
