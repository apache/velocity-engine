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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.Parser;

import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.velocity.runtime.RuntimeConstants;

/**
 * ASTStringLiteral support.  Will interpolate!
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: ASTStringLiteral.java,v 1.11 2001/08/07 21:56:30 geirm Exp $
 */
public class ASTStringLiteral extends SimpleNode
{
    /* cache the value of the interpolation switch */
    private boolean interpolate = true;
    private SimpleNode nodeTree = null;
    private String image = "";
    private String interpolateimage = "";

    public ASTStringLiteral(int id)
    {
        super(id);
    }

    public ASTStringLiteral(Parser p, int id)
    {
        super(p, id);
    }
    
    /**
     *  init : we don't have to do much.  Init the tree (there 
     *  shouldn't be one ) and then see if interpolation is turned on.
     */
    public Object init( InternalContextAdapter context, Object data) 
        throws Exception
    {
        /*
         *  simple habit...  we prollie don't have an AST beneath us
         */

        super.init( context, data );

        /*
         *  the stringlit is set at template parse time, so we can 
         *  do this here for now.  if things change and we can somehow 
         * create stringlits at runtime, this must
         *  move to the runtime execution path
         *
         *  so, only if interpolation is turned on AND it starts 
         *  with a " AND it has a  directive or reference, then we 
         *  can  interpolate.  Otherwise, don't bother.
         */

        interpolate = rsvc.getBoolean(RuntimeConstants.INTERPOLATE_STRINGLITERALS , true)
            && getFirstToken().image.startsWith("\"")
            && ( (getFirstToken().image.indexOf("$") != -1 ) 
                 || ( getFirstToken().image.indexOf("#") != -1 ));

        /*
         *  get the contents of the string, minus the '/" at each end
         */
        
        image = getFirstToken().image.substring(1, 
                                                getFirstToken().image.length() - 1);

        /*
         * tack a space on the end (dreaded <MORE> kludge)
         */

        interpolateimage = image + " ";

        return data;
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  renders the value of the string literal
     *  If the properties allow, and the string literal contains a $ or a #
     *  the literal is rendered against the context
     *  Otherwise, the stringlit is returned.
     */
    public Object value( InternalContextAdapter context)
    {
        if (interpolate )
        {          
            try
            {   
                /*
                 *  only parse the first time
                 */

                if (nodeTree == null)
                {
                    /*
                     *  parse the stringlit
                     */
                    
                    BufferedReader br = new BufferedReader( new StringReader( interpolateimage ));

                    nodeTree 
                        = rsvc.parse( br,context.getCurrentTemplateName() );        
                
                    /*
                     *  init with context. It won't modify anything
                     */

                    nodeTree.init( context, rsvc );
                }

                /*
                 *  now render against the real context
                 */

                StringWriter writer = new StringWriter();
                nodeTree.render(context, writer );
                
                /*
                 * and return the result as a String
                 */

                String ret = writer.toString();

                /*
                 *  remove the space from the end (dreaded <MORE> kludge)
                 */

                return ret.substring(0, ret.length() - 1 );

            }
            catch( Exception e )
            {
                /* 
                 *  eh.  If anything wrong, just punt 
                 *  and output the literal 
                 */
                rsvc.error("Error in interpolating string literal : " + e );
            }
        }
        
        /*
         *  ok, either not allowed to interpolate, there wasn't 
         *  a ref or directive, or we failed, so
         *  just output the literal
         */

        return image;
    }
}
