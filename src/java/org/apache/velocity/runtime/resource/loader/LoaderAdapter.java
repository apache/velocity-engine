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

import org.apache.commons.collections.ExtendedProperties;

import org.apache.velocity.runtime.resource.Resource;

/**
 * A wrapper for a <code>ResourceLoader</code>, useful when you want
 * to layer additional functionality over the top of an existing
 * loader.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 */
public abstract class LoaderAdapter
    extends ResourceLoader
{
    /**
     * Resource loader we are going to wrap.
     */
    protected ResourceLoader resourceLoader = null;

    /**
     * Intializes the loader which this class wraps based on the
     * <code>loader</code> configuration parameter for the applicable
     * <code>ResourceLoader</code>.
     */
    protected void initLoader(ExtendedProperties configuration)
    {
        // Use our sub-class's name.
        String className = getClass().getName();
        int i = className.lastIndexOf('.');
        if (i != -1)
        {
            className = className.substring(i + 1);
        }

        // We must have a resource loader to wrap.
        String loaderClass = configuration.getString("loader");

        try
        {
            ExtendedProperties loaderConfiguration =
                configuration.subset("loader");

            resourceLoader = ResourceLoaderFactory.getLoader(rsvc, loaderClass);
            resourceLoader.commonInit(rsvc, loaderConfiguration);
            resourceLoader.init(loaderConfiguration);

            rsvc.info(className + " : using loader : " + loaderClass);
        }
        catch (Exception e)
        {
            rsvc.error(className + " : problem instantiating loader : " + e);
        }
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(Resource)
     */
    public boolean isSourceModified(Resource resource)
    {
        return resourceLoader.isSourceModified(resource);
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(Resource)
     */
    public long getLastModified(Resource resource)
    {
        return resourceLoader.getLastModified(resource);
    }
}
