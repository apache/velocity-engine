package org.apache.velocity.runtime.directive;

import java.util.Map;

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.Context;
import org.apache.velocity.util.ClassUtils;

import org.apache.velocity.runtime.parser.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ASTReference;
import org.apache.velocity.runtime.parser.ParserTreeConstants;

public class Set implements Directive
{
    protected String property;
    
    public String getName() { return "set"; }
    public int getType() { return LINE; }
    public int getArgs() { return 1; }

    public void render(Context context, Writer writer, Node node)
        throws IOException
    {
        Object value = null;
        Node right = node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)
                        .jjtGetChild(1).jjtGetChild(0);

        value = right.value(context);
        
        ASTReference left = (ASTReference) node.jjtGetChild(0).jjtGetChild(0)
                        .jjtGetChild(0).jjtGetChild(0);
        
        if (left.jjtGetNumChildren() == 0)
            context.put(left.getFirstToken().image.substring(1), value);
        else
            left.setValue(context, value);
    
    }
}
