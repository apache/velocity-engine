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

public class Foreach implements Directive
{
    public String getName() { return "foreach"; }        
    public int getType() { return BLOCK; }
    public int getArgs() { return 3; }

    public void render(Context context, Writer writer, Node node)
        throws IOException
    {
    
        // tokens 2,4
        Object data = null;
        Object listObject = null;
        
        // Now we have to iterate over all the child nodes
        // for each in the list, we have to change the
        // context each time the element changes.
        String elementKey = node.jjtGetChild(0).getFirstToken()
                                .image.substring(1);
        
        // if there is an object in the context with
        // the same name as the $element save it so
        // we can restore it after the #foreach.
        
        // The Collection interface provides iterator()
        // I don't think elements() is provided by an interface
        // it just looks like it belongs to Vector. Crappy
        // 1.1 vs 1.2 junk.

        Object tmp = null;
        if (context.containsKey(elementKey))
            tmp = context.get(elementKey);

        listObject = node.jjtGetChild(2).value(context);
        
        /*!
         * @desc Need to create a ReferenceException here, for
         * example when the listObject is null. This
         * obviously should be caught.
         * @priority 1
         */

        if (listObject instanceof Object[])
        {
            int length = ((Object[]) listObject).length;
            
            for (int i = 0; i < length; i++)
            {
                context.put(elementKey,((Object[])listObject)[i]);
                node.jjtGetChild(3).render(context, writer);
            }
            context.remove(elementKey);
        }
        else if (ClassUtils.implementsMethod(listObject, "iterator"))
        {
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
        }
        else if (ClassUtils.implementsMethod(listObject, "elements"))
        {
            Enumeration e = ((Vector) listObject).elements();
            
            while (e.hasMoreElements())
            {
                context.put(elementKey,e.nextElement());
                node.jjtGetChild(3).render(context, writer);
            }
            context.remove(elementKey);
        }

        // Restore the element that was overridden by
        // the iterator.
        if (tmp != null)
            context.put(elementKey, tmp);
    }
}
