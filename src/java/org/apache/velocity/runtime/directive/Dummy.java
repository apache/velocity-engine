package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.Context;

import org.apache.velocity.runtime.parser.Node;

public class Dummy implements Directive
{
    public String getName() { return "dummy"; }
    public int getType() { return LINE; }
    public int getArgs() { return 3; }

    public void render(Context context, Writer writer, Node node)
        throws IOException
    {
    }
}
