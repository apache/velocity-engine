package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.Parser;

import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.StringReader;

import org.apache.velocity.runtime.RuntimeConstants;

/**
 * ASTStringLiteral support.  Will interpolate!
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: ASTStringLiteral.java,v 1.17.4.1 2004/03/03 23:22:59 geirm Exp $
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
     *  shouldn't be one) and then see if interpolation is turned on.
     */
    public Object init(InternalContextAdapter context, Object data) 
        throws Exception
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

        /*
         * tack a space on the end (dreaded <MORE> kludge)
         */

        interpolateimage = image + " ";

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

            nodeTree  = rsvc.parse(br, (context != null) ?
                    context.getCurrentTemplateName() : "StringLiteral", false);

            /*
             *  init with context. It won't modify anything
             */

            nodeTree.init(context, rsvc);
        }

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
                 *  remove the space from the end (dreaded <MORE> kludge)
                 */

                return ret.substring(0, ret.length() - 1);
            }
            catch(Exception e)
            {
                /* 
                 *  eh.  If anything wrong, just punt 
                 *  and output the literal 
                 */
                rsvc.error("Error in interpolating string literal : " + e);
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
