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

public class ASTNENode extends SimpleNode
{
    public ASTNENode(int id)
    {
        super(id);
    }

    public ASTNENode(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public boolean evaluate(  InternalContextAdapter context)
        throws MethodInvocationException
    {
        Object left = jjtGetChild(0).value( context );
        Object right = jjtGetChild(1).value( context );

        /*
         *  null check
         */

        if ( left == null || right == null)
        {
            rsvc.error( ( left == null ? "Left" : "Right" ) + " side ("
                           + jjtGetChild( (left == null? 0 : 1) ).literal()
                           + ") of '!=' operation has null value."
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
            return !(left.equals( right ));
        }
        else
        {
            rsvc.error("Error in evaluation of != expression."
                          + " Both arguments must be of the same Class."
                          + " Currently left = " + left.getClass() + ", right = " 
                          + right.getClass() + ". "
                          + context.getCurrentTemplateName() + " [line " + getLine() 
                          + ", column " + getColumn() + "] (ASTEQNode)");
            
            return false;
        }
    }

    public Object value(InternalContextAdapter context)
        throws MethodInvocationException
    {
        boolean val = evaluate(context);

        return val ? Boolean.TRUE : Boolean.FALSE;
    }

}
