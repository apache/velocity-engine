package org.apache.velocity.runtime.resource.loader;

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

import java.util.Map;
import java.util.Hashtable;

import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.resource.Resource;


/**
 * This is a simple template file loader.
 * Currently it only supports a  single path to templates.
 * That'll change once we decide how we want to do configuration
 * 
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * $Revision: 1.1 $
 */
public class FileResourceLoader extends ResourceLoader
{
    private String path;

    /*
     * This should probably be moved into the super class,
     * the stand init stuff. For the properties that all
     * loaders will probably share.
     */
    public void init(Map initializer)
    {
        path = (String) initializer.get("resource.path");
        Runtime.info("Resources Loaded From: " +  new File(path).getAbsolutePath());
        Runtime.info("Resource Loader Initialized.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     */
    public synchronized InputStream getResourceStream( String name )
        throws Exception
    {
        if (name == null || name.length() == 0)
        {
            throw new Exception ("Need to specify a file name or file path!");
        }

        String normalizedPath = StringUtils.normalizePath(name);
        if ( normalizedPath == null || normalizedPath.length() == 0 )
        {
            Runtime.error( "File resource error : argument " + normalizedPath + 
                " contains .. and may be trying to access " + 
                "content outside of template root.  Rejected." );
            
            return null;
        }

        /*
         *  if a / leads off, then just nip that :)
         */
        if ( normalizedPath.startsWith("/") )
            normalizedPath = normalizedPath.substring(1);

        File file = new File( path, normalizedPath );           
        if ( file.canRead() )
        {
            return new BufferedInputStream(
                new FileInputStream(file.getAbsolutePath()));
        }
        
        return null;
    }

    public boolean isSourceModified(Resource resource)
    {
        File file = new File( path, resource.getName() );           
        
        if ( file.canRead() )
        {
            if (file.lastModified() != resource.getLastModified())
                return true;
            else
                return false;
        }
        
        // If the file is now unreadable, or it has
        // just plain disappeared then we'll just say
        // that it's modified :-) When the loader attempts
        // to load the stream it will fail and the error
        // will be reported then.
        
        return true;
    }

    public long getLastModified(Resource resource)
    {
        File file = new File(path, resource.getName());
    
        if (file.canRead())
            return file.lastModified();
        else
            return 0;
    }
}
