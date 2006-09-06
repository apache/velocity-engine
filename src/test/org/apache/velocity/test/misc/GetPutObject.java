package org.apache.velocity.test.misc;

public class GetPutObject
{
    private Object value;

    public Object get()
    {
        return value;
    }   
        
    public void put(final Object value)
    {
        this.value = value;
    }
} 