package org.apache.velocity.runtime.resource.loader;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;

import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * This adapter is used to wrap multi-path support around other
 * <code>ResourceLoader</code> implementations (such as
 * <code>ClasspathResourceLoader</code> and
 * <code>FileResourceLoader</code>).
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 */
public class PathMapAdapter extends LoaderAdapter
{
    /**
     * The file separator to use when creating paths.
     */
    protected String separator;

    /**
     * The paths to search for templates.
     */
    protected List paths = null;

    /**
     * Template to path mappings.
     */
    protected Map templatePaths = new Hashtable();

    /**
     * Initializes the loader and list of search paths.
     */
    public void init(ExtendedProperties configuration)
    {
        rsvc.info("PathMapAdapter : initialization starting.");

        // Initialize parent class "classLoader" instance member.
        super.initLoader(configuration);

        separator = (resourceLoader instanceof FileResourceLoader ?
                     File.separator : "/");

        paths = configuration.getVector("path");

        int size = paths.size();

        for (int i = 0; i < size; i++)
        {
            rsvc.info("PathMapAdapter : adding path '" +
                      paths.get(i) + "'");
        }
        rsvc.info("PathMapAdapter : initialization complete.");
    }


    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name The name of template to get.
     * @return InputStream containing the template.
     * @throws ResourceNotFoundException If template not found in
     * classpath.
     */
     public synchronized InputStream getResourceStream(String name)
         throws ResourceNotFoundException
     {
         if (name == null || name.length() == 0)
         {
             throw new ResourceNotFoundException("No template name provided");
         }

         InputStream result = null;
         String path = (String) templatePaths.get(name);

         if (path != null)
         {
             try
             {
                 result = openStream( path, name);
             }
             catch (Exception e)
             {
                 throw new ResourceNotFoundException("Couldn't find resource:"
                                                     + e.getMessage());
             }
         }
         else
         {
             int size = paths.size();
             for (int i = 0; i < size; i++)
             {
                 path = (String) paths.get(i);
                 try
                 {
                     rsvc.debug("Looking for " + name + " in path " + path);
                     path = normalizePath(path);
                     rsvc.debug("Normalized path to " + path);
                     result = openStream( path, name);
                     if (result != null)
                     {
                         templatePaths.put(name, path);
                         break;
                     }
                 }
                 catch (Exception ignored)
                 {
                     // Expected to happen for resources not found in
                     // this path.
                 }
             }
         }

         if ( result == null)
         {
            throw new ResourceNotFoundException("PathMapAdapter : " + name +
                                                " not found");
         }

         return result;
     }

     /**
      *  Uses the configured loader to return the stream
      */
     private InputStream openStream(String path, String name)
        throws Exception
     {
         return resourceLoader.getResourceStream(
                 path.length() > 0 ? path + separator + name : name );
     }

     /**
      * Normalizes a path for use with a <code>ClassLoader</code>.
      */
     private String normalizePath(String path)
     {
         if (path.startsWith(separator))
         {
             path = path.substring(1);
         }
         else if (path.startsWith("." + separator))
         {
             path = path.substring(2);
         }
         else if (path.equals("."))
         {
             path = "";
         }
         return path;
     }
}
