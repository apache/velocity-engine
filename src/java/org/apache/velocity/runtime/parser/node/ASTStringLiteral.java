package org.apache.velocity.runtime.parser.node;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParserVisitor;

/**
 * ASTStringLiteral support.  Will interpolate!
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ASTStringLiteral extends SimpleNode
{
    /* cache the value of the interpolation switch */
    private boolean interpolate = true;
    private SimpleNode nodeTree = null;
    private String image = "";
    private String interpolateimage = "";

    /** true if the string contains a line comment (##) */
    private boolean containsLineComment;

    /**
     * @param id
     */
    public ASTStringLiteral(int id)
    {
        super(id);
    }

    /**
     * @param p
     * @param id
     */
    public ASTStringLiteral(Parser p, int id)
    {
        super(p, id);
    }

    /**
     *  init : we don't have to do much.  Init the tree (there
     *  shouldn't be one) and then see if interpolation is turned on.
     * @param context
     * @param data
     * @return Init result.
     * @throws TemplateInitException
     */
    public Object init(InternalContextAdapter context, Object data)
    throws TemplateInitException
    {
        /*
         *  simple habit...  we prollie don't have an AST beneath us
         */

        super.init(context, data);

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
            && ((getFirstToken().image.indexOf('$') != -1)
                 || (getFirstToken().image.indexOf('#') != -1));

        /*
         *  get the contents of the string, minus the '/" at each end
         */

        image = getFirstToken().image.substring(1,
                                                getFirstToken().image.length() - 1);

        /**
         * note.  A kludge on a kludge.  The first part, Geir calls
         * this the dreaded <MORE> kludge.  Basically, the use of the
         * <MORE> token eats the last character of an interpolated
         * string.  EXCEPT when a line comment (##) is in
         * the string this isn't an issue.
         *
         * So, to solve this we look for a line comment.  If it isn't found
         * we add a space here and remove it later.
         */

        /**
         * Note - this should really use a regexp to look for [^\]##
         * but apparently escaping of line comments isn't working right
         * now anyway.
         */
        containsLineComment = (image.indexOf("##") != -1);

        /*
         * if appropriate, tack a space on the end (dreaded <MORE> kludge)
         */

        if (!containsLineComment)
        {
            interpolateimage = image + " ";
        }
        else
        {
            interpolateimage = image;
        }

        if (interpolate)
        {
            /*
             *  now parse and init the nodeTree
             */
            BufferedReader br = new BufferedReader(new StringReader(interpolateimage));

            /*
             * it's possible to not have an initialization context - or we don't
             * want to trust the caller - so have a fallback value if so
             *
             *  Also, do *not* dump the VM namespace for this template
             */

            try 
            {
                nodeTree  = rsvc.parse(br, (context != null) ?
                        context.getCurrentTemplateName() : "StringLiteral", false);
            }
            catch (ParseException e)
            {
                throw new TemplateInitException("Problem parsing String literal.",
                        e,
                        (context != null) ? context.getCurrentTemplateName() : "StringLiteral",
                        getColumn(),
                        getLine() );
            }
                
            /*
             *  init with context. It won't modify anything
             */

            nodeTree.init(context, rsvc);
        }

        return data;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#jjtAccept(org.apache.velocity.runtime.parser.ParserVisitor, java.lang.Object)
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  renders the value of the string literal
     *  If the properties allow, and the string literal contains a $ or a #
     *  the literal is rendered against the context
     *  Otherwise, the stringlit is returned.
     * @param context
     * @return result of the rendering.
     */
    public Object value(InternalContextAdapter context)
    {
        if (interpolate)
        {
            try
            {
                /*
                 *  now render against the real context
                 */

                StringWriter writer = new StringWriter();
                nodeTree.render(context, writer);

                /*
                 * and return the result as a String
                 */

                String ret = writer.toString();

                /*
                 * if appropriate, remove the space from the end
                 * (dreaded <MORE> kludge part deux)
                 */
                if (!containsLineComment && ret.length() > 0)
                {
                    return ret.substring(0, ret.length() - 1);
                }
                else
                {
                    return ret;
                }
            }

            /**
             * For interpolated Strings we do not pass exceptions 
             * through -- just log the problem and move on.
             */
            catch( ParseErrorException  e )
            {
                log.error("Error in interpolating string literal", e);
            }
            catch( MethodInvocationException  e )
            {
                log.error("Error in interpolating string literal", e);
            }
            catch( ResourceNotFoundException  e )
            {
                log.error("Error in interpolating string literal", e);
            }
            
            /**
             * pass through application level runtime exceptions
             */
            catch( RuntimeException e )
            {
                throw e;
            }
            
            catch( IOException  e )
            {
                log.error("Error in interpolating string literal", e);
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
