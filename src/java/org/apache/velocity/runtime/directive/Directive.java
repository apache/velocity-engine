package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.Context;
import org.apache.velocity.runtime.parser.Node;


public abstract class Directive
{
    public static final int BLOCK = 1;
    public static final int LINE = 2;
    
    public abstract String getName();
    public abstract int getType();
    
    public void init(Context context, Node node) throws Exception
    {
        int i, k = node.jjtGetNumChildren();

        for (i = 0; i < k; i++)
            node.jjtGetChild(i).init(context, null);
    }
    
    public abstract void render(Context context, Writer writer, Node node)
        throws IOException;
}        
