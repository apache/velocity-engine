package org.apache.velocity.runtime.visitor;

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

import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;

import org.apache.velocity.runtime.parser.node.*;

/**
 * This is the base class for all visitors.
 * For each AST node, this class will provide
 * a bare-bones method for traversal.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: BaseVisitor.java,v 1.12.4.1 2004/03/03 23:23:02 geirm Exp $
 */
public abstract class BaseVisitor implements ParserVisitor
{
    /** Context used during traversal */
    protected InternalContextAdapter context;
    
    /** Writer used as the output sink */
    protected Writer writer;
    
    public void setWriter( Writer writer )
    {
        this.writer = writer;
    }        

    public void setContext( InternalContextAdapter context)
    {
        this.context = context;
    }
    
    public Object visit(SimpleNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTprocess node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTExpression node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTAssignment node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTOrNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTAndNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTEQNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTNENode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTLTNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTGTNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTLENode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTGENode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTAddNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTSubtractNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTMulNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTDivNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTModNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTNotNode node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTNumberLiteral node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTStringLiteral node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTIdentifier node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTMethod node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTReference node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTTrue node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTFalse node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTBlock node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTText node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTIfStatement node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTElseStatement node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTElseIfStatement node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }
    
    public Object visit(ASTComment node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTObjectArray node, Object data)
    { 
        data = node.childrenAccept(this, data); 
        return data; 
    }

    public Object visit(ASTWord node, Object data)
    { 
        data = node.childrenAccept(this, data);   
        return data;
    }
    
    public Object visit(ASTSetDirective node, Object data)
    { 
        data = node.childrenAccept(this, data);   
        return data;
    }
    
    public Object visit(ASTDirective node, Object data)
    { 
        data = node.childrenAccept(this, data);   
        return data;
    }
}
