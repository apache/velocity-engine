package org.apache.velocity.runtime.resource;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.configuration.VelocityResources;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoaderFactory;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;


/**
 * Class to manage the text resource for the Velocity
 * Runtime.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: ResourceManager.java,v 1.9 2001/02/26 03:33:19 geirm Exp $
 */
public class ResourceManager
{
    public static final int RESOURCE_TEMPLATE = 1;
    public static final int RESOURCE_CONTENT = 2;

    private static Hashtable globalCache = new Hashtable();
    
    /**
     * The List of templateLoaders that the Runtime will
     * use to locate the InputStream source of a template.
     */
    private static ArrayList resourceLoaders = new ArrayList();
    
    /**
     * This is a list of the template stream source
     * initializers, basically properties for a particular
     * template stream source. The order in this list
     * reflects numbering of the properties i.e.
     * template.loader.1.<property> = <value>
     * template.loader.2.<property> = <value>
     */
    private static ArrayList sourceInitializerList = new ArrayList();
    
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
    private static Hashtable sourceInitializerMap = new Hashtable();

    private static boolean resourceLoaderInitializersActive = false;

    /**
     * Initialize the ResourceManager. It is assumed
     * that assembleSourceInitializers() has been
     * called before this is run.
     */
    public static void initialize() throws Exception
    {
        ResourceLoader resourceLoader;
        
        assembleResourceLoaderInitializers();
        
        for (int i = 0; i < sourceInitializerList.size(); i++)
        {
            Map initializer = (Map) sourceInitializerList.get(i);
            String loaderClass = (String) initializer.get("class");
            resourceLoader = ResourceLoaderFactory.getLoader(loaderClass);
            resourceLoader.commonInit(initializer);
            resourceLoader.init(initializer);
            resourceLoaders.add(resourceLoader);
        }
    }

    /**
     * This will produce a List of Hashtables, each
     * hashtable contains the intialization info for
     * a particular resource loader. This Hastable
     * will be passed in when initializing the
     * the template loader.
     */
    private static void assembleResourceLoaderInitializers()
    {
        if (resourceLoaderInitializersActive)
            return;
        
        for (int i = 0; i < 10; i++)
        {
            String loaderID = "resource.loader." + new Integer(i).toString();
            Enumeration e = VelocityResources.getKeys(loaderID);
            
            if (!e.hasMoreElements())
            {
                continue;
            }
            
            Hashtable sourceInitializer = new Hashtable();
            
            while (e.hasMoreElements())
            {
                String property = (String) e.nextElement();
                String value = VelocityResources.getString(property);
                
                property = property.substring(loaderID.length() + 1);
                sourceInitializer.put(property, value);
                
                /*
                 * Make a Map of the public names for the sources
                 * to the sources property identifier so that external
                 * clients can set source properties. For example:
                 * File.resource.path would get translated into
                 * template.loader.1.resource.path and the translated
                 * name would be used to set the property.
                 */
                if (property.equalsIgnoreCase("public.name"))
                {
                    sourceInitializerMap.put(value.toLowerCase(), sourceInitializer);
                }
            }    
            sourceInitializerList.add(sourceInitializer);
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
     */
    public static Resource getResource(String resourceName, int resourceType)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        Resource resource = null;
        ResourceLoader resourceLoader = null;
        
        /* 
         * Check to see if the resource was placed in the cache.
         * If it was placed in the cache then we will use
         * the cached version of the resource. If not we
         * will load it.
         */
        
        if (globalCache.containsKey(resourceName))
        {
            resource = (Resource) globalCache.get(resourceName);

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
                    try
                    {
                        /*
                         *  read in the fresh stream and parse
                         */

                        resource.process();

                        /*
                         *  now set the modification info and reset
                         *  the modification check counters
                         */
                        
                        resource.setLastModified( 
                            resourceLoader.getLastModified( resource ));               
                    }
                    catch( ResourceNotFoundException rnfe )
                    {
                        Runtime.error("ResourceManager.getResource() exception: " + rnfe);
                        throw rnfe;
                    }
                    catch( ParseErrorException pee )
                    {
                        Runtime.error("ResourceManager.getResource() exception: " + pee);
                        throw pee;
                    }
                    catch( Exception eee )
                    {
                        Runtime.error("ResourceManager.getResource() exception: " + eee);
                        throw eee;
                    }
                }
            }

            return resource;
        }
        else
        {
            /*
             *  it's not in the cache
             */

            try
            {
                resource = ResourceFactory.getResource(resourceName, resourceType);
                resource.setName(resourceName);
                
                /* 
                 * Now we have to try to find the appropriate
                 * loader for this resource. We have to cycle through
                 * the list of available resource loaders and see
                 * which one gives us a stream that we can use to
                 * make a resource with.
                 */
                
                //! Bug this is being run more then once!
                
                for (int i = 0; i < resourceLoaders.size(); i++)
                {
                    resourceLoader = (ResourceLoader) resourceLoaders.get(i);
                    resource.setResourceLoader(resourceLoader);
                    
                    Runtime.info("Attempting to find " + resourceName + 
                        " with " + resourceLoader.getClassName());
                    
                    if (resource.process())
                        break;
                }
                
                /*
                 * Return null if we can't find a resource.
                 */
                if (resource.getData() == null)
                    throw new ResourceNotFoundException("Can't find " + resourceName + "!");
                
                resource.setLastModified(resourceLoader.getLastModified(resource));
                
                resource.setModificationCheckInterval(
                    resourceLoader.getModificationCheckInterval());
                
                resource.touch();
                
                /*
                 * Place the resource in the cache if the resource
                 * loader says to.
                 */
                
                if (resourceLoader.isCachingOn())
                    globalCache.put(resourceName, resource);
            }
            catch( ParseErrorException pee )
            {
                Runtime.error("ResourceManager.getResource() parse exception: " + pee);
                throw pee;
            }
            catch( Exception ee )
            {
                Runtime.error("ResourceManager.getResource() exception: " + ee);
                throw ee;
            }
        }
        return resource;
    }

    /**
     * Allow clients of Velocity to set a template stream
     * source property before the template source streams
     * are initialized. This would for example allow clients
     * to set the template path that would be used by the
     * file template stream source. Right now these properties
     * have to be set before the template stream source is
     * initialized. Maybe we should allow these properties
     * to be changed on the fly.
     *
     * It is assumed that the initializers have been
     * assembled.
     */
    public static void setSourceProperty(String key, String value)
    {
        if (resourceLoaderInitializersActive == false)
            assembleResourceLoaderInitializers();
            
        String publicName = key.substring(0, key.indexOf("."));
        String property = key.substring(key.indexOf(".") + 1);
        ((Map)sourceInitializerMap.get(publicName.toLowerCase())).put(property, value);
    }
}
