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
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;


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
 * VelocityContext context = new VelocityContext();
 *
 * context.put("foo", "bar");
 * context.put("customer", new Customer());
 *
 * template.merge(context, writer);
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Template.java,v 1.20 2001/01/03 05:15:02 geirm Exp $
 */
public class Template extends Resource
{
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

    /** Default constructor */
    public Template()
    {
    }

    public boolean process()
    {
        try
        {
            InputStream is = resourceLoader.getResourceStream(name);
        
            if (is != null)
            {
                data = Runtime.parse(is, name);
                initDocument();
                return true;
            }
            else
                return false;
        }
        catch (Exception e)
        {
            return false;
        }            
    }

    /**
     *  initializes the document.  init() is not longer 
     *  dependant upon context, but we need to let the 
     *  init() carry the template name down throught for VM
     *  namespace features
     */
    public void initDocument() throws Exception
    {
        /*
         *  send an empty InternalContextAdapter down into the AST to initialize it
         */
        
        InternalContextAdapter ica = new InternalContextAdapter(  new VelocityContext() );

        ica.setCurrentTemplateName( name );
        
        ((SimpleNode)data).init( ica, null);
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
    public void merge( Context context, Writer writer)
        throws IOException, Exception
    {
        if( data != null)
        {
            /*
             *  create an InternalContextAdapter to carry the user Context down
             *  into the rendering engine.  Set the template name and render()
             */

            InternalContextAdapter ica = new InternalContextAdapter( context );

            ica.setCurrentTemplateName( name );

            ( (SimpleNode) data ).render( ica, writer);
        }
        else
            Runtime.error("Template.merge() failure. The document is null, " + 
                          "most likely due to parsing error.");
    }
}

