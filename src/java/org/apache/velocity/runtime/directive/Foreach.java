package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.apache.velocity.Context;
import org.apache.velocity.util.ClassUtils;

import org.apache.velocity.runtime.parser.Node;
import org.apache.velocity.runtime.parser.Token;

public class Foreach extends Directive
{
    public String getName() { return "foreach"; }        
    public int getType() { return BLOCK; }

    private final static int ARRAY = 1;
    private final static int ITERATOR = 2;

    private String elementKey;
    private Object listObject;
    private Object tmp;
    
    public void init(Context context, Node node) throws Exception
    {
        Object sampleElement = null;
        
        elementKey = node.jjtGetChild(0).getFirstToken()
                        .image.substring(1);
        
        // This is a refence node and it needs to
        // be inititialized.
        
        node.jjtGetChild(2).init(context, null);
        listObject = node.jjtGetChild(2).value(context);
        
        // Figure out what type of object the list
        // element is so that we don't have to do it
        // everytime the node is traversed.
        
        if (listObject instanceof Object[])
        {
            node.setInfo(ARRAY);
            sampleElement = ((Object[]) listObject)[0];
        }            
        else if (ClassUtils.implementsMethod(listObject, "iterator"))
        {
            node.setInfo(ITERATOR);
            sampleElement = ((Collection) listObject).iterator().next();
        }            
    
        // This is a little trick so that we can initialize
        // all the blocks in the foreach  properly given
        // that there are references that refer to the
        // elementKey name.
        
        if (sampleElement != null)
        {
            context.put(elementKey, sampleElement);
            super.init(context, node);
            context.remove(elementKey);
        }            
    }

    public void render(Context context, Writer writer, Node node)
        throws IOException
    {
        listObject = node.jjtGetChild(2).value(context);
        
        switch(node.getInfo())
        {
            case ARRAY:
                int length = ((Object[]) listObject).length;
            
                for (int i = 0; i < length; i++)
                {
                    context.put(elementKey,((Object[])listObject)[i]);
                    node.jjtGetChild(3).render(context, writer);
                }
                context.remove(elementKey);
                break;
            
            case ITERATOR:
                // Maybe this could be optimized with get(index) ?
                // Check the interface. size() and get(index) might
                // be faster then using an Iterator.
                Iterator i = ((Collection) listObject).iterator();
            
                while (i.hasNext())
                {
                    context.put(elementKey,i.next());
                    node.jjtGetChild(3).render(context, writer);
                }
                context.remove(elementKey);
                break;
        }            
    }
}
