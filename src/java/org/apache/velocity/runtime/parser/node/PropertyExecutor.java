package org.apache.velocity.runtime.parser.node;

import java.lang.reflect.Method;

import org.apache.velocity.Context;

public class PropertyExecutor extends AbstractExecutor
{
    protected Method method;
    
    public void setData(Object data)
    {
        this.method = (Method) data;
    }        
    
    public Object execute(Object o, Context context)
    {
        try
        {
            return method.invoke(o, null);
        }
        catch (Exception e)
        {
            return null;
        }            
    }

    
}
