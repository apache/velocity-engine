package org.apache.velocity.runtime.parser.node;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
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

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.parser.Parser;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * This class is responsible for handling the pluggable
 * directives in VTL. ex.  #foreach()
 * 
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:kav@kav.dk">Kasper Nielsen</a>
 * @version $Id: ASTDirective.java,v 1.19 2001/09/07 05:02:15 geirm Exp $ 
 */
public class ASTDirective extends SimpleNode
{
    private Directive directive;
    private String directiveName = "";
    private boolean isDirective;

    public ASTDirective(int id)
    {
        super(id);
    }

    public ASTDirective(Parser p, int id)
    {
        super(p, id);
    }


    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
    
    public Object init( InternalContextAdapter context, Object data) 
        throws Exception
    {
        super.init( context, data );

        /*
         *  only do things that are not context dependant
         */

        if (parser.isDirective( directiveName ))
        {
            isDirective = true;
            
            directive = (Directive) parser.getDirective( directiveName )
                .getClass().newInstance();
    
            directive.init(rsvc, context,this);

            directive.setLocation( getLine(), getColumn() );
        }          
        else if (rsvc.isVelocimacro( directiveName, context.getCurrentTemplateName()  )) 
        {
            /*
             *  we seem to be a Velocimacro.
             */

            isDirective = true;
            directive = (Directive) rsvc.getVelocimacro( directiveName,  context.getCurrentTemplateName() );

            directive.init( rsvc, context, this );
            directive.setLocation( getLine(), getColumn() );
        } 
        else
        {
            isDirective = false;
        }            
    
        return data;
    }

    public boolean render( InternalContextAdapter context, Writer writer)
        throws IOException,MethodInvocationException, ResourceNotFoundException, ParseErrorException
    {
        /*
         *  normal processing
         */

        if (isDirective)
        {           
            directive.render(context, writer, this);
        }
        else
        {
            writer.write( "#");
            writer.write( directiveName );
        }

        return true;
    }

    /**
     *   Sets the directive name.  Used by the parser.  This keeps us from having to 
     *   dig it out of the token stream and gives the parse the change to override.
     */
    public void setDirectiveName( String str )
    {
        directiveName = str;
        return;
    }

    /**
     *  Gets the name of this directive.
     */
    public String getDirectiveName()
    {
        return directiveName;
    }
}


