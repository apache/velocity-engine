package org.apache.velocity.runtime.resource;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import java.io.InputStream;
import java.io.IOException;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.runtime.resource.ResourceFactory;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoaderFactory;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Class to manage the text resource for the Velocity
 * Runtime.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ResourceManagerImpl.java,v 1.7.4.1 2004/03/03 23:23:01 geirm Exp $
 */
public class ResourceManagerImpl implements ResourceManager
{
    /**
     * A template resources.
     */
    public static final int RESOURCE_TEMPLATE = 1;
    
    /**
     * A static content resource.
     */
    public static final int RESOURCE_CONTENT = 2;

    /**
     * token used to identify the loader internally
     */
    private static final String RESOURCE_LOADER_IDENTIFIER = "_RESOURCE_LOADER_IDENTIFIER_";

    /**
     *  Object implementing ResourceCache to 
     *  be our resource manager's Resource cache.
     */
    protected ResourceCache globalCache = null;
    
    /**
     * The List of templateLoaders that the Runtime will
     * use to locate the InputStream source of a template.
     */
    protected  ArrayList resourceLoaders = new ArrayList();
    
    /**
     * This is a list of the template input stream source
     * initializers, basically properties for a particular
     * template stream source. The order in this list
     * reflects numbering of the properties i.e.
     *
     * <loader-id>.resource.loader.<property> = <value>
     */
    private  ArrayList sourceInitializerList = new ArrayList();
    
    /**
     * This is a map of public name of the template
     * stream source to it's initializer. This is so
     * that clients of velocity can set properties of
     * a template source stream with its public name.
     * So for example, a client could set the 
     * File.resource.path property and this would
     * change the resource.path property for the
     * file template stream source.
     */
    private  Hashtable sourceInitializerMap = new Hashtable();

    /**
     * Each loader needs a configuration object for
     * its initialization, this flags keeps track of whether
     * or not the configuration objects have been created
     * for the resource loaders.
     */
    private  boolean resourceLoaderInitializersActive = false;

    /**
     *  switch to turn off log notice when a resource is found for
     *  the first time.
     */
    private  boolean logWhenFound = true;

    protected RuntimeServices rsvc = null;

    /**
     * Initialize the ResourceManager.
     */
    public void initialize( RuntimeServices rs ) 
        throws Exception
    {
        rsvc = rs;
        
        rsvc.info("Default ResourceManager initializing. (" + this.getClass() + ")");

        ResourceLoader resourceLoader;
        
        assembleResourceLoaderInitializers();
        
        for (int i = 0; i < sourceInitializerList.size(); i++)
        {
            ExtendedProperties configuration = (ExtendedProperties) sourceInitializerList.get(i);
            String loaderClass = configuration.getString("class");

            if ( loaderClass == null)
            {
                rsvc.error(  "Unable to find '"
                                + configuration.getString(RESOURCE_LOADER_IDENTIFIER)
                                + ".resource.loader.class' specification in configuation."
                                + " This is a critical value.  Please adjust configuration.");
                continue;
            }

            resourceLoader = ResourceLoaderFactory.getLoader( rsvc, loaderClass);
            resourceLoader.commonInit( rsvc, configuration);
            resourceLoader.init(configuration);
            resourceLoaders.add(resourceLoader);

        }

        /*
         * now see if this is overridden by configuration
         */

        logWhenFound = rsvc.getBoolean( RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, true );

        /*
         *  now, is a global cache specified?
         */
         
        String claz = rsvc.getString( RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS );
        
        Object o = null;
        
        if ( claz != null && claz.length() > 0 )
        {
            try
            {
               o = Class.forName( claz ).newInstance();
            }
            catch (ClassNotFoundException cnfe )
            {
                String err = "The specified class for ResourceCache ("
                    + claz    
                    + ") does not exist (or is not accessible to the current classlaoder).";
                 rsvc.error( err );
                 
                 o = null;
            }
            
            if (!(o instanceof ResourceCache) )
            {
                String err = "The specified class for ResourceCache ("
                    + claz 
                    + ") does not implement org.apache.runtime.resource.ResourceCache."
                    + " ResourceManager. Using default ResourceCache implementation.";
                    
                rsvc.error( err);
                
                o = null;
            }
        }
        
        /*
         *  if we didn't get through that, just use the default.
         */
         
        if ( o == null)
            o = new ResourceCacheImpl();
            
         globalCache = (ResourceCache) o;
            
         globalCache.initialize( rsvc );        

         rsvc.info("Default ResourceManager initialization complete.");

        }

    /**
     * This will produce a List of Hashtables, each
     * hashtable contains the intialization info for
     * a particular resource loader. This Hastable
     * will be passed in when initializing the
     * the template loader.
     */
    private void assembleResourceLoaderInitializers()
    {
        if (resourceLoaderInitializersActive)
        {
            return;
        }            

        Vector resourceLoaderNames = 
            rsvc.getConfiguration().getVector(RuntimeConstants.RESOURCE_LOADER);

        for (int i = 0; i < resourceLoaderNames.size(); i++)
        {
            /*
             * The loader id might look something like the following:
             *
             * file.resource.loader
             *
             * The loader id is the prefix used for all properties
             * pertaining to a particular loader.
             */
            String loaderID = 
                resourceLoaderNames.get(i) + "." + RuntimeConstants.RESOURCE_LOADER;

            ExtendedProperties loaderConfiguration =
                rsvc.getConfiguration().subset(loaderID);

            /*
             *  we can't really count on ExtendedProperties to give us an empty set
             */

            if ( loaderConfiguration == null)
            {
                rsvc.warn("ResourceManager : No configuration information for resource loader named '" 
                          + resourceLoaderNames.get(i) + "'. Skipping.");
                continue;
            }

            /*
             *  add the loader name token to the initializer if we need it
             *  for reference later. We can't count on the user to fill
             *  in the 'name' field
             */

            loaderConfiguration.setProperty( RESOURCE_LOADER_IDENTIFIER, resourceLoaderNames.get(i));

            /*
             * Add resources to the list of resource loader
             * initializers.
             */
            sourceInitializerList.add(loaderConfiguration);
        }
        
        resourceLoaderInitializersActive = true;
    }

    /**
     * Gets the named resource.  Returned class type corresponds to specified type
     * (i.e. <code>Template</code> to <code>RESOURCE_TEMPLATE</code>).
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>,
     *                     <code>RESOURCE_CONTENT</code>, etc.).
     * @param encoding  The character encoding to use.
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if a problem in parse
     */
    public Resource getResource(String resourceName, int resourceType, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        /* 
         * Check to see if the resource was placed in the cache.
         * If it was placed in the cache then we will use
         * the cached version of the resource. If not we
         * will load it.
         */
        
        Resource resource = globalCache.get(resourceName);

        if( resource != null)
        {
            /*
             *  refresh the resource
             */
             
            try
            {
                refreshResource( resource, encoding );
            }
            catch( ResourceNotFoundException rnfe )
            {
                /*
                 *  something exceptional happened to that resource
                 *  this could be on purpose, 
                 *  so clear the cache and try again
                 */
                 
                 globalCache.remove( resourceName );
     
                 return getResource( resourceName, resourceType, encoding );
            }
            catch( ParseErrorException pee )
            {
                rsvc.error(
                    "ResourceManager.getResource() exception: " + pee);
                
                throw pee;
            }
            catch( Exception eee )
            {
                rsvc.error(
                    "ResourceManager.getResource() exception: " + eee);
                
                throw eee;
            }
        }
        else
        {            
            try
            {
                /*
                 *  it's not in the cache, so load it.
                 */

                resource = loadResource( resourceName, resourceType, encoding );
                      
                if (resource.getResourceLoader().isCachingOn())
                {
                    globalCache.put(resourceName, resource);
                }                    
            }
            catch( ResourceNotFoundException rnfe2 )
            {
                rsvc.error(
                    "ResourceManager : unable to find resource '" + resourceName + 
                        "' in any resource loader.");
                
                throw rnfe2;
            }
            catch( ParseErrorException pee )
            {
                rsvc.error(
                    "ResourceManager.getResource() parse exception: " + pee);
                
                throw pee;
            }
            catch( Exception ee )
            {
                rsvc.error(
                    "ResourceManager.getResource() exception new: " + ee);

                throw ee;
            }
        }

        return resource;
    }
    
    /**
     *  Loads a resource from the current set of resource loaders
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>,
     *                     <code>RESOURCE_CONTENT</code>, etc.).
     * @param encoding  The character encoding to use.
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if a problem in parse
     */    
    protected Resource loadResource(String resourceName, int resourceType, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        Resource resource = ResourceFactory.getResource(resourceName, resourceType);
                
        resource.setRuntimeServices( rsvc );

        resource.setName( resourceName );
        resource.setEncoding( encoding );
                
        /* 
         * Now we have to try to find the appropriate
         * loader for this resource. We have to cycle through
         * the list of available resource loaders and see
         * which one gives us a stream that we can use to
         * make a resource with.
         */

        long howOldItWas = 0;  // Initialize to avoid warnings

        ResourceLoader resourceLoader = null;

        for (int i = 0; i < resourceLoaders.size(); i++)
        {
            resourceLoader = (ResourceLoader) resourceLoaders.get(i);
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

                     if ( logWhenFound )
                     {
                         rsvc.info("ResourceManager : found " + resourceName + 
                                      " with loader " + resourceLoader.getClassName() );
                     }
   
                     howOldItWas = resourceLoader.getLastModified( resource );
                     break;
                 }
            }
            catch( ResourceNotFoundException rnfe )
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
            throw new ResourceNotFoundException(
                "Unable to find resource '" + resourceName + "'");
        }

        /*
         *  some final cleanup
         */
         
        resource.setLastModified( howOldItWas );
         
        resource.setModificationCheckInterval(
            resourceLoader.getModificationCheckInterval());
        
        resource.touch();
    
        return resource;            
    }
    
    /**
     *  Takes an existing resource, and 'refreshes' it.  This
     *  generally means that the source of the resource is checked
     *  for changes according to some cache/check algorithm
     *  and if the resource changed, then the resource data is
     *  reloaded and re-parsed.
     *
     *  @param resource resource to refresh
     *
     * @throws ResourceNotFoundException if template not found
     *          from current source for this Resource
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if a problem in parse
     */
    protected void refreshResource( Resource resource, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        /* 
         * The resource knows whether it needs to be checked
         * or not, and the resource's loader can check to
         * see if the source has been modified. If both
         * these conditions are true then we must reload
         * the input stream and parse it to make a new
         * AST for the resource.
         */
        if ( resource.requiresChecking() )
        {
            /*
             *  touch() the resource to reset the counters
             */

            resource.touch();
            
            if(  resource.isSourceModified() )
            {
                /*
                 *  now check encoding info.  It's possible that the newly declared
                 *  encoding is different than the encoding already in the resource
                 *  this strikes me as bad...
                 */
                
                if (!resource.getEncoding().equals( encoding ) )
                {
                    rsvc.error("Declared encoding for template '" + resource.getName() 
                                  + "' is different on reload.  Old = '" + resource.getEncoding()
                                  + "'  New = '" + encoding );
    
                    resource.setEncoding( encoding );
                }

                /*
                 *  read how old the resource is _before_
                 *  processing (=>reading) it
                 */
                long howOldItWas = resource.getResourceLoader().getLastModified( resource );

                /*
                 *  read in the fresh stream and parse
                 */

                resource.process();

                /*
                 *  now set the modification info and reset
                 *  the modification check counters
                 */
                
                resource.setLastModified( howOldItWas );
            }
        }
    }

     /**
     * Gets the named resource.  Returned class type corresponds to specified type
     * (i.e. <code>Template</code> to <code>RESOURCE_TEMPLATE</code>).
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>,
     *                     <code>RESOURCE_CONTENT</code>, etc.).
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if a problem in parse
     *
     *  @deprecated Use
     *  {@link #getResource(String resourceName, int resourceType, 
     *          String encoding )}
     */
    public Resource getResource(String resourceName, int resourceType  )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {  
        return getResource( resourceName, resourceType, RuntimeConstants.ENCODING_DEFAULT);
    }

    /**
     *  Determines is a template exists, and returns name of the loader that 
     *  provides it.  This is a slightly less hokey way to support
     *  the Velocity.templateExists() utility method, which was broken
     *  when per-template encoding was introduced.  We can revisit this.
     *
     *  @param resourceName Name of template or content resource
     *  @return class name of loader than can provide it
     */
    public String getLoaderNameForResource(String resourceName )
    {
        ResourceLoader resourceLoader = null;
       
        /*
         *  loop through our loaders...
         */
        for (int i = 0; i < resourceLoaders.size(); i++)
        { 
            resourceLoader = (ResourceLoader) resourceLoaders.get(i);

            InputStream is = null;

            /*
             *  if we find one that can provide the resource,
             *  return the name of the loaders's Class
             */
            try
            {
                is=resourceLoader.getResourceStream( resourceName );
               
                if( is != null)
                {
                    return resourceLoader.getClass().toString();
                }
            }
            catch( ResourceNotFoundException e)
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
                    catch( IOException ioe)
                    {
                    }
                }
            }
        }

        return null;
    }
}


