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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Hashtable;

import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;

import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * A small wrapper around a Jar
 *
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: JarHolder.java,v 1.7 2001/08/07 21:58:18 geirm Exp $
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







