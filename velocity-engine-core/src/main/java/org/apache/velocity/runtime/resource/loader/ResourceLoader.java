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
import org.apache.velocity.io.UnicodeInputStream;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCacheImpl;
import org.apache.velocity.util.ExtProperties;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * This is abstract class the all text resource loaders should
 * extend.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
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
    protected Logger log = null;

    /**
     * This initialization is used by all resource
     * loaders and must be called to set up common
     * properties shared by all resource loaders
     *
     * @param rs
     * @param configuration
     */
    public void commonInit(RuntimeServices rs, ExtProperties configuration)
    {
        this.rsvc = rs;
        String loaderName = configuration.getString(RuntimeConstants.RESOURCE_LOADER_IDENTIFIER);
        log = rsvc.getLog("loader." + (loaderName == null ? this.getClass().getSimpleName() : loaderName));

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
            String msg = "Exception parsing cache setting: " + configuration.getString("cache");
            log.error(msg, e);
            throw new VelocityException(msg, e);
        }
        try
        {
            modificationCheckInterval = configuration.getLong("modificationCheckInterval", 0);
        }
        catch (Exception e)
        {
            modificationCheckInterval = 0;
            String msg = "Exception parsing modificationCheckInterval setting: " + configuration.getString("modificationCheckInterval");
            log.error(msg, e);
            throw new VelocityException(msg, e);
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
            String msg = "Exception retrieving resource cache class name";
            log.error(msg, e);
            throw new VelocityException(msg, e);
        }
    }

    /**
     * Initialize the template loader with a
     * a resources class.
     *
     * @param configuration
     */
    public abstract void init(ExtProperties configuration);

    /**
     * Get the Reader that the Runtime will parse
     * to create a template.
     *
     * @param source
     * @return The reader for the requested resource.
     * @throws ResourceNotFoundException
     * @since 2.0
     */
    public abstract Reader getResourceReader(String source, String encoding)
            throws ResourceNotFoundException;

    /**
     * Given a template, check to see if the source of InputStream
     * has been modified.
     *
     * @param resource
     * @return True if the resource has been modified.
     */
    public abstract boolean isSourceModified(Resource resource);

    /**
     * Get the last modified time of the InputStream source
     * that was used to create the template. We need the template
     * here because we have to extract the name of the template
     * in order to locate the InputStream source.
     *
     * @param resource
     * @return Time in millis when the resource has been modified.
     */
    public abstract long getLastModified(Resource resource);

    /**
     * Return the class name of this resource Loader
     *
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
     *
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
     *
     * @return True if this resource loader caches.
     */
    public boolean isCachingOn()
    {
        return isCachingOn;
    }

    /**
     * Set the interval at which the InputStream source
     * should be checked for modifications.
     *
     * @param modificationCheckInterval
     */
    public void setModificationCheckInterval(long modificationCheckInterval)
    {
        this.modificationCheckInterval = modificationCheckInterval;
    }

    /**
     * Get the interval at which the InputStream source
     * should be checked for modifications.
     *
     * @return The modification check interval.
     */
    public long getModificationCheckInterval()
    {
        return modificationCheckInterval;
    }

    /**
     * Check whether any given resource exists. This is not really
     * a very efficient test and it can and should be overridden in the
     * subclasses extending ResourceLoader2.
     *
     * @param resourceName The name of a resource.
     * @return true if a resource exists and can be accessed.
     * @since 1.6
     */
    public boolean resourceExists(final String resourceName)
    {
        Reader reader = null;
        try
        {
            reader = getResourceReader(resourceName, null);
        }
        catch (ResourceNotFoundException e)
        {
            log.debug("Could not load resource '{}' from ResourceLoader {}",
                      resourceName, this.getClass().getName());
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (Exception e)
            {
                String msg = "While closing InputStream for resource '" +
                    resourceName + "' from ResourceLoader " +
                    this.getClass().getName();
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }
        }
        return (reader != null);
    }

    /**
     * Builds a Reader given a raw InputStream and an encoding. Should be use
     * by every subclass that whishes to accept optional BOMs in resources.
     * This method does *not* close the given input stream whenever an exception is thrown.
     *
     * @param rawStream The raw input stream.
     * @param encoding  The asked encoding.
     * @return found reader
     * @throws IOException, UnsupportedEncodingException
     * @since 2.0
     */
    protected Reader buildReader(InputStream rawStream, String encoding)
            throws IOException, UnsupportedEncodingException
    {
        UnicodeInputStream inputStream = new UnicodeInputStream(rawStream);
        /*
         * Check encoding
         */
        String foundEncoding = inputStream.getEncodingFromStream();
        if (foundEncoding != null && encoding != null && !UnicodeInputStream.sameEncoding(foundEncoding, encoding))
        {
            log.warn("Found BOM encoding '{}' differs from asked encoding: '{}' - using BOM encoding to read resource.", foundEncoding, encoding);
            encoding = foundEncoding;
        }
        if (encoding == null)
        {
            if (foundEncoding == null)
            {
                encoding = rsvc.getString(RuntimeConstants.INPUT_ENCODING);
            } else
            {
                encoding = foundEncoding;
            }
        }

        try
        {
            return new InputStreamReader(inputStream, encoding);
        }
        catch (UnsupportedEncodingException uee)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException ioe) {}
            throw uee;
        }
    }

}
