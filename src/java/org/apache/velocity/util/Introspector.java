package org.apache.velocity.util;

import java.lang.reflect.Method;

public class Introspector
{
    // How do I want to structure this? I only have to
    // do this once through parsing. Go through the
    // whole scenerio. Maps, Properties, Methods.
    
    // Even on the first pass, collect all the info
    // in a hash. Make a primitive hash first for
    // just the method name and the number of args.
    
    public static Method getMethod(Class c, String name, int params)
        throws Exception
    {
        Method[] methods = c.getMethods();
    
        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getName().equals(name) &&
                methods[i].getParameterTypes().length == params)
                return methods[i];
        }            
    
        return null;
    }
}
