package org.apache.velocity.runtime.parser.node;

import java.util.Map;
import org.apache.velocity.Context;

public abstract class AbstractExecutor
{
    protected Object data;

    public abstract Object execute(Object o, Context context);

    public void setData(Object data)
    {
        this.data = data;
    }        
}
