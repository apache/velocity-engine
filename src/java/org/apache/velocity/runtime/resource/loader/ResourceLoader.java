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

import java.io.InputStream;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCacheImpl;

import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This is abstract class the all text resource loaders should
 * extend.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public abstract class ResourceLoader
{
    /**
     * Does this loader want templates produced with it
     * cached in the Runtime.
     */
     protected boolean isCachingOn = false;

    /**
     * This property will be passed on to the templates
     * that are created with this loader.
     */
    protected long modificationCheckInterval = 2;

    /**
     * Class name for this loader, for logging/debuggin
     * purposes.
     */
    protected String className = null;

    protected RuntimeServices rsvc = null;
    protected Log log = null;

    /**
     * This initialization is used by all resource
     * loaders and must be called to set up common
     * properties shared by all resource loaders
     * @param rs
     * @param configuration
     */
    public void commonInit( RuntimeServices rs, ExtendedProperties configuration)
    {
        this.rsvc = rs;
        this.log = rsvc.getLog();

        /*
         *  these two properties are not required for all loaders.
         *  For example, for ClasspathLoader, what would cache mean?
         *  so adding default values which I think are the safest
         *
         *  don't cache, and modCheckInterval irrelevant...
         */

        try
        {
            isCachingOn = configuration.getBoolean("cache", false);
        }
        catch (Exception e)
        {
            isCachingOn = false;
            log.error("Exception using default of '" + isCachingOn + '\'', e);
        }
        try
        {
            modificationCheckInterval = configuration.getLong("modificationCheckInterval", 0);
        }
        catch (Exception e)
        {
            modificationCheckInterval = 0;
            log.error("Exception using default of '" +
                      modificationCheckInterval + '\'', e);
        }

        /*
         * this is a must!
         */
        className = ResourceCacheImpl.class.getName();
        try
        {
            className = configuration.getString("class", className);
        }
        catch (Exception e)
        {
            log.error("Exception using default of '" + className + '\'', e);
        }
    }

    /**
     * Initialize the template loader with a
     * a resources class.
     * @param configuration
     */
    public abstract void init( ExtendedProperties configuration);

    /**
     * Get the InputStream that the Runtime will parse
     * to create a template.
     * @param source
     * @return The input stream for the requested resource.
     * @throws ResourceNotFoundException
     */
    public abstract InputStream getResourceStream( String source )
        throws ResourceNotFoundException;

    /**
     * Given a template, check to see if the source of InputStream
     * has been modified.
     * @param resource
     * @return True if the resource has been modified.
     */
    public abstract boolean isSourceModified(Resource resource);

    /**
     * Get the last modified time of the InputStream source
     * that was used to create the template. We need the template
     * here because we have to extract the name of the template
     * in order to locate the InputStream source.
     * @param resource
     * @return Time in millis when the resource has been modified.
     */
    public abstract long getLastModified(Resource resource);

    /**
     * Return the class name of this resource Loader
     * @return Class name of the resource loader.
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Set the caching state. If true, then this loader
     * would like the Runtime to cache templates that
     * have been created with InputStreams provided
     * by this loader.
     * @param value
     */
    public void setCachingOn(boolean value)
    {
        isCachingOn = value;
    }

    /**
     * The Runtime uses this to find out whether this
     * template loader wants the Runtime to cache
     * templates created with InputStreams provided
     * by this loader.
     * @return True if this resource loader caches.
     */
    public boolean isCachingOn()
    {
        return isCachingOn;
    }

    /**
     * Set the interval at which the InputStream source
     * should be checked for modifications.
     * @param modificationCheckInterval
     */
    public void setModificationCheckInterval(long modificationCheckInterval)
    {
        this.modificationCheckInterval = modificationCheckInterval;
    }

    /**
     * Get the interval at which the InputStream source
     * should be checked for modifications.
     * @return The modification check interval.
     */
    public long getModificationCheckInterval()
    {
        return modificationCheckInterval;
    }
}
