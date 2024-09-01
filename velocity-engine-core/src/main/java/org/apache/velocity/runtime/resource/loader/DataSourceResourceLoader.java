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

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.ExtProperties;

import org.apache.commons.lang3.StringUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * <p>This is a simple template file loader that loads templates
 * from a DataSource instead of plain files.</p>
 *
 * <p>It can be configured with a datasource name, a table name,
 * id column (name), content column (the template body) and a
 * datetime column (for last modification info).</p>
 * <br>
 * <p>Example configuration snippet for velocity.properties:</p>
 * <br>
 * <pre><code>
 * resource.loaders = file, ds
 *
 * resource.loader.ds.description = Velocity DataSource Resource Loader <br>
 * resource.loader.ds.class = org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader <br>
 * resource.loader.ds.resource.datasource_url = java:comp/env/jdbc/Velocity <br>
 * resource.loader.ds.resource.table = tb_velocity_template <br>
 * resource.loader.ds.resource.key_column = id_template <br>
 * resource.loader.ds.resource.template_column = template_definition <br>
 * resource.loader.ds.resource.timestamp_column = template_timestamp <br>
 * resource.loader.ds.cache = false <br>
 * resource.loader.ds.modification_check_interval = 60 <br>
 * resource.loader.ds.statements_pool_max_size = 50 <br>
 * </code></pre>
 * <p>Optionally, the developer can instantiate the DataSourceResourceLoader and set the DataSource via code in
 * a manner similar to the following:</p>
 * <br>
 * <pre><code>
 * DataSourceResourceLoader ds = new DataSourceResourceLoader();
 * ds.setDataSource(DATASOURCE);
 * Velocity.setProperty("resource.loader.ds.instance",ds);
 * </code></pre>
 * <p> The property <code>resource.loader.ds.class</code> should be left out, otherwise all the other
 * properties in velocity.properties would remain the same.</p>
 * <br>
 * <p>Example WEB-INF/web.xml:</p>
 * <br>
 * <pre><code>
 *  &lt;resource-ref&gt;
 *   &lt;description&gt;Velocity template DataSource&lt;/description&gt;
 *   &lt;res-ref-name&gt;jdbc/Velocity&lt;/res-ref-name&gt;
 *   &lt;res-type&gt;javax.sql.DataSource&lt;/res-type&gt;
 *   &lt;res-auth&gt;Container&lt;/res-auth&gt;
 *  &lt;/resource-ref&gt;
 * </code></pre>
 *  <br>
 * and Tomcat 4 server.xml file: <br>
 * <pre><code>
 *  [...]
 *  &lt;Context path="/exampleVelocity" docBase="exampleVelocity" debug="0"&gt;
 *  [...]
 *   &lt;ResourceParams name="jdbc/Velocity"&gt;
 *    &lt;parameter&gt;
 *      &lt;name&gt;driverClassName&lt;/name&gt;
 *      &lt;value&gt;org.hsql.jdbcDriver&lt;/value&gt;
 *    &lt;/parameter&gt;
 *    &lt;parameter&gt;
 *     &lt;name&gt;driverName&lt;/name&gt;
 *     &lt;value&gt;jdbc:HypersonicSQL:database&lt;/value&gt;
 *    &lt;/parameter&gt;
 *    &lt;parameter&gt;
 *     &lt;name&gt;user&lt;/name&gt;
 *     &lt;value&gt;database_username&lt;/value&gt;
 *    &lt;/parameter&gt;
 *    &lt;parameter&gt;
 *     &lt;name&gt;password&lt;/name&gt;
 *     &lt;value&gt;database_password&lt;/value&gt;
 *    &lt;/parameter&gt;
 *   &lt;/ResourceParams&gt;
 *  [...]
 *  &lt;/Context&gt;
 *  [...]
 * </code></pre>
 * <br>
 *  <p>Example sql script:</p>
 * <pre><code>
 *  CREATE TABLE tb_velocity_template (
 *    id_template varchar (40) NOT NULL ,
 *    template_definition text (16) NOT NULL ,
 *    template_timestamp datetime NOT NULL
 *  );
 * </code></pre>
 * <p>Prior to Velocity 2.4, this class should not be considered thread-safe.</p>
 * <p>Since Velocity 2.4, the handling of JDBC connections and prepared statements is delegated to the
 * {@link org.apache.velocity.runtime.resource.loader.DatabaseObjectsFactory} instance. The default class for this
 * database objects factory is {@link org.apache.velocity.runtime.resource.loader.DefaultDatabaseObjectsFactory},
 * which obtains a new connection from the data source and prepares statements at each query. You can configure this
 * resource loader to use the {@link org.apache.velocity.runtime.resource.loader.CachingDatabaseObjectsFactory} which
 * will keep a single connection and tries to reuse prepared statements.
 * statements</p>
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @author <a href="mailto:david.kinnvall@alertir.com">David Kinnvall</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:lachiewicz@plusnet.pl">Sylwester Lachiewicz</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 * @since 1.5
 */
public class DataSourceResourceLoader extends ResourceLoader
{
    private static final String DATABASE_OBJECTS_FACTORY_DEFAULT_CLASS = "org.apache.velocity.runtime.resource.loader.DefaultDatabaseObjectsFactory";

    private DataSource dataSource;
    private DatabaseObjectsFactory factory;
    private String templateColumn;
    private String timestampColumn;
    private String templateSQL;
    private String timestampSQL;

    private class SelfCleaningReader extends FilterReader
    {
        private final ResultSet resultSet;

        public SelfCleaningReader(Reader reader, ResultSet resultSet)
        {
            super(reader);
            this.resultSet = resultSet;
        }

        @Override
        public void close() throws IOException
        {
            super.close();
            try
            {
                resultSet.close();
                factory.releaseStatement(templateSQL, (PreparedStatement)resultSet.getStatement());
            }
            catch (RuntimeException re)
            {
                throw re;
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }

    /**
     * @see ResourceLoader#init(org.apache.velocity.util.ExtProperties)
     */
    @Override
    public void init(ExtProperties configuration)
    {
        String tableName       = StringUtils.trim(configuration.getString("resource.table"));
        String keyColumn       = StringUtils.trim(configuration.getString("resource.key_column"));
        templateColumn  = StringUtils.trim(configuration.getString("resource.template_column"));
        timestampColumn = StringUtils.trim(configuration.getString("resource.timestamp_column"));

        templateSQL = "SELECT " + templateColumn + " FROM " + tableName + " WHERE " + keyColumn + " = ?";
        timestampSQL = "SELECT " + timestampColumn + " FROM " + tableName + " WHERE " + keyColumn + " = ?";

        String dataSourceName  = StringUtils.trim(configuration.getString("resource.datasource_url"));

        if (dataSource != null)
        {
            log.debug("DataSourceResourceLoader: using dataSource instance with table \"{}\"", tableName);
            log.debug("DataSourceResourceLoader: using columns \"{}\", \"{}\" and \"{}\"", keyColumn, templateColumn, timestampColumn);

        }
        else if (dataSourceName != null)
        {
            log.debug("DataSourceResourceLoader: using \"{}\" datasource with table \"{}\"", dataSourceName, tableName);
            log.debug("DataSourceResourceLoader: using columns \"{}\", \"{}\" and \"{}\"", keyColumn, templateColumn, timestampColumn);

            try
            {
                dataSource = (DataSource) new InitialContext().lookup(dataSourceName);
            }
            catch (NamingException ne)
            {
                throw new VelocityException("could not lookup datasource for name: " + dataSourceName, ne);
            }

            log.trace("DataSourceResourceLoader initialized.");
        }
        else
        {
            String msg = "DataSourceResourceLoader not properly initialized. No DataSource was identified.";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        String factoryClassName = configuration.getString("database_objects_factory.class");
        if (factoryClassName == null)
        {
            factoryClassName = DATABASE_OBJECTS_FACTORY_DEFAULT_CLASS;
        }
        try
        {
            Class<?> factoryClass = ClassUtils.getClass(factoryClassName);
            factory = (DatabaseObjectsFactory) factoryClass.getDeclaredConstructor().newInstance();
            factory.init(dataSource, configuration.subset("database_objects_factory"));
        }
        catch (Exception e)
        {
            throw new VelocityException("could not find database objects factory class", e);
        }

        log.trace("DataSourceResourceLoader initialized.");
    }

    /**
     * Set the DataSource used by this resource loader.  Call this as an alternative to
     * specifying the data source name via properties.
     * @param dataSource The data source for this ResourceLoader.
     */
    public void setDataSource(final DataSource dataSource)
    {
        if (factory != null)
        {
            throw new VelocityException("cannot change data source after initialization");
        }
        this.dataSource = dataSource;
    }

    /**
     * @see ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
     */
    @Override
    public boolean isSourceModified(final Resource resource)
    {
        return (resource.getLastModified() !=
                readLastModified(resource, "checking timestamp"));
    }

    /**
     * @see ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    @Override
    public long getLastModified(final Resource resource)
    {
        return readLastModified(resource, "getting timestamp");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template
     * @param encoding asked encoding
     * @return InputStream containing template
     * @throws ResourceNotFoundException
     * @since 2.0
     */
    @Override
    public synchronized Reader getResourceReader(final String name, String encoding)
            throws ResourceNotFoundException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException("DataSourceResourceLoader: Template name was empty or null");
        }

        ResultSet rs = null;
        try
        {
            PreparedStatement statement = factory.prepareStatement(templateSQL);
            rs = fetchResult(statement, name);

            if (rs.next())
            {
                Reader reader = getReader(rs, templateColumn, encoding);
                if (reader == null)
                {
                    throw new ResourceNotFoundException("DataSourceResourceLoader: "
                            + "template column for '"
                            + name + "' is null");
                }
                return new SelfCleaningReader(reader, rs);
            }
            else
            {
                throw new ResourceNotFoundException("DataSourceResourceLoader: "
                        + "could not find resource '"
                        + name + "'");

            }
        }
        catch (Exception e)
        {
            String msg = "DataSourceResourceLoader: database problem while getting resource '"
                    + name + "': ";

            log.error(msg, e);
            throw new ResourceNotFoundException(msg);
        }
    }

    /**
     * Fetches the last modification time of the resource
     *
     * @param resource Resource object we are finding timestamp of
     * @param operation string for logging, indicating caller's intention
     *
     * @return timestamp as long
     */
    private long readLastModified(final Resource resource, final String operation)
    {
        long timeStamp = 0;

        /* get the template name from the resource */
        String name = resource.getName();
        if (name == null || name.length() == 0)
        {
            String msg = "DataSourceResourceLoader: Template name was empty or null";
            log.error(msg);
            throw new NullPointerException(msg);
        }
        else
        {
            PreparedStatement statement = null;
            ResultSet rs = null;
            try
            {
                statement = factory.prepareStatement(timestampSQL);
                rs = fetchResult(statement, name);

                if (rs.next())
                {
                    Timestamp ts = rs.getTimestamp(timestampColumn);
                    timeStamp = ts != null ? ts.getTime() : 0;
                }
                else
                {
                    String msg = "DataSourceResourceLoader: could not find resource " + name + " while " + operation;
                    log.error(msg);
                    throw new ResourceNotFoundException(msg);
                }
            }
            catch (Exception e)
            {
                String msg = "DataSourceResourceLoader: database problem while "
                            + operation + " of '" + name + "': ";

                log.error(msg, e);
                throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
            }
            finally
            {
                closeResultSet(rs);
                if (statement != null)
                {
                    try
                    {
                        factory.releaseStatement(timestampSQL, statement);
                    }
                    catch (SQLException sqle)
                    {
                        // just log, don't throw
                        log.debug("DataSourceResourceLoader: error releasing prepared statement", sqle);
                    }
                }
            }
        }
        return timeStamp;
    }

    /**
     * Closes the result set.
     */
    private void closeResultSet(final ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (RuntimeException re)
            {
                throw re;
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }

    /**
     * Fetches the result for a given template name.
     * Inherit this method if there is any calculation to perform on the template name.
     *
     * @param ps target prepared statement
     * @param templateName input template name
     * @return result set
     * @throws SQLException
     */
    protected ResultSet fetchResult(
        final PreparedStatement ps,
        final String templateName
    ) throws SQLException
    {
        ps.setString(1, templateName);
        return ps.executeQuery();
    }

    /**
     * Gets a reader from a result set's column.
     * @param resultSet result set
     * @param column template column
     * @param encoding template encoding (unused)
     * @return reader
     * @throws SQLException
     * @deprecated the 'encoding' parameter is useless, it should have been set in the database. Use {@link #getReader(ResultSet, String)}
     */
    protected Reader getReader(ResultSet resultSet, String column, String encoding)
        throws SQLException
    {
        return getReader(resultSet, column);
    }

    /**
     * Gets a reader from a result set's column.
     * @param resultSet result set
     * @param column template column
     * @return reader
     * @throws SQLException
     */
    protected Reader getReader(ResultSet resultSet, String column)
            throws SQLException
    {
        return resultSet.getCharacterStream(column);
    }

    /**
     * Frees all resources.
     */
    public void clear()
    {
        if (factory != null)
        {
            factory.clear();
        }
    }

}
