package org.apache.velocity.runtime.parser.node;
/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.velocity.util.introspection.Introspector;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.InternalContextAdapter;

import org.apache.velocity.runtime.RuntimeServices;

/**
 * Returned the value of object property when executed.
 */
public class PropertyExecutor extends AbstractExecutor
{
    private String methodUsed = null;

    public PropertyExecutor( RuntimeServices r, Class c, String property)
    {
        rsvc = r;
        
        /*
         * Not using the Introspector here because
         * it can't deal with methods that have
         * no arguments! That needs to be fixed.
         */
        
        try
        {
            methodUsed = "get" + property;
            
            Object[] params = {  };
            method = rsvc.getIntrospector().getMethod( c, methodUsed, params);
           
            if (method == null)
            {
                throw new NoSuchMethodException();
            }
            
        }
        catch (NoSuchMethodException nsme)
        {
            /*
             *  ok, to be more bean-spec-like, 
             *  let try with the first letter of the property
             *  altered to the other case (upper <-> lower)
             *
             *  I am not thrilled about this - this is an inverted
             *  interpretation of the bean spec : the bean spec really
             *  talks about the *other* direction, defining the property
             *  that is related to a get<id>()  set<id>() pair.  We are going
             *  the other way, I believe.  
             *
             *  Taking an uppercase to a lower is getting even further afield
             *  I think, but symmetric and what people may expect.
             */

            StringBuffer sb = new StringBuffer( "get" );
            sb.append( property );

            if(  Character.isLowerCase( sb.charAt(3)))
            {
                sb.setCharAt( 3 ,  Character.toUpperCase( sb.charAt( 3 ) ) );
            }
            else
            {
                sb.setCharAt( 3 ,  Character.toLowerCase( sb.charAt( 3 ) ) );
            }

            try
            {
                methodUsed = sb.toString();
                               
                Object[] params = {  };
                method = rsvc.getIntrospector().getMethod( c, methodUsed, params);
                
                if (method == null)
                {
                    throw new NoSuchMethodException();
                }
            }
            catch ( NoSuchMethodException nsme2 )
            {
            }
            catch(Exception e )
            {
            }
        }
        catch(Exception e )
        {
        }
    }
    

    /**
     * Execute method against context.
     */
    public Object execute(Object o, InternalContextAdapter context)
        throws IllegalAccessException,  MethodInvocationException
    {
        if (method == null)
            return null;
     
        try 
        {
            return method.invoke(o, null);  
        }
        catch( InvocationTargetException ite )
        {
            EventCartridge ec = context.getEventCartridge();

            /*
             *  if we have an event cartridge, see if it wants to veto
             *  also, let non-Exception Throwables go...
             */

            if ( ec != null && ite.getTargetException() instanceof java.lang.Exception)
            {
                try
                {
                    return ec.methodException( o.getClass(), methodUsed, (Exception)ite.getTargetException() );
                }
                catch( Exception e )
                {
                    throw new MethodInvocationException( 
                      "Invocation of method '" + methodUsed + "'" 
                      + " in  " + o.getClass() 
                      + " threw exception " 
                      + ite.getTargetException().getClass() + " : "
                      + ite.getTargetException().getMessage(), 
                      ite.getTargetException(), methodUsed );
                }
            }
            else
            {
                /*
                 * no event cartridge to override. Just throw
                 */

                throw  new MethodInvocationException( 
                "Invocation of method '" + methodUsed + "'" 
                + " in  " + o.getClass() 
                + " threw exception " 
                + ite.getTargetException().getClass() + " : "
                + ite.getTargetException().getMessage(),
                ite.getTargetException(), methodUsed );

              
            }
        }
        catch( IllegalArgumentException iae )
        {
            return null;
        }
    }
}


