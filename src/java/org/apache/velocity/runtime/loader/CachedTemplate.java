package org.apache.velocity.runtime.loader;

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
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.velocity.Template;

/**
 * This class is used to wrap a pre-parsed template.
 * It also contains additional logic to help reduce
 * disk I/O while at the same time allowing updates to 
 * templates if desired.
 * 
 * @author Dave Bryson
 * @author <a href="mailto:dlr@collab.net">Daneil Rall</a>
 * $Revision: 1.1 $
 */
public class CachedTemplate
{
    /**
     * The number of milliseconds in a minute, used to calculate the
     * check interval.
     */
    protected static final long MILLIS_PER_MINUTE = 60 * 1000;

    /**
     * The cached template.
     */
    private Template template = null;

    /**
     * The file in which the cached template lives.
     */
    private File file = null;

    /**
     * The file modification time (in milliseconds) for the cached template.
     */
    private long lastmodified = 0;

    /**
     * How often the file modification time is checked (in milliseconds).
     */
    private long checkInterval = 0;

    /**
     * The last time the file modification time was checked (in milliseconds).
     */
    private long lastcheck = 0;

    /**
     * The next time the file modification time will be checked (in 
     * milliseconds).
     */
    private long nextcheck = 0;
    
    /**
     * Constructs a cached template.
     * @param file The template file.
     */
    public CachedTemplate( File file )
    {
        this.file=file;
    }
    
    /**
     * Check to see if the template is still valid
     * according to our rules.
     * @return Whether or not the cached template is still valid.
     */
    public boolean isValid() throws IOException
    {
        // If the file is null, no need
        // to continue. We'll load it again
        if ( file == null )
        {
            return false;
        }
        
        // If the time interval that we defined
        // in the props file has expired, then we need 
        // to do some additional checks on the file.
        if ( lastcheck >= nextcheck )
        {
            // Does the file still exist?
            // if not, no need to continue.
            if ( !file.exists() )
            {
                return false;
            }
           
            // Check to see if the file has been modified since
            // we last cached it.
            if ( lastmodified != file.lastModified() )
            {
                // reload since it has changed on disk.
                reload();
            }
        }
        else
        {
            // update the last check time.
            lastcheck=System.currentTimeMillis(); 
        }
        
        return true;
    }
    
    /**
     * Gets the interval to wait before checking for file modification
     * (in minutes).
     * @return The interval to wait before checking for file modification.
     */
    public long getInterval()
    {
        return (checkInterval / MILLIS_PER_MINUTE);
    }

    /**
     * Set the modification check interval.
     * @param interval The interval (in minutes).
     */
    public void setInterval( long interval )
    {
        // Convert provided interval to milliseconds for internal storage.
        this.checkInterval=interval * MILLIS_PER_MINUTE;
    }

    /**
     * Set the file in which the template lives.
     * @param f The file for the template.
     */
    public void setFile( File f )
    {
        this.file=file;
    }

    /**
     * Return the pre-parsed template.
     * @return The cached template.
     */
    public Template getTemplate()
    {
        return template;
    }
    
    /**
     * Set the template.
     * @param t The template.
     */
    public void setTemplate( Template t)
    {
        this.template=t;
    }
    
    /**
     * Reload the file from disk.
     */
    private void reload()
    {
        if ( file.canRead() )
        {
            try
            {
                template = new Template( new FileInputStream(file.getAbsolutePath()));
                //template.parse();
                lastcheck=System.currentTimeMillis();
                nextcheck= lastcheck+checkInterval;
            }
            catch (Exception e)
            {
            }
        }
        
    }
}






