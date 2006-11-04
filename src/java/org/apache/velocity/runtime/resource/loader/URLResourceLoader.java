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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;

/**
 * This is a simple URL-based loader.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id: URLResourceLoader.java 191743 2005-06-21 23:22:20Z dlr $
 */
public class URLResourceLoader extends ResourceLoader
{
    private String[] roots = null;
    protected HashMap templateRoots = null;

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
     */
    public void init(ExtendedProperties configuration)
    {
        log.trace("URLResourceLoader : initialization starting.");

        roots = configuration.getStringArray("root");

        if (log.isInfoEnabled())
        {
            for (int i=0; i < roots.length; i++)
            {
                log.info("URLResourceLoader : adding root '" + roots[i] + "'");
            }
        }

        // init the template paths map
        templateRoots = new HashMap();

        log.trace("URLResourceLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to fetch bytestream of
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     */
    public synchronized InputStream getResourceStream(String name)
        throws ResourceNotFoundException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException("URLResourceLoader : No template name provided");
        }

        InputStream inputStream = null;
        Exception exception = null;
        for(int i=0; i < roots.length; i++)
        {
            try
            {
                URL u = new URL(roots[i] + name);
                inputStream = u.openStream();

                if (inputStream != null)
                {
                    if (log.isDebugEnabled()) log.debug("URLResourceLoader: Found '"+name+"' at '"+roots[i]+"'");

                    // save this root for later re-use
                    templateRoots.put(name, roots[i]);
                    break;
                }
            }
            catch(IOException ioe)
            {
                if (log.isDebugEnabled()) log.debug("URLResourceLoader: Exception when looking for '"+name+"' at '"+roots[i]+"'", ioe);

                // only save the first one for later throwing
                if (exception == null)
                {
                    exception = ioe;
                }
            }
        }

        // if we never found the template
        if (inputStream == null)
        {
            String msg;
            if (exception == null)
            {
                msg = "URLResourceLoader : Resource '" + name + "' not found.";
            }
            else
            {
                msg = exception.getMessage();
            }
            // convert to a general Velocity ResourceNotFoundException
            throw new ResourceNotFoundException(msg);
        }

        return inputStream;
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
        if (fileLastModified == 0 ||
            fileLastModified != resource.getLastModified())
        {
            return true;
        }
        return false;
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
            return conn.getLastModified();
        }
        catch (IOException ioe)
        {
            // the file is not reachable at its previous address
            log.warn("URLResourceLoader: '" + name +
                     "' is no longer reachable at '" + root + "'", ioe);
            return 0;
        }
    }

}
