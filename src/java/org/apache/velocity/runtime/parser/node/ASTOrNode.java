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

import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.context.InternalContextAdapter;

import org.apache.velocity.exception.MethodInvocationException;

/**
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTOrNode.java,v 1.6.8.1 2004/03/03 23:22:59 geirm Exp $ 
*/
public class ASTOrNode extends SimpleNode
{
    public ASTOrNode(int id)
    {
        super(id);
    }

    public ASTOrNode(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  Returns the value of the expression.
     *  Since the value of the expression is simply the boolean
     *  result of evaluate(), lets return that.
     */
    public Object value(InternalContextAdapter context )
        throws MethodInvocationException
    {
        return new Boolean( evaluate( context ) );
    }

    /**
     *  the logical or :
     *    the rule :
     *      left || null -> left
     *      null || right -> right
     *      null || null -> false
     *      left || right ->  left || right
     */
    public boolean evaluate( InternalContextAdapter context)
        throws MethodInvocationException
    {
        Node left = jjtGetChild(0);
        Node right = jjtGetChild(1);

        /*
         *  if the left is not null and true, then true
         */
        
        if (left != null && left.evaluate( context ) )
            return true;

        /*
         *  same for right
         */

        if ( right != null && right.evaluate( context ) )
            return true;

        return false;
    }
}





