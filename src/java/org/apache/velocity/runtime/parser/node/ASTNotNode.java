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
import org.apache.velocity.runtime.parser.*;

import org.apache.velocity.exception.MethodInvocationException;

public class ASTNotNode extends SimpleNode
{
    public ASTNotNode(int id)
    {
        super(id);
    }

    public ASTNotNode(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public boolean evaluate( InternalContextAdapter context)
        throws MethodInvocationException
    {
        if (jjtGetChild(0).evaluate(context))
            return false;
        else
            return true;
    }

    public Object value( InternalContextAdapter context)
        throws MethodInvocationException
    {
        return (jjtGetChild(0).evaluate( context ) ? Boolean.FALSE : Boolean.TRUE) ;
    }
}
