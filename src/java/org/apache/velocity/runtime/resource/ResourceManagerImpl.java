package org.apache.velocity.runtime.resource;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoaderFactory;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.StringUtils;


/**
 * Class to manage the text resource for the Velocity Runtime.
 *
 * @author  <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author  <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author  <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author  <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author  <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version  $Id$
 */
public class ResourceManagerImpl
    implements ResourceManager
{

    /** A template resources. */
    public static final int RESOURCE_TEMPLATE = 1;

    /** A static content resource. */
    public static final int RESOURCE_CONTENT = 2;

    /** token used to identify the loader internally. */
    private static final String RESOURCE_LOADER_IDENTIFIER = "_RESOURCE_LOADER_IDENTIFIER_";

    /** Object implementing ResourceCache to be our resource manager's Resource cache. */
    protected ResourceCache globalCache = null;

    /** The List of templateLoaders that the Runtime will use to locate the InputStream source of a template. */
    protected final List resourceLoaders = new ArrayList();

    /**
     * This is a list of the template input stream source initializers, basically properties for a particular template stream
     * source. The order in this list reflects numbering of the properties i.e.
     *
     * <p>&lt;loader-id&gt;.resource.loader.&lt;property&gt; = &lt;value&gt;</p>
     */
    private final List sourceInitializerList = new ArrayList();

    /**
     * Has this Manager been initialized?
     */
    private boolean isInit = false;

    /** switch to turn off log notice when a resource is found for the first time. */
    private boolean logWhenFound = true;

    /** The internal RuntimeServices object. */
    protected RuntimeServices rsvc = null;

    /** Logging. */
    protected Log log = null;

    /**
     * Initialize the ResourceManager.
     *
     * @param  rsvc  The Runtime Services object which is associated with this Resource Manager.
     *
     * @throws  Exception
     */
    public synchronized void initialize(final RuntimeServices rsvc)
        throws Exception
    {
	if (isInit)
	{
	    log.warn("Re-initialization of ResourceLoader attempted!");
	    return;
	}
	
        ResourceLoader resourceLoader = null;

        this.rsvc = rsvc;
        log = rsvc.getLog();

        log.debug("Default ResourceManager initializing. (" + this.getClass() + ")");

        assembleResourceLoaderInitializers();

        for (Iterator it = sourceInitializerList.iterator(); it.hasNext();)
        {
            /**
             * Resource loader can be loaded either via class name or be passed
             * in as an instance.
             */
            ExtendedProperties configuration = (ExtendedProperties) it.next();

            String loaderClass = StringUtils.nullTrim(configuration.getString("class"));
            ResourceLoader loaderInstance = (ResourceLoader) configuration.get("instance");

            if (loaderInstance != null)
            {
                resourceLoader = loaderInstance;
            }
            else if (loaderClass != null)
            {
                resourceLoader = ResourceLoaderFactory.getLoader(rsvc, loaderClass);
            }
            else
            {
                log.error("Unable to find '" +
                          configuration.getString(RESOURCE_LOADER_IDENTIFIER) +
                          ".resource.loader.class' specification in configuration." +
                          " This is a critical value.  Please adjust configuration.");

                continue; // for(...
            }

            resourceLoader.commonInit(rsvc, configuration);
            resourceLoader.init(configuration);
            resourceLoaders.add(resourceLoader);
        }

        /*
         * now see if this is overridden by configuration
         */

        logWhenFound = rsvc.getBoolean(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, true);

        /*
         *  now, is a global cache specified?
         */

        String cacheClassName = rsvc.getString(RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS);

        Object cacheObject = null;

        if (org.apache.commons.lang.StringUtils.isNotEmpty(cacheClassName))
        {

            try
            {
                cacheObject = ClassUtils.getNewInstance(cacheClassName);
            }
            catch (ClassNotFoundException cnfe)
            {
                log.error("The specified class for ResourceCache (" + cacheClassName +
                          ") does not exist or is not accessible to the current classloader.");
                cacheObject = null;
            }

            if (!(cacheObject instanceof ResourceCache))
            {
                log.error("The specified class for ResourceCache (" + cacheClassName +
                          ") does not implement " + ResourceCache.class.getName() +
                          " ResourceManager. Using default ResourceCache implementation.");
                cacheObject = null;
            }
        }

        /*
         *  if we didn't get through that, just use the default.
         */
        if (cacheObject == null)
        {
            cacheObject = new ResourceCacheImpl();
        }

        globalCache = (ResourceCache) cacheObject;

        globalCache.initialize(rsvc);

        log.trace("Default ResourceManager initialization complete.");
    }

    /**
     * This will produce a List of Hashtables, each hashtable contains the intialization info for a particular resource loader. This
     * Hashtable will be passed in when initializing the the template loader.
     */
    private void assembleResourceLoaderInitializers()
    {
        Vector resourceLoaderNames = rsvc.getConfiguration().getVector(RuntimeConstants.RESOURCE_LOADER);
        StringUtils.trimStrings(resourceLoaderNames);

        for (Iterator it = resourceLoaderNames.iterator(); it.hasNext(); )
        {

            /*
             * The loader id might look something like the following:
             *
             * file.resource.loader
             *
             * The loader id is the prefix used for all properties
             * pertaining to a particular loader.
             */
            String loaderName = (String) it.next();
            StringBuffer loaderID = new StringBuffer(loaderName);
            loaderID.append(".").append(RuntimeConstants.RESOURCE_LOADER);

            ExtendedProperties loaderConfiguration = 
        		rsvc.getConfiguration().subset(loaderID.toString());

            /*
             *  we can't really count on ExtendedProperties to give us an empty set
             */

            if (loaderConfiguration == null)
            {
                log.warn("ResourceManager : No configuration information for resource loader named '" +
                         loaderName + "'. Skipping.");

                continue;
            }

            /*
             *  add the loader name token to the initializer if we need it
             *  for reference later. We can't count on the user to fill
             *  in the 'name' field
             */

            loaderConfiguration.setProperty(RESOURCE_LOADER_IDENTIFIER, loaderName);

            /*
             * Add resources to the list of resource loader
             * initializers.
             */
            sourceInitializerList.add(loaderConfiguration);
        }
    }

    /**
     * Gets the named resource. Returned class type corresponds to specified type (i.e. <code>Template</code> to <code>
     * RESOURCE_TEMPLATE</code>).
     *
     * @param  resourceName  The name of the resource to retrieve.
     * @param  resourceType  The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     * @param  encoding  The character encoding to use.
     *
     * @return  Resource with the template parsed and ready.
     *
     * @throws  ResourceNotFoundException  if template not found from any available source.
     * @throws  ParseErrorException  if template cannot be parsed due to syntax (or other) error.
     * @throws  Exception  if a problem in parse
     */
    public synchronized Resource getResource(final String resourceName, final int resourceType, final String encoding)
        throws ResourceNotFoundException,
            ParseErrorException,
            Exception
    {
        /*
         * Check to see if the resource was placed in the cache.
         * If it was placed in the cache then we will use
         * the cached version of the resource. If not we
         * will load it.
         *
         * Note: the type is included in the key to differentiate ContentResource
         * (static content from #include) with a Template.
         */

        String resourceKey = resourceType + resourceName;
        Resource resource = globalCache.get(resourceKey);

        if (resource != null)
        {
            /*
             *  refresh the resource
             */

            try
            {
                refreshResource(resource, encoding);
            }
            catch (ResourceNotFoundException rnfe)
            {
                /*
                 *  something exceptional happened to that resource
                 *  this could be on purpose,
                 *  so clear the cache and try again
                 */

                globalCache.remove(resourceKey);

                return getResource(resourceName, resourceType, encoding);
            }
            catch (ParseErrorException pee)
            {
                log.error("ResourceManager.getResource() exception", pee);
                throw pee;
            }
            catch (RuntimeException re)
            {
        	throw re;
            }
            catch (Exception e)
            {
                log.error("ResourceManager.getResource() exception", e);
                throw e;
            }
        }
        else
        {
            try
            {
                /*
                 *  it's not in the cache, so load it.
                 */

                resource = loadResource(resourceName, resourceType, encoding);

                if (resource.getResourceLoader().isCachingOn())
                {
                    globalCache.put(resourceKey, resource);
                }
            }
            catch (ResourceNotFoundException rnfe)
            {
                log.error("ResourceManager : unable to find resource '" +
                          resourceName + "' in any resource loader.");
                throw rnfe;
            }
            catch (ParseErrorException pee)
            {
                log.error("ResourceManager.getResource() parse exception", pee);
                throw pee;
            }
            catch (RuntimeException re)
            {
    		throw re;
            }
            catch (Exception e)
            {
                log.error("ResourceManager.getResource() exception new", e);
                throw e;
            }
        }

        return resource;
    }

    /**
     * Loads a resource from the current set of resource loaders.
     *
     * @param  resourceName  The name of the resource to retrieve.
     * @param  resourceType  The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     * @param  encoding  The character encoding to use.
     *
     * @return  Resource with the template parsed and ready.
     *
     * @throws  ResourceNotFoundException  if template not found from any available source.
     * @throws  ParseErrorException  if template cannot be parsed due to syntax (or other) error.
     * @throws  Exception  if a problem in parse
     */
    protected Resource loadResource(String resourceName, int resourceType, String encoding)
        throws ResourceNotFoundException,
            ParseErrorException,
            Exception
    {
        Resource resource = ResourceFactory.getResource(resourceName, resourceType);

        resource.setRuntimeServices(rsvc);

        resource.setName(resourceName);
        resource.setEncoding(encoding);

        /*
         * Now we have to try to find the appropriate
         * loader for this resource. We have to cycle through
         * the list of available resource loaders and see
         * which one gives us a stream that we can use to
         * make a resource with.
         */

        long howOldItWas = 0;

        for (Iterator it = resourceLoaders.iterator(); it.hasNext();)
        {
            ResourceLoader resourceLoader = (ResourceLoader) it.next();
            resource.setResourceLoader(resourceLoader);

            /*
             *  catch the ResourceNotFound exception
             *  as that is ok in our new multi-loader environment
             */

            try
            {

                if (resource.process())
                {
                    /*
                     *  FIXME  (gmj)
                     *  moved in here - technically still
                     *  a problem - but the resource needs to be
                     *  processed before the loader can figure
                     *  it out due to to the new
                     *  multi-path support - will revisit and fix
                     */

                    if (logWhenFound && log.isDebugEnabled())
                    {
                        log.debug("ResourceManager : found " + resourceName +
                                  " with loader " +
                                  resourceLoader.getClassName());
                    }

                    howOldItWas = resourceLoader.getLastModified(resource);

                    break;
                }
            }
            catch (ResourceNotFoundException rnfe)
            {
                /*
                 *  that's ok - it's possible to fail in
                 *  multi-loader environment
                 */
            }
        }

        /*
         * Return null if we can't find a resource.
         */
        if (resource.getData() == null)
        {
            throw new ResourceNotFoundException("Unable to find resource '" + resourceName + "'");
        }

        /*
         *  some final cleanup
         */

        resource.setLastModified(howOldItWas);
        resource.setModificationCheckInterval(resource.getResourceLoader().getModificationCheckInterval());

        resource.touch();

        return resource;
    }

    /**
     * Takes an existing resource, and 'refreshes' it. This generally means that the source of the resource is checked for changes
     * according to some cache/check algorithm and if the resource changed, then the resource data is reloaded and re-parsed.
     *
     * @param  resource  resource to refresh
     * @param  encoding  character encoding of the resource to refresh.
     *
     * @throws  ResourceNotFoundException  if template not found from current source for this Resource
     * @throws  ParseErrorException  if template cannot be parsed due to syntax (or other) error.
     * @throws  Exception  if a problem in parse
     */
    protected void refreshResource(final Resource resource, final String encoding)
        throws ResourceNotFoundException,
            ParseErrorException,
            Exception
    {

        /*
         * The resource knows whether it needs to be checked
         * or not, and the resource's loader can check to
         * see if the source has been modified. If both
         * these conditions are true then we must reload
         * the input stream and parse it to make a new
         * AST for the resource.
         */
        if (resource.requiresChecking())
        {
            /*
             *  touch() the resource to reset the counters
             */

            resource.touch();

            if (resource.isSourceModified())
            {
                /*
                 *  now check encoding info.  It's possible that the newly declared
                 *  encoding is different than the encoding already in the resource
                 *  this strikes me as bad...
                 */

                if (!org.apache.commons.lang.StringUtils.equals(resource.getEncoding(), encoding))
                {
                    log.warn("Declared encoding for template '" +
                             resource.getName() +
                             "' is different on reload. Old = '" +
                             resource.getEncoding() + "' New = '" + encoding);

                    resource.setEncoding(encoding);
                }

                /*
                 *  read how old the resource is _before_
                 *  processing (=>reading) it
                 */
                long howOldItWas = resource.getResourceLoader().getLastModified(resource);

                /*
                 *  read in the fresh stream and parse
                 */

                resource.process();

                /*
                 *  now set the modification info and reset
                 *  the modification check counters
                 */

                resource.setLastModified(howOldItWas);
            }
        }
    }

    /**
     * Gets the named resource. Returned class type corresponds to specified type (i.e. <code>Template</code> to <code>
     * RESOURCE_TEMPLATE</code>).
     *
     * @param  resourceName  The name of the resource to retrieve.
     * @param  resourceType  The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     *
     * @return  Resource with the template parsed and ready.
     *
     * @throws  ResourceNotFoundException  if template not found from any available source.
     * @throws  ParseErrorException  if template cannot be parsed due to syntax (or other) error.
     * @throws  Exception  if a problem in parse
     *
     * @deprecated  Use {@link #getResource(String resourceName, int resourceType, String encoding )}
     */
    public Resource getResource(String resourceName, int resourceType)
        throws ResourceNotFoundException,
            ParseErrorException,
            Exception
    {
        return getResource(resourceName, resourceType, RuntimeConstants.ENCODING_DEFAULT);
    }

    /**
     * Determines if a template exists, and returns name of the loader that provides it. This is a slightly less hokey way to
     * support the Velocity.templateExists() utility method, which was broken when per-template encoding was introduced. We can
     * revisit this.
     *
     * @param  resourceName  Name of template or content resource
     *
     * @return  class name of loader than can provide it
     */
    public String getLoaderNameForResource(String resourceName)
    {
        /*
         *  loop through our loaders...
         */
        for (Iterator it = resourceLoaders.iterator(); it.hasNext(); )
        {
            ResourceLoader resourceLoader = (ResourceLoader) it.next();

            InputStream is = null;

            /*
             *  if we find one that can provide the resource,
             *  return the name of the loaders's Class
             */
            try
            {
                is = resourceLoader.getResourceStream(resourceName);

                if (is != null)
                {
                    return resourceLoader.getClass().toString();
                }
            }
            catch (ResourceNotFoundException rnfe)
            {
                /*
                 * this isn't a problem.  keep going
                 */
            }
            finally
            {

                /*
                 *  if we did find one, clean up because we were
                 *  returned an open stream
                 */
                if (is != null)
                {

                    try
                    {
                        is.close();
                    }
                    catch (IOException supressed)
                    { }
                }
            }
        }

        return null;
    }
}
