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
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.ExtProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 *  ClasspathResourceLoader is a simple loader that will load
 *  templates from the classpath.
 *  <br>
 *  <br>
 *  Will load templates from  from multiple instances of
 *  and arbitrary combinations of :
 *  <ul>
 *  <li> jar files
 *  <li> zip files
 *  <li> template directories (any directory containing templates)
 *  </ul>
 *  This is a configuration-free loader, in that there are no
 *  parameters to be specified in the configuration properties,
 *  other than specifying this as the loader to use.  For example
 *  the following is all that the loader needs to be functional :
 *  <br>
 *  <br>
 *  resource.loader = class
 *  class.resource.loader.class =
 *    org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
 *  <br>
 *  <br>
 *  To use, put your template directories, jars
 *  and zip files into the classpath or other mechanisms that make
 *  resources accessible to the classloader.
 *  <br>
 *  <br>
 *  This makes deployment trivial for web applications running in
 *  any Servlet 2.2 compliant servlet runner, such as Tomcat 3.2
 *  and others.
 *  <br>
 *  <br>
 *  For a Servlet Spec v2.2 servlet runner,
 *  just drop the jars of template files into the WEB-INF/lib
 *  directory of your webapp, and you won't have to worry about setting
 *  template paths or altering them with the root of the webapp
 *  before initializing.
 *  <br>
 *  <br>
 *  I have also tried it with a WAR deployment, and that seemed to
 *  work just fine.
 *
 * @author <a href="mailto:mailmur@yahoo.com">Aki Nieminen</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ClasspathResourceLoader extends ResourceLoader
{

    /**
     *  This is abstract in the base class, so we need it
     * @param configuration
     */
    public void init( ExtProperties configuration)
    {
        if (log.isTraceEnabled())
        {
            log.trace("ClasspathResourceLoader : initialization complete.");
        }
    }

    /**
     * Get a Reader so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to get
     * @param encoding asked encoding
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in  classpath.
     * @since 2.0
     */
    public Reader getResourceReader( String name, String encoding )
            throws ResourceNotFoundException
    {
        Reader result = null;

        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException ("No template name provided");
        }

        /**
         * look for resource in thread classloader first (e.g. WEB-INF\lib in
         * a servlet container) then fall back to the system classloader.
         */

        InputStream rawStream = null;
        try
        {
            rawStream = ClassUtils.getResourceAsStream( getClass(), name );
            if (rawStream != null)
            {
                result = buildReader(rawStream, encoding);
            }
        }
        catch( Exception fnfe )
        {
            if (rawStream != null)
            {
                try
                {
                    rawStream.close();
                }
                catch (IOException ioe) {}
            }
            throw new ResourceNotFoundException("ClasspathResourceLoader problem with template: " + name, fnfe );
        }

        if (result == null)
        {
            String msg = "ClasspathResourceLoader Error: cannot find resource " + name;

            throw new ResourceNotFoundException( msg );
        }

        return result;
    }

    /**
     * @see ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
     */
    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    /**
     * @see ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    public long getLastModified(Resource resource)
    {
        return 0;
    }
}

