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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.util.StringResource;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;
import org.apache.velocity.util.ClassUtils;

/**
 * Resource loader that works with Strings. Users should manually add
 * resources to the repository that is know by the factory of this package.
 *
 * Below is an example configuration for this loader.
 * Note that 'repositoryimpl' is not mandatory;
 * if not provided, the factory will fall back on using the default
 * implementation of this package.
 *
 * string.resource.loader.description = Velocity StringResource loader
 * string.resource.loader.class = org.apache.velocity.runtime.resource.loader..StringResourceLoader
 * string.resource.loader.repository.class = org.apache.velocity.runtime.resource.loader.StringResourceRepositoryImpl
 *
 * Resources can be added to the repository like this:
 * <code>
 *   StringResourceRepository = StringResourceLoader.getRepository();
 *
 *   String myTemplateName = "/somewhere/intherepo/name";
 *   String myTemplateBody = "Hi, ${username}... this is a some template!";
 *   vsRepository.putStringResource(myTemplateName, myTemplateBody);
 * </code>
 *
 * After this, the templates can be retrieved as usual.
 *
 * @author <a href="mailto:eelco.hillenius@openedge.nl">Eelco Hillenius</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class StringResourceLoader extends ResourceLoader
{
    /** Key to look up the repository implementation class. */
    public static final String REPOSITORY_CLASS = "repository.class";

    /** The default implementation class. */
    public static final String REPOSITORY_CLASS_DEFAULT = StringResourceRepositoryImpl.class.getName();

    /** Key to look up the repository char encoding. */
    public static final String REPOSITORY_ENCODING = "repository.encoding";

    /** The default repository encoding. */
    public static final String REPOSITORY_ENCODING_DEFAULT = "UTF-8";

    /**
     * Returns a reference to the Repository.
     *
     * @return A StringResourceRepository Reference.
     */
    public static StringResourceRepository getRepository()
    {
        return RepositoryFactory.getRepository();
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
     */
    public void init(final ExtendedProperties configuration)
    {
        log.info("StringResourceLoader : initialization starting.");

        String repositoryClass = configuration.getString(REPOSITORY_CLASS, REPOSITORY_CLASS_DEFAULT);
        String encoding = configuration.getString(REPOSITORY_ENCODING, REPOSITORY_ENCODING_DEFAULT);

        RepositoryFactory.setRepositoryClass(repositoryClass);
        RepositoryFactory.setEncoding(encoding);
        RepositoryFactory.init(log);

        log.info("StringResourceLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to get.
     * @return InputStream containing the template.
     * @throws ResourceNotFoundException Ff template not found
     *         in the RepositoryFactory.
     */
    public InputStream getResourceStream(final String name)
            throws ResourceNotFoundException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException("No template name provided");
        }

        StringResource resource = getRepository().getStringResource(name);
        
        if(resource == null)
        {
            throw new ResourceNotFoundException("Could not locate resource '" + name + "'");
        }
        
        byte [] byteArray = null;
    	
        try
        {
            byteArray = resource.getBody().getBytes(resource.getEncoding());
            return new ByteArrayInputStream(byteArray);
        }
        catch(UnsupportedEncodingException ue)
        {
            throw new VelocityException("Could not convert String using encoding " + resource.getEncoding(), ue);
        }
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
     */
    public boolean isSourceModified(final Resource resource)
    {
        StringResource original = null;
        boolean result = true;

        original = getRepository().getStringResource(resource.getName());

        if (original != null)
        {
            result =  original.getLastModified() != resource.getLastModified();
        }

        return result;
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    public long getLastModified(final Resource resource)
    {
        StringResource original = null;

        original = getRepository().getStringResource(resource.getName());

        return (original != null)
                ? original.getLastModified()
                : 0;
    }


    /**
     * Factory for constructing and obtaining the instance of
     * StringResourceRepository implementation.
     *
     * Users can provide their own implementation by setting the property 'repository.class'
     * for the resource loader. Note that at this time only one instance of a
     * string resource repository can be used in a single VM.
     *
     * @author <a href="mailto:eelco.hillenius@openedge.nl">Eelco Hillenius</a>
     * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
     * @version $Id$
     */
    private static final class RepositoryFactory
    {
        /**
         * is the factory initialised properly?
         */
        private static boolean isInitialized = false;

        /**
         * repository instance
         */
        private static StringResourceRepository repository = null;

        /**
         * Sets the repository class.
         *
         * @param className class that implements StringResourceRepository.
         */
        public static void setRepositoryClass(final String className)
        {
            if (isInitialized)
            {
                throw new IllegalStateException("The RepositoryFactory has already been initialized!");
            }

            try
            {
                repository = (StringResourceRepository) ClassUtils.getNewInstance(className);
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new VelocityException("Could not find '" + className + "'", cnfe);
            }
            catch (IllegalAccessException iae)
            {
                throw new VelocityException("Could not access '" + className + "'", iae);
            }
            catch (InstantiationException ie)
            {
                throw new VelocityException("Could not instantiante '" + className + "'", ie);
            }
        }

        /**
         * Sets the current repository encoding.
         *
         * @param encoding The current repository encoding.
         */
        public static void setEncoding(final String encoding)
        {
            if (repository == null)
            {
                throw new IllegalStateException("The Repository class has not yet been set!");
            }

            repository.setEncoding(encoding);
        }

        /**
         * Init the factory with the user given class name.
         *
         * @throws VelocityException If something goes wrong.
         */
        public static synchronized void init(final Log log)
                throws VelocityException
        {
            if (isInitialized)
            {
                throw new IllegalStateException("Attempted to re-initialize Factory!");
            }

            if (log.isInfoEnabled())
            {
                log.info("Using " + repository.getClass().getName() + " as repository implementation");
                log.info("Current repository encoding is " + repository.getEncoding());
            }
            isInitialized = true;
        }

        /**
         * Get a reference to the repository.
         * @return A StringResourceRepository implementation object.
         */
        public static StringResourceRepository getRepository()
        {
            if(!isInitialized)
            {
                throw new IllegalStateException(
                        "RepositoryFactory was not properly set up");
            }
            return repository;
        }
    }
}

