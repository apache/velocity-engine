package org.apache.velocity;

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

import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.loader.TemplateLoader;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * This class is used for controlling all template
 * operations. This class uses a parser created
 * by JavaCC to create an AST that is subsequently
 * traversed by a ProcessVisitor. This class is in
 * the process of changing over to use the
 * InjectorVistor, this is part of the planned
 * caching mechanism.
 *
 * <pre>
 * Template template = Runtime.getTemplate("test.wm");
 * Context context = new Context();
 *
 * context.put("foo", "bar");
 * context.put("customer", new Customer());
 *
 * template.merge(context, writer);
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Template.java,v 1.15 2000/12/04 02:10:18 geirm Exp $
 */
public class Template
{
    /**
     * The root of the AST node structure that results
     * from parsing a Velocity template. This node
     * structure can walk itself, or it can be traversed
     * with a visitor. There are two methods for self
     * walking: init(), and render(). These are an
     * attempt to keep multithreading housekeeping
     * issues to a minimum, so that we don't have to
     * keep a pool of visitors to init()/render()
     * the node structure.
     */
    private SimpleNode document;
    
    /**
     * To keep track of whether this template has been
     * initialized. We use the document.init(context)
     * to perform this. The AST walks itself optimizing
     * nodes creating objects that can be reused during
     * rendering. For example all reflection metadata
     * is stored in ASTReference nodes, the Method objects 
     * are determine in the init() phase and reused 
     * during rendering.
     */
    private boolean initialized = false;

    /**
     * The template loader that initially loaded the input
     * stream for this template, and knows how to check the
     * source of the input stream for modification.
     */
    private TemplateLoader templateLoader;

    /**
     * The number of milliseconds in a minute, used to calculate the
     * check interval.
     */
    protected static final long MILLIS_PER_MINUTE = 60 * 1000;

    /**
     * How often the file modification time is checked (in milliseconds).
     */
    private long modificationCheckInterval = 0;

    /**
     * The file modification time (in milliseconds) for the cached template.
     */
    private long lastModified = 0;

    /**
     * The next time the file modification time will be checked (in 
     * milliseconds).
     */
    private long lastCheck = 0;

    /**
     * The next time the file modification time will be checked (in 
     * milliseconds).
     */
    private long nextCheck = 0;

    /** Template name */
    private String name;

    /** Default constructor */
    public Template()
    {
    }

    /**
     * Set the modification check interval.
     * @param interval The interval (in minutes).
     */
    public void setModificationCheckInterval(long modificationCheckInterval)
    {
        this.modificationCheckInterval = modificationCheckInterval;
    }
    
    public void setDocument(SimpleNode document)
    {
        this.document = document;
    }        

    public SimpleNode getDocument()
    {
        return document;
    }        

    /**
     *  initializes the document.  init() is not longer 
     *  dependant upon context.
     */
    public void initDocument()
        throws Exception
    {
        document.init( null, null);
    }

    /**
     * Is it time to check to see if the template
     * source has been updated?
     */
     public boolean requiresChecking()
     {
        if ( lastCheck >= nextCheck)
        {
            return true;
        }            
        else
        {
            lastCheck = System.currentTimeMillis();
            return false;
        }
    }

    /**
     * Touch this template and thereby resetting
     * the lastCheck, and nextCheck fields.
     */
    public void touch()
    {
        lastCheck = System.currentTimeMillis();
        nextCheck = lastCheck + modificationCheckInterval;
    }
    
    /**
     * Set the name of this template, for example
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
    public TemplateLoader getTemplateLoader()
    {
        return templateLoader;
    }
    
    /**
     * Set the template loader for this template. Set
     * when the Runtime determines where this template
     * came from the list of possible sources.
     */
    public void setTemplateLoader(TemplateLoader templateLoader)
    {
        this.templateLoader = templateLoader;
    }        

    /**
     * The AST node structure is merged with the
     * context to produce the final output. We
     * also init() the AST node structure if
     * it hasn't been already. It might actually
     * be better to move the init() phase up into
     * the parse(), but it would require the passing
     * in of the context. The context is required to
     * determine the objects being used by reflection.
     */
    public void merge(Context context, Writer writer)
        throws IOException, Exception
    {
        document.render(context, writer);
    }
}
