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

import org.apache.velocity.exception.MethodInvocationException;

/**
 *  Handles the equivalence operator
 *
 *    <arg1>  == <arg2>
 *
 *  This operator requires that the LHS and RHS are both of the
 *  same Class.
 *
 *  @version $Id: ASTEQNode.java,v 1.9.4.1 2004/03/03 23:22:58 geirm Exp $
 */
public class ASTEQNode extends SimpleNode
{
    public ASTEQNode(int id)
    {
        super(id);
    }

    public ASTEQNode(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *   Calculates the value of the logical expression
     *
     *     arg1 == arg2
     *
     *   All class types are supported.   Uses equals() to 
     *   determine equivalence.  This should work as we represent
     *   with the types we already support, and anything else that
     *   implements equals() to mean more than identical references.
     *
     *
     *  @param context  internal context used to evaluate the LHS and RHS
     *  @return true if equivalent, false if not equivalent,
     *          false if not compatible arguments, or false
     *          if either LHS or RHS is null
     */
    public boolean evaluate( InternalContextAdapter context)
        throws MethodInvocationException
    {
        Object left = jjtGetChild(0).value(context);
        Object right = jjtGetChild(1).value(context);

        /*
         *  they could be null if they are references and not in the context
         */

        if (left == null || right == null)
        {
            rsvc.error( ( left == null ? "Left" : "Right" ) 
                           + " side ("
                           + jjtGetChild( (left == null? 0 : 1) ).literal() 
                           + ") of '==' operation "
                           + "has null value. " 
                           + "If a reference, it may not be in the context."
                           + " Operation not possible. "
                           + context.getCurrentTemplateName() + " [line " + getLine() 
                           + ", column " + getColumn() + "]");
            return false;
        }

        /*
         *  check to see if they are the same class.  I don't think this is slower
         *  as I don't think that getClass() results in object creation, and we can
         *  extend == to handle all classes
         */

        if (left.getClass().equals( right.getClass() ) )
        {
            return left.equals( right );
        }
        else
        {
            rsvc.error("Error in evaluation of == expression."
                          + " Both arguments must be of the same Class."
                          + " Currently left = " + left.getClass() + ", right = " 
                          + right.getClass() + ". "
                          + context.getCurrentTemplateName() + " [line " + getLine() 
                          + ", column " + getColumn() + "] (ASTEQNode)");
        }

        return false;    
    }

    public Object value(InternalContextAdapter context)
        throws MethodInvocationException
    {
        boolean val = evaluate(context);

        return val ? Boolean.TRUE : Boolean.FALSE;
    }

}
