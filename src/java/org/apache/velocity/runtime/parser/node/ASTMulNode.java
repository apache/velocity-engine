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
 * Handles integer multiplication
 *
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTMulNode.java,v 1.7.8.1 2004/03/03 23:22:59 geirm Exp $ 
*/
public class ASTMulNode extends SimpleNode
{
    public ASTMulNode(int id)
    {
        super(id);
    }

    public ASTMulNode(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  computes the product of the two args.  Returns null if either arg is null
     *  or if either arg is not an integer
     */
    public Object value( InternalContextAdapter context )
        throws MethodInvocationException
    {
        /*
         *  get the two args
         */

        Object left = jjtGetChild(0).value( context );
        Object right = jjtGetChild(1).value( context );

        /*
         *  if either is null, lets log and bail
         */

        if (left == null || right == null)
        {
            rsvc.error( ( left == null ? "Left" : "Right" ) + " side ("
                           + jjtGetChild( (left == null? 0 : 1) ).literal()
                           + ") of multiplication operation has null value."
                           + " Operation not possible. "
                           + context.getCurrentTemplateName() + " [line " + getLine() 
                           + ", column " + getColumn() + "]");
            return null;
        }
        
        /*
         *  if not an Integer, not much we can do either
         */

        if ( !( left instanceof Integer )  || !( right instanceof Integer ))
        {
            rsvc.error( ( !( left instanceof Integer ) ? "Left" : "Right" ) 
                           + " side of multiplication operation is not a valid type. "
                           + "Currently only integers (1,2,3...) and Integer type is supported. "
                           +  context.getCurrentTemplateName() + " [line " + getLine() 
                           + ", column " + getColumn() + "]");
 
            return null;
        }

        return new Integer( ( (Integer) left ).intValue() * (  (Integer) right ).intValue() );
    }
}




