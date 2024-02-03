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
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.ExtProperties;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * <p>
 * ResourceLoader to load templates from multiple Jar files.
 * </p>
 * <p>
 * The configuration of the JarResourceLoader is straightforward -
 * You simply add the JarResourceLoader to the configuration via
 * </p>
 * <pre><code>
 *    resource.loaders = jar
 *    resource.loader.jar.class = org.apache.velocity.runtime.resource.loader.JarResourceLoader
 *    resource.loader.jar.path = list of JAR &lt;URL&gt;s
 * </code></pre>
 *
 * <p> So for example, if you had a jar file on your local filesystem, you could simply do</p>
 *    <pre><code>
 *    resource.loader.jar.path = jar:file:/opt/myfiles/jar1.jar
 *    </code></pre>
 * <p> Note that jar specification for the <code>.path</code> configuration property
 * conforms to the same rules for the java.net.JarUrlConnection class.
 * </p>
 *
 * <p> For a working example, see the unit test case,
 *  org.apache.velocity.test.MultiLoaderTestCase class
 * </p>
 *
 * @author <a href="mailto:mailmur@yahoo.com">Aki Nieminen</a>
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id$
 */
public class JarResourceLoader extends ResourceLoader
{
    /**
     * Maps entries to the parent JAR File
     * Key = the entry *excluding* plain directories
     * Value = the JAR URL
     */
    private Map<String, String> entryDirectory = new HashMap<>(559);

    /**
     * Maps JAR URLs to the actual JAR
     * Key = the JAR URL
     * Value = the JAR
     */
    private Map<String, JarHolder> jarfiles = new HashMap<>(89);

    /**
     * Called by Velocity to initialize the loader
     * @param configuration
     */
    @Override
    public void init(ExtProperties configuration)
    {
        log.trace("JarResourceLoader: initialization starting.");

        List<String> paths = configuration.getList(RuntimeConstants.RESOURCE_LOADER_PATHS);

        if (paths != null)
        {
            log.debug("JarResourceLoader # of paths: {}", paths.size() );

            for (ListIterator<String> it = paths.listIterator(); it.hasNext(); )
            {
                String jar = StringUtils.trim(it.next());
                it.set(jar);
                loadJar(jar);
            }
        }

        log.trace("JarResourceLoader: initialization complete.");
    }

    private void loadJar( String path )
    {
        log.debug("JarResourceLoader: trying to load \"{}\"", path);

        // Check path information
        if ( path == null )
        {
            String msg = "JarResourceLoader: can not load JAR - JAR path is null";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        if ( !path.startsWith("jar:") )
        {
            String msg = "JarResourceLoader: JAR path must start with jar: -> see java.net.JarURLConnection for information";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        if (!path.contains("!/"))
        {
            path += "!/";
        }

        // Close the jar if it's already open
        // this is useful for a reload
        closeJar( path );

        // Create a new JarHolder
        JarHolder temp = new JarHolder( rsvc,  path, log );
        // Add it's entries to the entryCollection
        addEntries(temp.getEntries());
        // Add it to the Jar table
        jarfiles.put(temp.getUrlPath(), temp);
    }

    /**
     * Closes a Jar file and set its URLConnection
     * to null.
     */
    private void closeJar( String path )
    {
        if ( jarfiles.containsKey(path) )
        {
            JarHolder theJar = jarfiles.get(path);
            theJar.close();
        }
    }

    /**
     * Copy all the entries into the entryDirectory
     * It will overwrite any duplicate keys.
     */
    private void addEntries( Map<String, String> entries )
    {
        entryDirectory.putAll( entries );
    }

    /**
     * Get a Reader so that the Runtime can build a
     * template with it.
     *
     * @param source name of template to get
     * @param encoding asked encoding
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     * @since 2.0
    */
    @Override
    public Reader getResourceReader(String source, String encoding )
            throws ResourceNotFoundException
    {
        Reader result = null;

        if (org.apache.commons.lang3.StringUtils.isEmpty(source))
        {
            throw new ResourceNotFoundException("Need to have a resource!");
        }

        /*
         *  if a / leads off, then just nip that :)
         */
        if ( source.startsWith("/") )
        {
            source = source.substring(1);
        }

        if ( entryDirectory.containsKey( source ) )
        {
            String jarurl  = entryDirectory.get( source );

            if ( jarfiles.containsKey( jarurl ) )
            {
                JarHolder holder = (JarHolder)jarfiles.get( jarurl );
                InputStream rawStream = holder.getResource( source );
                try
                {
                    return buildReader(rawStream, encoding);
                }
                catch (Exception e)
                {
                    if (rawStream != null)
                    {
                        try
                        {
                            rawStream.close();
                        }
                        catch (IOException ioe) {}
                    }
                    String msg = "JAR resource error: Exception while loading " + source;
                    log.error(msg, e);
                    throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
                }
            }
        }

        throw new ResourceNotFoundException( "JarResourceLoader Error: cannot find resource " +
                source );

    }

    // TODO: SHOULD BE DELEGATED TO THE JARHOLDER

    /**
     * @see ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
     */
    @Override
    public boolean isSourceModified(Resource resource)
    {
        return true;
    }

    /**
     * @see ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    @Override
    public long getLastModified(Resource resource)
    {
        return 0;
    }
}
