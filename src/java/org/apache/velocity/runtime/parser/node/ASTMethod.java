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

import java.io.*;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.*;
import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

import org.apache.velocity.exception.MethodInvocationException;
import java.lang.reflect.InvocationTargetException;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;

/**
 *  ASTMethod.java
 *
 *  Method support for references :  $foo.method()
 *
 *  NOTE :
 *
 *  introspection is now done at render time.
 *
 *  Please look at the Parser.jjt file which is
 *  what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTMethod.java,v 1.20 2001/09/09 21:49:11 geirm Exp $ 
 */
public class ASTMethod extends SimpleNode
{
    private String methodName = "";
    private int paramCount = 0;
    private Object [] params;

    public ASTMethod(int id)
    {
        super(id);
    }

    public ASTMethod(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  simple init - init our subtree and get what we can from 
     *  the AST
     */
    public Object init(  InternalContextAdapter context, Object data)
        throws Exception
    {
        super.init(  context, data );

        /*
         *  this is about all we can do
         */

        methodName = getFirstToken().image;
        paramCount = jjtGetNumChildren() - 1;
        params = new Object[paramCount];   
        
        return data;
    }

    /**
     *   does the instrospection of the class for the method needed.
     *   Note, as this calls value() on the args if any, this must
     *   only be called at execute() / render() time
     */
    private Method doIntrospection( InternalContextAdapter context, Class data)
        throws MethodInvocationException, Exception
    {      
        /*
         *  Now the parameters have to be processed, there
         *  may be references contained within that need
         *  to be introspected.
         */
        
        for (int j = 0; j < paramCount; j++)
            params[j] = jjtGetChild(j + 1).value(context);
 
        Method m = rsvc.getIntrospector().getMethod( data, methodName, params);

        return m;
    }
    
    /**
     *  invokes the method.  Returns null if a problem, the
     *  actual return if the method returns something, or 
     *  an empty string "" if the method returns void
     */
    public Object execute(Object o, InternalContextAdapter context)
        throws MethodInvocationException
    {
        /*
         *  new strategy (strategery!) for introspection. Since we want 
         *  to be thread- as well as context-safe, we *must* do it now,
         *  at execution time.  There can be no in-node caching,
         *  but if we are careful, we can do it in the context.
         */

        Method method = null;

        try 
        {
            /*
             *   check the cache 
             */

            IntrospectionCacheData icd =  context.icacheGet( this );
            Class c = o.getClass();

            /*
             *  like ASTIdentifier, if we have cache information, and the
             *  Class of Object o is the same as that in the cache, we are
             *  safe.
             */

            if ( icd != null && icd.contextData == c )
            {
                /*
                 * sadly, we do need recalc the values of the args, as this can 
                 * change from visit to visit
                 */

                for (int j = 0; j < paramCount; j++)
                    params[j] = jjtGetChild(j + 1).value(context);

                /*
                 * and get the method from the cache
                 */

                method = (Method) icd.thingy;
            }
            else
            {
                /*
                 *  otherwise, do the introspection, and then
                 *  cache it
                 */

                method = doIntrospection( context, c );
                
                if (method != null)
                {    
                    icd = new IntrospectionCacheData();
                    icd.contextData = c;
                    icd.thingy = method;
                    context.icachePut( this, icd );
                }
            }
 
            /*
             *  if we still haven't gotten the method, either we are calling 
             *  a method that doesn't exist (which is fine...)  or I screwed
             *  it up.
             */

            if (method == null)
                return null;
        }
        catch( MethodInvocationException mie )
        {
            /*
             *  this can come from the doIntrospection(), as the arg values
             *  are evaluated to find the right method signature.  We just
             *  want to propogate it here, not do anything fancy
             */

            throw mie;
        }
        catch( Exception e )
        {
            /*
             *  can come from the doIntropection() also, from Introspector
             */

            rsvc.error("ASTMethod.execute() : exception from introspection : " + e);
            return null;
        }

        try
        {
            /*
             *  get the returned object.  It may be null, and that is
             *  valid for something declared with a void return type.
             *  Since the caller is expecting something to be returned,
             *  as long as things are peachy, we can return an empty 
             *  String so ASTReference() correctly figures out that
             *  all is well.
             */

            Object obj = method.invoke(o, params);
            
            if (obj == null)
            {
                if( method.getReturnType() == Void.TYPE)
                     return new String("");
            }
            
            return obj;
        }
        catch( InvocationTargetException ite )
        {
            /*
             *  In the event that the invocation of the method
             *  itself throws an exception, we want to catch that
             *  wrap it, and throw.  We don't log here as we want to figure
             *  out which reference threw the exception, so do that 
             *  above
             */

            EventCartridge ec = context.getEventCartridge();

            /*
             *  if we have an event cartridge, see if it wants to veto
             *  also, let non-Exception Throwables go...
             */

            if ( ec != null && ite.getTargetException() instanceof java.lang.Exception)
            {
                try
                {
                    return ec.methodException( o.getClass(), methodName, (Exception)ite.getTargetException() );
                }
                catch( Exception e )
                {
                    throw new MethodInvocationException( 
                        "Invocation of method '" 
                        + methodName + "' in  " + o.getClass() 
                        + " threw exception " 
                        + e.getClass() + " : " + e.getMessage(), 
                        e, methodName );
                }
            }
            else
            {
                /*
                 * no event cartridge to override. Just throw
                 */

                throw new MethodInvocationException( 
                "Invocation of method '" 
                + methodName + "' in  " + o.getClass() 
                + " threw exception " 
                + ite.getTargetException().getClass() + " : "
                + ite.getTargetException().getMessage(), 
                ite.getTargetException(), methodName );
            }
        }
        catch( Exception e )
        {
            rsvc.error("ASTMethod.execute() : exception invoking method '" 
                               + methodName + "' in " + o.getClass() + " : "  + e );
                               
            return null;
        }            
    }
}
