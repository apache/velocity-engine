package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.Context;

import org.apache.velocity.runtime.parser.Node;

public class Dummy extends Directive
{
    public String getName() { return "dummy"; }
    public int getType() { return LINE; }

    public void init(Context context, Node node) throws Exception
    {
    }
    
    public void render(Context context, Writer writer, Node node)
        throws IOException
    {
    }

}
