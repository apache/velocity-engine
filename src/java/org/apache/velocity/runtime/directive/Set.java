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

// Clean this up make one body of reference code.

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
        //Node right = jjtGetChild(1).jjtGetChild(0);
        Node right = node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)
                        .jjtGetChild(1).jjtGetChild(0);

        value = right.value(context);
        
        //Node left = jjtGetChild(0);
        Node left = node.jjtGetChild(0).jjtGetChild(0)
                        .jjtGetChild(0).jjtGetChild(0);
        
        if (left.jjtGetNumChildren() == 0)
            context.put(left.getFirstToken().image.substring(1), value);
        else            
            setReferenceValue(context, (ASTReference) left, value);
    
    }

    protected void setReferenceValue(Context context, ASTReference node, Object value)
    {
        Object result = getReferenceValue(context, node, 1);
        Object[] args = { value };
        ClassUtils.invoke(result, "set" + property, args);
    }

    // Put this in the base visitor.
    protected  Object getReferenceValue(Context context, ASTReference node, int tailChildrenToIgnore)
    {
        // The rootOfIntrospection is the object we will
        // retrieve from the Context. This is the base
        // object we will apply reflection to.
        
        String rootOfIntrospection = node.getFirstToken().image;
        Object result = getVariableValue(context, rootOfIntrospection);
        Object newResult;
        String method;
        String identifier;
        
        String signature = "";
        
        // How many child nodes do we have?
        int children = node.jjtGetNumChildren();
        
        for (int i = 0; i < children - tailChildrenToIgnore; i++)
        {
            Node n = node.jjtGetChild(i);
            
            // Change this to use polymorphism!
            
            switch(n.getType())
            {
                case ParserTreeConstants.JJTIDENTIFIER:
                    identifier = n.getFirstToken().image;
                    method = "get" + identifier;

                    newResult = ClassUtils.invoke(result, method);
                    if (newResult == null)
                    {
                        method = "get";
                        Object[] args = { identifier };
                        Class[] ptypes = null;
                        
                        // Have to make sure class types are
                        // correct for a proper signature match.
                        
                        if (result instanceof Map)
                        {
                            // This can be created once.
                            ptypes = new Class[1];
                            ptypes[0] = new Object().getClass();
                        }                            
                        
                        result = ClassUtils.invoke(result, method, args, ptypes);
                    }
                    else
                    {
                        result = newResult;
                    }                        
                    
                    break;
            
                case ParserTreeConstants.JJTMETHOD:

                    // node 1: method name
                    // The rest of the nodes are parameters
                    // to the method. They may be references
                    // or string literals. If they are
                    // references then we just use a little
                    // recursion.

                    method = n.jjtGetChild(0).getFirstToken().image;
                    int parameters = n.jjtGetNumChildren() - 1;
                
                    Object[] params = new Object[parameters];
                
                    for (int j = 0; j < parameters; j++)
                    {
                        Node p = n.jjtGetChild(j + 1);
                    
                        // Again use polymorphism. Wait until
                        // the nodes settle down.
                        
                        switch(p.getType())
                        {
                            case ParserTreeConstants.JJTREFERENCE:
                                params[j] = getReferenceValue(context, (ASTReference)p, 0);
                                break;
                            
                            case ParserTreeConstants.JJTSTRINGLITERAL:
                                params[j] = getStringLiteralValue(p.getFirstToken().image);
                                break;
                        }
                    }                        
                
                result = ClassUtils.invoke(result, method, params);
                
                signature = signature + "Method.";
                
                break;                    
            }
        }
        
        if (tailChildrenToIgnore == 1)
            property = node.jjtGetChild(children - 1).getFirstToken().image;

        return result;
    }

    protected  Object getVariableValue(Context context, String variable)
    {
        if (context.containsKey(variable.substring(1)))
        {
            return context.get(variable.substring(1));
        }            
        else
        {
            return null;
        }            
    }

    protected  Object getStringLiteralValue(String s)
    {
        return s.substring(1, s.length() - 1);
    }
}
