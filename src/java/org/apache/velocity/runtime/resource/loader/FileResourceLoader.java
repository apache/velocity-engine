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

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.velocity.util.StringUtils;

import org.apache.velocity.runtime.resource.Resource;

import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * A loader for templates stored on the file system.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: FileResourceLoader.java,v 1.19.4.1 2004/03/03 23:23:02 geirm Exp $
 */
public class FileResourceLoader extends ResourceLoader
{
    /**
     * The paths to search for templates.
     */
    private Vector paths = null;

    /**
     * Used to map the path that a template was found on
     * so that we can properly check the modification
     * times of the files.
     */
    private Hashtable templatePaths = new Hashtable();

    public void init( ExtendedProperties configuration)
    {
        rsvc.info("FileResourceLoader : initialization starting.");
        
        paths = configuration.getVector("path");
        
        /*
         *  lets tell people what paths we will be using
         */

        int sz = paths.size();

        for( int i=0; i < sz; i++)
        {
            rsvc.info("FileResourceLoader : adding path '" + (String) paths.get(i) + "'");
        }

        rsvc.info("FileResourceLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to get
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     */
    public synchronized InputStream getResourceStream(String templateName)
        throws ResourceNotFoundException
    {
        /*
         * Make sure we have a valid templateName.
         */
        if (templateName == null || templateName.length() == 0)
        {
            /*
             * If we don't get a properly formed templateName then
             * there's not much we can do. So we'll forget about
             * trying to search any more paths for the template.
             */
            throw new ResourceNotFoundException(
                "Need to specify a file name or file path!");
        }

        String template = StringUtils.normalizePath(templateName);
        if ( template == null || template.length() == 0 )
        {
            String msg = "File resource error : argument " + template + 
                " contains .. and may be trying to access " + 
                "content outside of template root.  Rejected.";

            rsvc.error( "FileResourceLoader : " + msg );
      
            throw new ResourceNotFoundException ( msg );
        }

        /*
         *  if a / leads off, then just nip that :)
         */
        if (template.startsWith("/"))
        {
            template = template.substring(1);
        }

        int size = paths.size();
        for (int i = 0; i < size; i++)
        {
            String path = (String) paths.get(i);
            InputStream inputStream = findTemplate(path, template);
            
            if (inputStream != null)
            {
                /*
                 * Store the path that this template came
                 * from so that we can check its modification
                 * time.
                 */

                templatePaths.put(templateName, path);
                return inputStream;
            }                
        }
    
        /*
         * We have now searched all the paths for
         * templates and we didn't find anything so
         * throw an exception.
         */
         String msg = "FileResourceLoader Error: cannot find resource " +
          template;
    
         throw new ResourceNotFoundException( msg );
    }
    
    /**
     * Try to find a template given a normalized path.
     * 
     * @param String a normalized path
     * @return InputStream input stream that will be parsed
     *
     */
    private InputStream findTemplate(String path, String template)
    {
        try 
        {
            File file = new File( path, template );   
        
            if ( file.canRead() )
            {
                return new BufferedInputStream(
                    new FileInputStream(file.getAbsolutePath()));
            }
            else
            {                
                return null;
            }                
        }
        catch( FileNotFoundException fnfe )
        {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */
            return null;
        }
    }
    
    /**
     * How to keep track of all the modified times
     * across the paths.  Note that a file might have
     * appeared in a directory which is earlier in the
     * path; so we should search the path and see if
     * the file we find that way is the same as the one
     * that we have cached.
     */
    public boolean isSourceModified(Resource resource)
    {
        /*
         * we assume that the file needs to be reloaded; 
         * if we find the original file and it's unchanged,
         * then we'll flip this.
         */
        boolean modified = true;

        String fileName = resource.getName();
        String path = (String) templatePaths.get(fileName);
        File currentFile = null;

        for (int i = 0; currentFile == null && i < paths.size(); i++)
        {
            String testPath = (String) paths.get(i);
            File testFile = new File(testPath, fileName);
            if (testFile.canRead())
            {
                currentFile = testFile;
            }
        }
        File file = new File(path, fileName);
        if (currentFile == null || !file.exists())
        {
            /*
             * noop: if the file is missing now (either the cached
             * file is gone, or the file can no longer be found)
             * then we leave modified alone (it's set to true); a 
             * reload attempt will be done, which will either use
             * a new template or fail with an appropriate message
             * about how the file couldn't be found.
             */
        }
        else if (currentFile.equals(file) && file.canRead())
        {
            /*
             * if only if currentFile is the same as file and
             * file.lastModified() is the same as
             * resource.getLastModified(), then we should use the
             * cached version.
             */
            modified = (file.lastModified() != resource.getLastModified());
        }

        /*
         * rsvc.debug("isSourceModified for " + fileName + ": " + modified);
         */
        return modified;
    }

    public long getLastModified(Resource resource)
    {
        String path = (String) templatePaths.get(resource.getName());
        File file = new File(path, resource.getName());

        if (file.canRead())
        {
            return file.lastModified();
        }            
        else
        {
            return 0;
        }            
    }
}
