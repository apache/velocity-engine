package org.apache.velocity.runtime.resource.loader;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;

import org.apache.velocity.runtime.RuntimeServices;

import org.apache.velocity.runtime.resource.Resource;

import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This is abstract class the all text resource loaders should
 * extend.
 * 
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ResourceLoader.java,v 1.14.4.1 2004/03/03 23:23:02 geirm Exp $
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

    /**
     * This initialization is used by all resource
     * loaders and must be called to set up common
     * properties shared by all resource loaders
     */
    public void commonInit( RuntimeServices rs, ExtendedProperties configuration)
    {
        this.rsvc = rs;

        /*
         *  these two properties are not required for all loaders.
         *  For example, for ClasspathLoader, what would cache mean? 
         *  so adding default values which I think are the safest
         *
         *  don't cache, and modCheckInterval irrelevant...
         */

        isCachingOn = configuration.getBoolean("cache", false);
        modificationCheckInterval = configuration.getLong("modificationCheckInterval", 0);
        
        /*
         * this is a must!
         */

        className = configuration.getString("class");
    }

    /** 
     * Initialize the template loader with a
     * a resources class.
     */
    public abstract void init( ExtendedProperties configuration);

    /** 
     * Get the InputStream that the Runtime will parse
     * to create a template.
     */
    public abstract InputStream getResourceStream( String source ) 
        throws ResourceNotFoundException;

    /**
     * Given a template, check to see if the source of InputStream
     * has been modified.
     */
    public abstract boolean isSourceModified(Resource resource);
    
    /**
     * Get the last modified time of the InputStream source
     * that was used to create the template. We need the template
     * here because we have to extract the name of the template
     * in order to locate the InputStream source.
     */
    public abstract long getLastModified(Resource resource);

    /**
     * Return the class name of this resource Loader
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
     */
    public boolean isCachingOn()
    {
        return isCachingOn;
    }

    /**
     * Set the interval at which the InputStream source
     * should be checked for modifications.
     */
    public void setModificationCheckInterval(long modificationCheckInterval)
    {
        this.modificationCheckInterval = modificationCheckInterval;
    }
    
    /**
     * Get the interval at which the InputStream source
     * should be checked for modifications.
     */
    public long getModificationCheckInterval()
    {
        return modificationCheckInterval;
    }
}
