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

import java.io.InputStream;
import java.io.IOException;

import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.collections.ExtendedProperties;

/**
 * <p>
 * ResourceLoader to load templates from multiple Jar files.
 * </p>
 * <p>
 * The configuration of the JarResourceLoader is straightforward -
 * You simply add the JarResourceLoader to the configuration via
 * </p>
 * <p><pre>
 *    resource.loader = jar
 *    jar.resource.loader.class = org.apache.velocity.runtime.resource.loader.JarResourceLoader
 *    jar.resource.loader.path = list of JAR &lt;URL&gt;s
 * </pre></p>
 *
 * <p> So for example, if you had a jar file on your local filesystem, you could simply do
 *    <pre>
 *    jar.resource.loader.path = jar:file:/opt/myfiles/jar1.jar
 *    </pre>
 * </p>
 * <p> Note that jar specification for the <code>.path</code> configuration property
 * conforms to the same rules for the java.net.JarUrlConnection class.
 * </p>
 *
 * <p> For a working example, see the unit test case, 
 *  org.apache.velocity.test.MultiLoaderTestCase class
 * </p>
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: JarResourceLoader.java,v 1.14 2001/08/07 21:58:18 geirm Exp $
 */
public class JarResourceLoader extends ResourceLoader
{
    /**
     * Maps entries to the parent JAR File
     * Key = the entry *excluding* plain directories
     * Value = the JAR URL
     */
    private Hashtable entryDirectory = new Hashtable(559);
    
    /**
     * Maps JAR URLs to the actual JAR
     * Key = the JAR URL
     * Value = the JAR
     */
    private Hashtable jarfiles = new Hashtable(89);
   
    /**
     * Called by Velocity to initialize the loader
     */
    public void init( ExtendedProperties configuration)
    {
        rsvc.info("JarResourceLoader : initialization starting.");

        Vector paths = configuration.getVector("path");

        /*
         *  support the old version but deprecate with a log message
         */

        if( paths == null || paths.size() == 0)
        {
            paths = configuration.getVector("resource.path");

            if (paths != null && paths.size() > 0)
            {
                rsvc.warn("JarResourceLoader : you are using a deprecated configuration"
                             + " property for the JarResourceLoader -> '<name>.resource.loader.resource.path'."
                             + " Please change to the conventional '<name>.resource.loader.path'.");
            }
        }
                             
        rsvc.info("JarResourceLoader # of paths : " + paths.size() );
        
        for ( int i=0; i<paths.size(); i++ )
        {
            loadJar( (String)paths.get(i) );
        }
        
        rsvc.info("JarResourceLoader : initialization complete.");
    }
    
    private void loadJar( String path )
    {
        rsvc.info("JarResourceLoader : trying to load: " + path);

        // Check path information
        if ( path == null )
        {
            rsvc.error("JarResourceLoader : can not load JAR - JAR path is null");
        }
        if ( !path.startsWith("jar:") )
        {
            rsvc.error("JarResourceLoader : JAR path must start with jar: -> " +
                "see java.net.JarURLConnection for information");
        }
        if ( !path.endsWith("!/") )
        {
            path += "!/";
        }
        
        // Close the jar if it's already open
        // this is useful for a reload
        closeJar( path );
        
        // Create a new JarHolder
        JarHolder temp = new JarHolder( rsvc,  path );
        // Add it's entries to the entryCollection
        addEntries( temp.getEntries() );
        // Add it to the Jar table
        jarfiles.put( temp.getUrlPath(), temp );
    }

    /**
     * Closes a Jar file and set its URLConnection 
     * to null.
     */
    private void closeJar( String path )
    {
        if ( jarfiles.containsKey(path) )
        {
            JarHolder theJar = (JarHolder)jarfiles.get(path);
            theJar.close();
        }
    }
    
    /**
     * Copy all the entries into the entryDirectory
     * It will overwrite any duplicate keys.
     */
    private synchronized void addEntries( Hashtable entries )
    {
        entryDirectory.putAll( entries );
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
    public synchronized InputStream getResourceStream( String source )
        throws ResourceNotFoundException
    {
        InputStream results = null;

        if ( source == null || source.length() == 0)
        {
            throw new ResourceNotFoundException ("Need to a resource!");
        }
        
        String normalizedPath = StringUtils.normalizePath( source );
        
        if ( normalizedPath == null || normalizedPath.length() == 0 )
        {
            String msg = "JAR resource error : argument " + normalizedPath + 
                " contains .. and may be trying to access " + 
                "content outside of template root.  Rejected.";
            
            rsvc.error( "JarResourceLoader : " + msg );
            
            throw new ResourceNotFoundException ( msg );
        }
        
        /*
         *  if a / leads off, then just nip that :)
         */
        if ( normalizedPath.startsWith("/") )
        {
            normalizedPath = normalizedPath.substring(1);
        }
    
        if ( entryDirectory.containsKey( normalizedPath ) )
        {
            String jarurl  = (String)entryDirectory.get( normalizedPath );
            
            if ( jarfiles.containsKey( jarurl ) )
            {
                JarHolder holder = (JarHolder)jarfiles.get( jarurl );
                results =  holder.getResource( normalizedPath );
                return results;
            }
        }
        
        throw new ResourceNotFoundException( "JarResourceLoader Error: cannot find resource " +
          source );

    }
        
        
    // TO DO BELOW 
    // SHOULD BE DELEGATED TO THE JARHOLDER
    public boolean isSourceModified(Resource resource)
    {
        return true;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }
}










