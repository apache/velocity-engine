package org.apache.velocity.runtime.parser;

import java.util.Map;
import org.apache.velocity.Context;

public class MapExecutor extends AbstractExecutor
{
    private String name;
    
    public void setData(Object data)
    {
        this.name = data.toString();
    }        
    
    public Object execute(Object o, Context context)
    {
        if (((Map)o).containsKey(name))
        {
            return ((Map)o).get(name);
        }            
        else
        {
            return null;
        }            
    }
}
