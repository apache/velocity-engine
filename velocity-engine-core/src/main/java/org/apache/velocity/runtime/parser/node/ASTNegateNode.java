package org.apache.velocity.runtime.parser.node;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MathException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.util.DuckType;

public class ASTNegateNode extends SimpleNode
{
    protected boolean strictMode = false;

    public ASTNegateNode(int i)
    {
        super(i);
    }

    public ASTNegateNode(Parser p, int i)
    {
        super(p, i);
    }

    /**
     * {@inheritDoc}
     */
    public Object init(InternalContextAdapter context, Object data) throws TemplateInitException
    {
        super.init(context, data);
        /* save a literal image now (needed in case of error) */
        strictMode = rsvc.getBoolean(RuntimeConstants.STRICT_MATH, false);
        cleanupParserAndTokens();
        return data;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#jjtAccept(org.apache.velocity.runtime.parser.node.ParserVisitor, java.lang.Object)
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#evaluate(org.apache.velocity.context.InternalContextAdapter)
     */
    public boolean evaluate( InternalContextAdapter context)
            throws MethodInvocationException
    {
        return jjtGetChild(0).evaluate(context);
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#value(org.apache.velocity.context.InternalContextAdapter)
     */
    public Object value( InternalContextAdapter context)
            throws MethodInvocationException
    {
        Object value = jjtGetChild(0).evaluate( context );
        try
        {
            value = DuckType.asNumber(value);
        }
        catch (NumberFormatException nfe) {}
        if (!(value instanceof Number))
        {
            String msg = "Argument of unary negate (" +
                    jjtGetChild(0).literal() +
                    ") " +
                    (value == null ? "has a null value." : "is not a Number.");
            if (strictMode)
            {
                log.error(msg);
                throw new MathException(msg);
            }
            else
            {
                log.debug(msg);
                return null;
            }
        }
        return MathUtils.negate((Number) value);
    }
    @Override
    public String literal()
    {
        return "-" + jjtGetChild(0).literal();
    }
}
