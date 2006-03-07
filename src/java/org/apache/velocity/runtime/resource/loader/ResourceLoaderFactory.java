package org.apache.velocity.runtime.resource.loader;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.StringUtils;

/**
 * Factory to grab a template loader.
 * 
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ResourceLoaderFactory
{
    /**
     * Gets the loader specified in the configuration file.
     * @return TemplateLoader
     */
    public static ResourceLoader getLoader(RuntimeServices rs, String loaderClassName)
     throws Exception
    {
        ResourceLoader loader = null;
        
        try
        {
            loader = (ResourceLoader) ClassUtils.getNewInstance( loaderClassName );
            
            rs.getLog().debug("ResourceLoader instantiated: "
                              + loader.getClass().getName());
            
            return loader;
        }
        // The ugly three strike again: ClassNotFoundException,IllegalAccessException,InstantiationException
        catch(Exception e)
        {
            rs.getLog().error("Problem instantiating the template loader.\n" +
                          "Look at your properties file and make sure the\n" +
                          "name of the template loader is correct. Here is the\n" +
                          "error:", e);
            
            throw new Exception("Problem initializing template loader: " + loaderClassName + 
            "\nError is: " + StringUtils.stackTrace(e));
        }
    }
}
