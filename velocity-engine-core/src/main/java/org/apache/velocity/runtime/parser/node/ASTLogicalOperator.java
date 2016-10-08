package org.apache.velocity.runtime.parser.node;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.parser.Parser;

public abstract class ASTLogicalOperator extends ASTBinaryOperator
{
    public ASTLogicalOperator(int id)
    {
        super(id);
    }

    public ASTLogicalOperator(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * @throws TemplateInitException
     * @see org.apache.velocity.runtime.parser.node.Node#init(org.apache.velocity.context.InternalContextAdapter, java.lang.Object)
     */
    public Object init( InternalContextAdapter context, Object data) throws TemplateInitException
    {
        Object obj = super.init(context, data);
        cleanupParserAndTokens(); // drop reference to Parser and all JavaCC Tokens
        return obj;
    }
}
