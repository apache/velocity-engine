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

import org.apache.velocity.runtime.parser.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;

/**
 * This class is used for controlling all template
 * operations. This class uses a parser created
 * by JavaCC to create an AST that is subsequently
 * traversed by a ProcessVisitor. This class is in
 * the process of changing over to use the
 * InjectorVistor, this is part of the planned
 * caching mechanism.
 *
 * Template template = new Template("test.wm");
 * Context context = new Context();
 *
 * context.put("foo", "bar");
 * context.put("customer", new Customer());
 *
 * template.parse();
 * template.merge(context, writer);
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Template.java,v 1.2 2000/10/09 15:09:10 jvanzyl Exp $
 */
public class Template
{
    /** Template processor */
    //private Processor processor;
    private SimpleNode document;
    private boolean initialized = false;

    // So now, after a template is constructed all the
    // initial processing is done.

    //public Template(InputStream inputStream, Processor processor)
    public Template(InputStream inputStream) throws Exception
    {
        parse(inputStream);
    }

    public void parse(InputStream inputStream) throws Exception
    {
        document = Runtime.parse(inputStream);
    }

    public void merge(Context context, Writer writer)
        throws IOException
    {
        synchronized(this)
        {
            if (!initialized) 
                init(context);
        }                
        
        document.render(context, writer);
    }

    private void init(Context context)
    {
        try
        {
            System.out.println("initializing!");
            document.init(context, null);
            initialized = true;
        }
        catch (Exception e)
        {
            Runtime.error(e);
        }
    }
}
