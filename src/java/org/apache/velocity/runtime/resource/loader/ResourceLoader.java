package org.apache.velocity.runtime.resource.loader;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.InputStream;
import java.util.Map;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.runtime.resource.Resource;

import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This is abstract class the all text resource loaders should
 * extend.
 * 
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ResourceLoader.java,v 1.11 2001/08/07 21:58:18 geirm Exp $
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

