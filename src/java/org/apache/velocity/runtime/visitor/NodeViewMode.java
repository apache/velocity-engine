package org.apache.velocity.runtime.visitor;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import org.apache.velocity.*;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.*;

/**
 * This class is simply a visitor implementation
 * that traverses the AST, produced by the Velocity
 * parsing process, and creates a visual structure
 * of the AST. This is primarily used for
 * debugging, but it useful for documentation
 * as well.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: NodeViewMode.java,v 1.4 2001/03/05 11:47:19 jvanzyl Exp $
 */
public class NodeViewMode extends BaseVisitor
{
    private int indent = 0;
    private boolean showTokens = true;

    /** Indent child nodes to help visually identify
      *  the structure of the AST.
      */
    private String indentString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; ++i)
        {
            sb.append("  ");
        }
        return sb.toString();
    }

    /**
      * Display the type of nodes and optionally the
      * first token.
      */
    private Object showNode(Node node, Object data)
    {
        String tokens = "";
        String special = "";
        Token t;
        
        if (showTokens)
        {
            t = node.getFirstToken();
            
            if (t.specialToken != null && ! t.specialToken.image.startsWith("##"))
                special = t.specialToken.image;
            
            tokens = " -> " + special + t.image;
        }            
        
        System.out.println(indentString() + node + tokens);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    /** Display a SimpleNode */
    public Object visit(SimpleNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTprocess node */
    public Object visit(ASTprocess node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTExpression node */
    public Object visit(ASTExpression node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTAssignment node ( = ) */
    public Object visit(ASTAssignment node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTOrNode ( || ) */
    public Object visit(ASTOrNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTAndNode ( && ) */
    public Object visit(ASTAndNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTEQNode ( == ) */
    public Object visit(ASTEQNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTNENode ( != ) */
    public Object visit(ASTNENode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTLTNode ( < ) */
    public Object visit(ASTLTNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTGTNode ( > ) */
    public Object visit(ASTGTNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTLENode ( <= ) */
    public Object visit(ASTLENode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTGENode ( >= ) */
    public Object visit(ASTGENode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTAddNode ( + ) */
    public Object visit(ASTAddNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTSubtractNode ( - ) */
    public Object visit(ASTSubtractNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTMulNode ( * ) */
    public Object visit(ASTMulNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTDivNode ( / ) */
    public Object visit(ASTDivNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTModNode ( % ) */
    public Object visit(ASTModNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTNotNode ( ! ) */
    public Object visit(ASTNotNode node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTNumberLiteral node  */
    public Object visit(ASTNumberLiteral node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTStringLiteral node */
    public Object visit(ASTStringLiteral node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTIdentifier node */
    public Object visit(ASTIdentifier node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTMethod node */
    public Object visit(ASTMethod node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTReference node */
    public Object visit(ASTReference node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTTrue node */
    public Object visit(ASTTrue node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTFalse node */
    public Object visit(ASTFalse node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTBlock node */
    public Object visit(ASTBlock node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTText node */
    public Object visit(ASTText node, Object data)
    { 
        return showNode(node,data); 
    }
    
    /** Display an ASTIfStatement node */
    public Object visit(ASTIfStatement node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTElseStatement node */
    public Object visit(ASTElseStatement node, Object data)
    { 
        return showNode(node,data);
    }
    
    /** Display an ASTElseIfStatement node */
    public Object visit(ASTElseIfStatement node, Object data)
    { 
        return showNode(node,data);
    }
    
    public Object visit(ASTObjectArray node, Object data)
    { 
        return showNode(node,data);
    }

    public Object visit(ASTDirective node, Object data)
    { 
        return showNode(node,data);
    }

    public Object visit(ASTWord node, Object data)
    { 
        return showNode(node,data);
    }
    
    public Object visit(ASTSetDirective node, Object data)
    { 
        return showNode(node,data);
    }
}
