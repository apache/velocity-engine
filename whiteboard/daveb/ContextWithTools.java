/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.*;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.configuration.*;

/**
 * Just a demo context that loads it's own tools.
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Id: ContextWithTools.java,v 1.1 2001/01/05 04:24:31 daveb Exp $
 */
public class ContextWithTools extends AbstractContext
{
    
    public ContextWithTools()
    {
        super();
        loadTools();
    }

    /**
     * Load the tools specified in the Properties file
     * This can use a lot of work, but it works for testing.
     */
    private void loadTools()
    {
        String toolPackages = VelocityResources.getString("ContextTools");
        Enumeration tools = new StringTokenizer( toolPackages ); 
        try
        {
            while ( tools.hasMoreElements() )
            {
                String toolName = (String)tools.nextElement();
                Object o = Class.forName( toolName ).newInstance();
                ContextTool ct = (ContextTool)o;
                ct.setContext( this );
                put( ct.getName(), ct);
            }
        }
        catch ( Exception cnf )
        {
            Runtime.error( "Not loaded: " + cnf );
        }
    }

    // Below: Implements the AbstractContext stuff
    
     /** storage for key/value pairs */
    private HashMap context = new HashMap();

    
    /** chaining CTOR */
    public ContextWithTools( Context context )
    {
        super( context );
        loadTools();
    }
 
    public Object internalGet( String key )
    {
        return context.get( key );
    }        

    public Object internalPut( String key, Object value )
    {
        return context.put( key, value );
    }

    public  boolean internalContainsKey(Object key)
    {
        return context.containsKey( key );
    }

    public  Object[] internalGetKeys()
    {
        return context.keySet().toArray();
    }

    public  Object internalRemove(Object key)
    {
        return context.remove( key );
    }

    /**
     * Clones this context object
     * @return Object an instance of this Context
     */
    public Object clone()
    {
        ContextWithTools clone = null;

        try
        {
            clone = (ContextWithTools) super.clone();
            clone.context = (HashMap) context.clone();
        }
        catch (CloneNotSupportedException cnse)
        {
        }
        return clone;
    }
}





