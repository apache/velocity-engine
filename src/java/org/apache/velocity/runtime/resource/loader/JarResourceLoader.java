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
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.configuration.VelocityResources;
import org.apache.velocity.runtime.resource.Resource;

import org.apache.velocity.exception.ResourceNotFoundException;


/**
 * Loader to grab templates from a Jar file.
 * 
 * Adopted from Craig R. McClanahan JarResources.java in Catalina and
 * Jason Van Zyl's FileResourceLoader.java
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * $Revision: 1.2 $
 */
public class JarResourceLoader extends ResourceLoader
{
    /**
     * The URLConnection to our JAR file.
     */
    protected JarURLConnection conn = null;

    /**
     * The JarFile object associated with our document base.
     */
    protected JarFile jarFile = null;
    
    /*
     * This should probably be moved into the super class,
     * the stand init stuff. For the properties that all
     * loaders will probably share.
     * @param initialize the Map of paths to load
     */
    public void init(Map initializer)
    {
        String path = (String) initializer.get("resource.path");
        
        setJarUrl( path );
        
        Runtime.info("Resources Loaded From: " +  path);
        Runtime.info("JarResourceLoader Initialized.");
    }

    /**
     * Setup Jar URL 
     */
    private void setJarUrl( String jarPath )
    {
        // Validate the format of the proposed document root
        if ( jarPath == null )
        {
            throw new IllegalArgumentException( "JarPath is NULL" );
        }
        if ( !jarPath.startsWith("jar:") )
        {
            throw new IllegalArgumentException( "JarPath must start with - jar: - see java.net.JarURLConnection" );
        }
        if ( !jarPath.endsWith("!/") )
        {
            jarPath += "!/";
        }
        // Close any previous JAR that we have opened
        if ( jarFile != null ) 
        {
            try
            {
                jarFile.close();
            }
            catch (IOException e)
            {
                Runtime.error("Error Closing JAR file " +  e);
            }
            
            jarFile = null;
            conn = null;
        }
        
        // Open a URLConnection to the specified JAR file
        try
        {
            URL url = new URL( jarPath );
            conn = (JarURLConnection) url.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            jarFile = conn.getJarFile();

            //loop through Jar file and cache entries here
        } 
        catch (Exception e)
        {
            Runtime.error("Error establishing connection to JAR "+ e);
        }
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
    public synchronized InputStream getResourceStream( String name )
        throws ResourceNotFoundException
    {
        InputStream result = null;
        
        if (name == null || name.length() == 0)
        {
            throw new ResourceNotFoundException ("Need to a resource!");
        }
        
        String normalizedPath = StringUtils.normalizePath(name);
        if ( normalizedPath == null || normalizedPath.length() == 0 )
        {
            String msg = "File resource error : argument " + normalizedPath + 
                " contains .. and may be trying to access " + 
                "content outside of template root.  Rejected.";
            
            Runtime.error( "FileResourceLoader : " + msg );
            
            throw new ResourceNotFoundException ( msg );
        }
        
        /*
         *  if a / leads off, then just nip that :)
         */
        if ( normalizedPath.startsWith("/") )
        {
            normalizedPath = normalizedPath.substring(1);
        }
        
        try 
        {
            JarEntry entry = jarFile.getJarEntry( normalizedPath );
            if (entry == null)
            {
                Runtime.error("Resource Not Found" );
            }
            else
            {
                
                result =  jarFile.getInputStream(entry);
            }
        }
        catch( Exception fnfe )
        {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */
            
            Runtime.error("FileResourceLoader Error : exception : " + fnfe );
            throw new ResourceNotFoundException( fnfe.getMessage() );
        }
        
        return result;
    }
    
    
    
    public boolean isSourceModified(Resource resource)
    {
        return true;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }
}










