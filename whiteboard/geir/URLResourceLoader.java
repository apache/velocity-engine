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

import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import java.util.Vector;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This is a simple URL-based loader.
 * 
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id: URLResourceLoader.java,v 1.1.8.1 2004/03/04 00:18:30 geirm Exp $
 */
public class URLResourceLoader extends ResourceLoader
{
    private Vector urlroots = new Vector();

    public void init( ExtendedProperties configuration)
    {
        rsvc.info("URLResourceLoader : initialization starting.");

        urlroots = configuration.getVector("root");
       
        for( int i=0; i < urlroots.size(); i++)
        {
            rsvc.info("URLResourceLoader : adding root '" + (String) urlroots.get(i) + "'");
        }        
 
        rsvc.info("URLResourceLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param url  url of template to fetch bytestream of
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     */
    public synchronized InputStream getResourceStream( String url )
        throws ResourceNotFoundException
    {
        for( int i=0; i < urlroots.size(); i++)
        {
            try
            {
                String full = (String) urlroots.get( i );
                
                URL u = new URL( full + url );
                URLConnection conn = u.openConnection();
                InputStream inputStream = conn.getInputStream();
                
                if (inputStream != null)
                {
                    return inputStream;
                }
            }
            catch( Exception e )
            {
                /*
                 *  for now, just ignore.  we can prollie do 
                 *   something better here..
                 */
            }
        }

        String msg = "URLResourceLoader :  Error: cannot find resource " +
            url;
       
        rsvc.error( "URLResourceLoader : " + msg );

        throw new ResourceNotFoundException( msg );
    }
    
    /**
     * How to keep track of all the modified times
     * across the paths.
     */
    public boolean isSourceModified(Resource resource)
    {
        return true;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }
}
