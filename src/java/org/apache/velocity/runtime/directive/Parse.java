package org.apache.velocity.runtime.directive;

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

import java.io.*;

import org.apache.velocity.runtime.parser.*;
import org.apache.velocity.Context;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.configuration.*;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.ParseDirectiveVisitor;
import org.apache.velocity.util.StringUtils;

/**
 * Pluggable directive that handles the #parse() statement in VTL. 
 *
 * Notes:
 * -----
 *  1) The parsed source material can only come from somewhere in 
 *    the TemplateRoot tree for security reasons. There is no way 
 *    around this.  If you want to include content from elsewhere on
 *    your disk, use a link from somwhere under Template Root to that 
 *    content.
 *
 *  2) There is a limited parse depth.  It is set as a property 
 *    "parse_directive.maxdepth = 10"  for example.  There is a 20 iteration
 *    safety in the event that the parameter isn't set.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Parse.java,v 1.5 2000/11/28 00:21:04 jvanzyl Exp $
 */
public class Parse extends Directive
{
    /**
     * Name of this directive. Reflection is used
     * in the Runtime to grab the value of this
     * field so that the directive can be named
     * and initialized.
     */
    
    public String DIRECTIVE_NAME = "parse";
    /**
     * Type of this directive. Reflection is used
     * in the Runtime to grab the value of this
     * field so that the directive can be typed
     * and initialized.
     */
    public int DIRECTIVE_TYPE = LINE;
    
    SimpleNode nodeTree_ = null;
    int iParseDepth_ = 1;
    boolean bReady_ = false;

    /**
     *   Initializes the trees
     */
    public void init(Context context, Node node) 
        throws Exception
    {
        /*
         *  init my bretheren.  I am not in the tree, so I don't get called twice :)
         */

        super.init(context, node );

        /*
         *  did we get an argument?
         */

        if ( node.jjtGetChild(0) == null)
        {
            Runtime.error( new String("#parse() error :  null argument") );
            return;
        }
            
        /*
         *  does it have a value?  If you have a null reference, then no.
         */
        
        Object value =  node.jjtGetChild(0).value( context );
        
        if ( value == null)
        {
            Runtime.error( new String("#parse() error :  null argument") );
            return ;
        }

        /*
         *  get the path
         */
        
        String strArg = value.toString();
            
        /*
         *  everything must be under the template root TEMPLATE_PATH
         */
        
        //String strTemplatePath = Runtime.getString(Runtime.TEMPLATE_PATH);
        
        /*
         *  for security, we will not accept anything with .. in the path
         */
        
        if ( strArg.indexOf("..") != -1)
        {
            Runtime.error( new String("#parse() error : argument " + strArg + " contains .. and may be trying to access content outside of template root.  Rejected.") );
            return;
        }

        /*
         *  if a / leads off, then just nip that :)
         */
        
        if ( strArg.startsWith( "/") )
            strArg = strArg.substring(1);

        /*
         *  we will put caching here in the future...
         */

        Template t = Runtime.getTemplate(strArg);
        
        if (t != null)
        {
            try
            {
                nodeTree_ = t.getDocument();

                ParseDirectiveVisitor v = new ParseDirectiveVisitor();
                v.setDepth( iParseDepth_ );
                v.setContext( null );
                v.setWriter( null );
                nodeTree_.jjtAccept( v, null );
                nodeTree_.init( context, null );
            }
            catch ( ParseDirectiveException pde )
            {
                pde.addFile( strArg );
                throw pde;
            }
        }
        else
            throw new Exception("#parse : cannot find " + strArg + " template!");

        bReady_ = true;
    }

    /**
     *  iterates through the argument list and renders every
     *  argument that is appropriate.  Any non appropriate
     *  arguments are logged, but render() continues.
     */
    public boolean render(Context context, Writer writer, Node node)
        throws IOException
    {
        if (bReady_)
            nodeTree_.render(context, writer);
        return true;
    }

    /**
     *  Sets the depth of recursive parsing
     */
    public void setParseDepth( int i )
        throws Exception
    {
        iParseDepth_ = i;

        /*
         *  see if we have exceeded the configured depth.  If it isn't configured, put a stop at 20 just in case.
         */

        if (iParseDepth_ >= Runtime.getInt("parse_directive.maxdepth", 20))
                throw new ParseDirectiveException("Max recursion depth reached.", iParseDepth_);

        return;
    }
}

