package org.apache.velocity.runtime.parser.node;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.Token;

public abstract class ASTBinaryOperator extends SimpleNode
{
    /**
     * The operator token, kept until init
     */
    private Token operator;

    public ASTBinaryOperator(int id)
    {
        super(id);
    }

    public ASTBinaryOperator(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtOpen()
     */
    @Override
    public void jjtOpen()
    {
        super.jjtOpen();
        operator = parser.getToken(0); // the operator has just been consumed
    }

    /**
     * @throws TemplateInitException
     * @see org.apache.velocity.runtime.parser.node.Node#init(org.apache.velocity.context.InternalContextAdapter, java.lang.Object)
     */
    @Override
    public Object init(InternalContextAdapter context, Object data) throws TemplateInitException
    {
        Object obj = super.init(context, data);
        warnTextualOperator(operator);
        operator = null;
        cleanupParserAndTokens(); // drop reference to Parser and all JavaCC Tokens
        return obj;
    }

    /**
     * get the string representing the mathematical operator
     * @return operator string
     */
    public abstract String getLiteralOperator();

    @Override
    public String literal()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(jjtGetChild(0).literal());
        builder.append(' ');
        builder.append(getLiteralOperator());
        builder.append(' ');
        return builder.toString();
    }
}
