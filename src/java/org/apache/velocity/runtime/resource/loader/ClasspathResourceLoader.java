package org.apache.velocity.runtime.resource.loader;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 
import java.io.InputStream;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

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
 *  resources accessable to the classloader.
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
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ClasspathResourceLoader.java,v 1.8.4.1 2004/03/03 23:23:02 geirm Exp $
 */
public class ClasspathResourceLoader extends ResourceLoader
{

    /**
     *  This is abstract in the base class, so we need it
     */
    public void init( ExtendedProperties configuration)
    {
        rsvc.info("ClasspathResourceLoader : initialization starting.");
        rsvc.info("ClasspathResourceLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to get
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in  classpath.
     */
    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        InputStream result = null;
        
        if (name == null || name.length() == 0)
        {
            throw new ResourceNotFoundException ("No template name provided");
        }
        
        try 
        {
            ClassLoader classLoader = this.getClass().getClassLoader();
            result= classLoader.getResourceAsStream( name );
        }
        catch( Exception fnfe )
        {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */
            
            throw new ResourceNotFoundException( fnfe.getMessage() );
        }
        
        return result;
    }
    
    /**
     * Defaults to return false.
     */
    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    /**
     * Defaults to return 0
     */
    public long getLastModified(Resource resource)
    {
        return 0;
    }
}

