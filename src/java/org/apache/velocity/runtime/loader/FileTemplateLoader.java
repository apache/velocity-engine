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
import java.io.BufferedInputStream;

import java.util.Hashtable;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.Runtime;

/**
 * This is a simple template file loader.
 * Currently it only supports a  single path to templates.
 * That'll change once we decide how we want to do configuration
 * 
 * @author Dave Bryson
 * $Revision: 1.7 $
 */
public class FileTemplateLoader extends TemplateLoader
{
    private Hashtable cache;
    private String templatepath;
    private boolean useCache;
    private long checkInterval;

    /**
     * Get the properties needed.
     */
    public void init()
    {
        templatepath = Runtime.getString(
            Runtime.TEMPLATE_PATH);
        
        Runtime.info("Template loader path: " + new File(templatepath).getAbsolutePath() );
        
        useCache = Runtime.getBoolean(
            Runtime.TEMPLATE_CACHE );

        Runtime.info("Template caching: " + useCache );
        
        checkInterval = new Long(Runtime.getString(
            Runtime.TEMPLATE_MOD_CHECK_INTERVAL)).longValue();

        if ( useCache )
        {
            cache = new Hashtable();
        }
        
        Runtime.info("Template loader initialized.");
    }
    
    /**
     * Fetch the template
     * @return Template
     */
    public synchronized Template getTemplate( String name )
     throws Exception
    {
        if (name == null || name.length() == 0)
        {
            throw new Exception ("Need to specify a file name or file path!");
        }
        if ( useCache )
        {
            if ( cache.containsKey( name ) )
            {
                CachedTemplate ct = (CachedTemplate)cache.get( name );
                
                if ( ct.isValid() )
                {
                    return ct.getTemplate();
                }
                else
                {
                    //remove from cache
                    ct.setFile( null );
                    ct.setTemplate( null );
                    cache.remove( name );
                }
                    
            }
        }

        File file = new File( templatepath, name );           
        if ( file.canRead() )
        {
            Template template = new Template(new BufferedInputStream(
                new FileInputStream(file.getAbsolutePath())));
            
            if ( useCache )
            {
                CachedTemplate cacheit = new CachedTemplate( file );
                cacheit.setTemplate( template );
                cacheit.setInterval( checkInterval );
                cache.put( name, cacheit );
            }
            return template;
        }
        else
        {
            throw new Exception("Can't load template: " + file.getAbsolutePath());
        }
    }
}







