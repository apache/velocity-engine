package org.apache.velocity.runtime.resource;

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
import java.io.Writer;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

/**
 * This class represent a general text resource that
 * may have been retrieved from any number of possible
 * sources.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Resource.java,v 1.8 2001/08/07 21:58:18 geirm Exp $
 */
public abstract class Resource
{
    protected RuntimeServices rsvc = null;

    /**
     * The template loader that initially loaded the input
     * stream for this template, and knows how to check the
     * source of the input stream for modification.
     */
    protected ResourceLoader resourceLoader;

    /**
     * The number of milliseconds in a minute, used to calculate the
     * check interval.
     */
    protected static final long MILLIS_PER_SECOND =  1000;

    /**
     * How often the file modification time is checked (in milliseconds).
     */
    protected long modificationCheckInterval = 0;

    /**
     * The file modification time (in milliseconds) for the cached template.
     */
    protected long lastModified = 0;

    /**
     * The next time the file modification time will be checked (in 
     * milliseconds).
     */
    protected long nextCheck = 0;

    /**
     *  Name of the resource
     */
    protected String name;

    /**
     *  Character encoding of this resource
     */
    protected String encoding = RuntimeConstants.ENCODING_DEFAULT;

    /** 
     *  Resource might require ancillary storage of some kind 
     */
    protected Object data = null;

    /** 
     *  Default constructor 
     */
    public Resource()
    {
    }

    public void setRuntimeServices( RuntimeServices rs )
    {
        rsvc = rs;
    }

    /**
     * Perform any subsequent processing that might need
     * to be done by a resource. In the case of a template
     * the actual parsing of the input stream needs to be
     * performed.
     */
    public abstract boolean process() 
        throws ResourceNotFoundException, ParseErrorException, Exception;

    public boolean isSourceModified()
    {
        return resourceLoader.isSourceModified(this);
    }

    /**
     * Set the modification check interval.
     * @param interval The interval (in minutes).
     */
    public void setModificationCheckInterval(long modificationCheckInterval)
    {
        this.modificationCheckInterval = modificationCheckInterval;
    }
    
    /**
     * Is it time to check to see if the resource
     * source has been updated?
     */
    public boolean requiresChecking()
    {
        /*
         *  short circuit this if modificationCheckInterval == 0
         *  as this means "don't check"
         */
        
        if (modificationCheckInterval <= 0 )
        {
           return false;
        }

        /*
         *  see if we need to check now
         */

        return ( System.currentTimeMillis() >= nextCheck );
    }

    /**
     * 'Touch' this template and thereby resetting
     * the nextCheck field.
     */
    public void touch()
    {
        nextCheck = System.currentTimeMillis() + ( MILLIS_PER_SECOND *  modificationCheckInterval);
    }
    
    /**
     * Set the name of this resource, for example
     * test.vm.
     */
    public void setName(String name)
    {
        this.name = name;
    }        

    /**
     * Get the name of this template.
     */
    public String getName()
    {
        return name;
    }        

    /**
     *  set the encoding of this resource
     *  for example, "ISO-8859-1"
     */
    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    /**
     *  get the encoding of this resource
     *  for example, "ISO-8859-1"
     */
    public String getEncoding()
    {
        return encoding;
    }


    /**
     * Return the lastModifed time of this
     * template.
     */
    public long getLastModified()
    {
        return lastModified;
    }        
    
    /**
     * Set the last modified time for this
     * template.
     */
    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }        

    /**
     * Return the template loader that pulled
     * in the template stream
     */
    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }
    
    /**
     * Set the template loader for this template. Set
     * when the Runtime determines where this template
     * came from the list of possible sources.
     */
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }        

    /** 
     * Set arbitrary data object that might be used
     * by the resource.
     */
    public void setData(Object data)
    {
        this.data = data;
    }
    
    /**
     * Get arbitrary data object that might be used
     * by the resource.
     */
    public Object getData()
    {
        return data;
    }        
}
