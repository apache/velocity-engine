package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.Context;
import org.apache.velocity.runtime.parser.Node;


public interface Directive
{
    public static final int BLOCK = 1;
    public static final int LINE = 2;
    
    public String getName();
    public int getType();

    public void render(Context context, Writer writer, Node node)
        throws IOException;
}        
