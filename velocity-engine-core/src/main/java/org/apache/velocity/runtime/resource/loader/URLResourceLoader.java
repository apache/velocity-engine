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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.ExtProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * This is a simple URL-based loader.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id: URLResourceLoader.java 191743 2005-06-21 23:22:20Z dlr $
 * @since 1.5
 */
public class URLResourceLoader extends ResourceLoader
{
    private String[] roots = null;
    protected HashMap templateRoots = null;
    private int timeout = -1;

    /**
     * @see ResourceLoader#init(org.apache.velocity.util.ExtProperties)
     */
    public void init(ExtProperties configuration)
    {
        log.trace("URLResourceLoader: initialization starting.");

        roots = configuration.getStringArray("root");
        if (log.isDebugEnabled())
        {
            for (String root : roots)
            {
                log.debug("URLResourceLoader: adding root '{}'", root);
            }
        }

        timeout = configuration.getInt("timeout", -1);

        // init the template paths map
        templateRoots = new HashMap();

        log.trace("URLResourceLoader: initialization complete.");
    }

    /**
     * Get a Reader so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to fetch bytestream of
     * @param encoding asked encoding
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     * @since 2.0
     */
    public synchronized Reader getResourceReader(String name, String encoding)
            throws ResourceNotFoundException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException("URLResourceLoader: No template name provided");
        }

        Reader reader = null;
        Exception exception = null;
        for (String root : roots)
        {
            InputStream rawStream = null;
            try
            {
                URL u = new URL(root + name);
                URLConnection conn = u.openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                rawStream = conn.getInputStream();
                reader = buildReader(rawStream, encoding);

                if (reader != null)
                {
                    log.debug("URLResourceLoader: Found '{}' at '{}'", name, root);

                    // save this root for later re-use
                    templateRoots.put(name, root);
                    break;
                }
            }
            catch (IOException ioe)
            {
                if (rawStream != null)
                {
                    try
                    {
                        rawStream.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
                log.debug("URLResourceLoader: Exception when looking for '{}' at '{}'", name, root, ioe);

                // only save the first one for later throwing
                if (exception == null)
                {
                    exception = ioe;
                }
            }
        }

        // if we never found the template
        if (reader == null)
        {
            String msg;
            if (exception == null)
            {
                msg = "URLResourceLoader: Resource '" + name + "' not found.";
            }
            else
            {
                msg = exception.getMessage();
            }
            // convert to a general Velocity ResourceNotFoundException
            throw new ResourceNotFoundException(msg);
        }

        return reader;
    }

    /**
     * Checks to see if a resource has been deleted, moved or modified.
     *
     * @param resource Resource  The resource to check for modification
     * @return boolean  True if the resource has been modified, moved, or unreachable
     */
    public boolean isSourceModified(Resource resource)
    {
        long fileLastModified = getLastModified(resource);
        // if the file is unreachable or otherwise changed
        return fileLastModified == 0 ||
            fileLastModified != resource.getLastModified();
    }

    /**
     * Checks to see when a resource was last modified
     *
     * @param resource Resource the resource to check
     * @return long The time when the resource was last modified or 0 if the file can't be reached
     */
    public long getLastModified(Resource resource)
    {
        // get the previously used root
        String name = resource.getName();
        String root = (String)templateRoots.get(name);

        try
        {
            // get a connection to the URL
            URL u = new URL(root + name);
            URLConnection conn = u.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            return conn.getLastModified();
        }
        catch (IOException ioe)
        {
            // the file is not reachable at its previous address
            String msg = "URLResourceLoader: '"+name+"' is no longer reachable at '"+root+"'";
            log.error(msg, ioe);
            throw new ResourceNotFoundException(msg, ioe);
        }
    }

    /**
     * Returns the current, custom timeout setting. If negative, there is no custom timeout.
     * @since 1.6
     */
    public int getTimeout()
    {
        return timeout;
    }
}
