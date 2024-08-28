package org.apache.velocity.test.sql;

import org.apache.velocity.util.ExtProperties;

public class DatasourceResourceLoaderCachingFactoryTestCase extends DataSourceResourceLoaderTestCase
{
    public DatasourceResourceLoaderCachingFactoryTestCase(String name) throws Exception {
        super(name);
    }

    @Override
    protected ExtProperties getResourceLoaderProperties()
    {
        ExtProperties props = super.getResourceLoaderProperties();
        props.put("ds.database_objects_factory.class", "org.apache.velocity.resource.loader.CachingDatabaseObjectsFactory");
        return props;
    }

}
