package org.apache.velocity.runtime.parser.node;

import java.util.Map;
import org.apache.velocity.Context;
import org.apache.velocity.runtime.Runtime;

public class MapExecutor extends AbstractExecutor
{
    private String name;
    
    public void setData(Object data)
    {
        this.name = data.toString();
    }        
    
    /**
     * The root of introspection is assumed to be a Map
     * so try to get the object that has been stored
     * with the key 'name'. The lookup might fail
     * for two reasons: the object may actually not
     * be in the Map, or a property/method name
     * could have been typed in incorrectly in
     * the template which means we will be trying
     * to cast a context object to a Map which
     * will cause a ClassCastException. Just catch
     * the CCE and return null: ASTIdentifier will
     * log the warning.
     */
    public Object execute(Object o, Context context)
    {
        try
        {
            return ((Map)o).get(name);
        }
        catch (ClassCastException cce)
        {
            return null;
        }
    }
}
