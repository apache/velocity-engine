
package org.apache.velocity.context;

import org.apache.velocity.util.introspection.IntrospectionCacheData;

public final class InternalContextAdapter implements Context
{
    Context context = null;
    InternalContextBase icb = null;

    public InternalContextAdapter( Context c )
    {
        context = c;

        if ( !( c instanceof InternalContextBase ))
        {
            System.out.println("Woog!");
            icb = new InternalContextBase();
        }
        else
            icb = (InternalContextBase) context;
             
    }

    public void setCurrentTemplateName( String s )
    {
       icb.setCurrentTemplateName( s );
    }
  
    public String getCurrentTemplateName()
    {
        return icb.getCurrentTemplateName();
    }

    public IntrospectionCacheData icacheGet( Object key )
    {
        return icb.icacheGet( key );
    }
    
    public void icachePut( Object key, IntrospectionCacheData o )
    {
        icb.icachePut( key, o );
    }

    /** ----------------- */

    public Object put(String key, Object value)
    {
        return context.put( key , value );
    }

    public Object get(String key)
    {
        return context.get( key );
    }

    public boolean containsKey(Object key)
    {
        return context.containsKey( key );
    }

    public Object[] getKeys()
    {
        return context.getKeys();
    }

    public Object remove(Object key)
    {
        return context.remove( key );
    }
}
