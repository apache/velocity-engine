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
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Hashtable;

import org.apache.velocity.runtime.RuntimeServices;

import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * A small wrapper around a Jar
 *
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: JarHolder.java,v 1.8.4.1 2004/03/03 23:23:02 geirm Exp $
 */
public class JarHolder
{
    private String urlpath = null;
    private JarFile theJar = null;
    private JarURLConnection conn = null;
        
    private RuntimeServices rsvc = null;

    public JarHolder( RuntimeServices rs, String urlpath )
    {
        rsvc = rs;

        this.urlpath=urlpath;
        init();
        
        rsvc.info("  JarHolder : initialized JAR: " + urlpath );
    }

    public void init()
    {
        try
        {
            rsvc.info("  JarHolder : attempting to connect to "+ urlpath);
            URL url = new URL( urlpath );
            conn = (JarURLConnection) url.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            theJar = conn.getJarFile();
        } 
        catch (Exception e)
        {
            rsvc.error("  JarHolder : error establishing connection to JAR "+ e);
        }
    }

    public void close() 
    {
        try
        {
            theJar.close();
        }
        catch ( Exception e )
        {
            rsvc.error("  JarHolder : error Closing JAR the file " +  e);
        }
        theJar = null;
        conn = null;

        rsvc.info("  JarHolder : JAR file closed");
    }
    
    public InputStream getResource( String theentry )
     throws ResourceNotFoundException {
        InputStream data = null;
        
        try 
        {
            JarEntry entry = theJar.getJarEntry( theentry );
            
            if ( entry != null )
            {                
                data =  theJar.getInputStream( entry );
            }
        }
        catch( Exception fnfe )
        {
            rsvc.error("  JarHolder : getResource() error : exception : " + fnfe );
            throw new ResourceNotFoundException( fnfe.getMessage() );
        }
        
        return data;
    }

    public Hashtable getEntries()
    {
        Hashtable allEntries = new Hashtable(559);
        
        Enumeration all  = theJar.entries();
        while ( all.hasMoreElements() )
        {
            JarEntry je = (JarEntry)all.nextElement();
            
            // We don't map plain directory entries
            if ( !je.isDirectory() )
            {
                allEntries.put( je.getName(), this.urlpath );   
            }
        }
        return allEntries;
    }
    
    public String getUrlPath()
    {
        return urlpath;
    }
}







