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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
 * @version $Id: FileResourceLoader.java,v 1.19 2003/05/04 17:37:41 geirm Exp $
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
