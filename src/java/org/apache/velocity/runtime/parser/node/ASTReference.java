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

import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.exception.ReferenceException;
import org.apache.velocity.runtime.parser.*;

import org.apache.velocity.util.introspection.Introspector;

import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

/**
 * This class is responsible for handling the references in
 * VTL ($foo).
 * 
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:kjohnson@transparent.com>Kent Johnson</a>
 * @version $Id: ASTReference.java,v 1.40 2001/09/26 11:22:25 geirm Exp $ 
*/
public class ASTReference extends SimpleNode
{
    /* Reference types */
    private static final int NORMAL_REFERENCE = 1;
    private static final int FORMAL_REFERENCE = 2;
    private static final int QUIET_REFERENCE = 3;
    
    private int referenceType;
    private String nullString;
    private String rootString;
    private boolean escaped = false;
    private boolean computableReference = true;
    private String escPrefix = "";
    private String morePrefix = "";
    private String identifier = "";
    
    private String literal = null;

    private int numChildren = 0;

    public ASTReference(int id)
    {
        super(id);
    }

    public ASTReference(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public Object init( InternalContextAdapter context, Object data) 
        throws Exception
    {
        /*
         *  init our children
         */

        super.init( context, data );

        /*
         *  the only thing we can do in init() is getRoot()
         *  as that is template based, not context based,
         *  so it's thread- and context-safe
         */

        rootString = getRoot();

        numChildren = jjtGetNumChildren();
        
        /*
         * and if appropriate...
         */

        if (numChildren > 0 )
        {
            identifier = jjtGetChild(numChildren - 1).getFirstToken().image;
        }

        return data;
    }        
    
    /**
     *  Returns the 'root string', the reference key
     */
     public String getRootString()
     {
        return rootString;
     }
     
    /**
     *   gets an Object that 'is' the value of the reference
     *
     *   @param o   unused Object parameter
     *   @param context context used to generate value
     */
    public Object execute(Object o, InternalContextAdapter context)
        throws MethodInvocationException
    {   
        /*
         *  get the root object from the context
         */

        Object result = getVariableValue(context, rootString);
       
        if (result == null)
        {
            return null;
        }            

        /*
         * Iteratively work 'down' (it's flat...) the reference
         * to get the value, but check to make sure that
         * every result along the path is valid. For example:
         *
         * $hashtable.Customer.Name
         *
         * The $hashtable may be valid, but there is no key
         * 'Customer' in the hashtable so we want to stop
         * when we find a null value and return the null
         * so the error gets logged.
         */
        
        try 
        {
            for (int i = 0; i < numChildren; i++)
            {
                result = jjtGetChild(i).execute(result,context);
            
                if (result == null)
                {
                    return null;
                }         
            }
            
            return result;
        }
        catch( MethodInvocationException mie)
        {
            /*
             *  someone tossed their cookies
             */

            rsvc.error("Method " + mie.getMethodName() + " threw exception for reference $" 
                          + rootString 
                          + " in template " + context.getCurrentTemplateName()
                          + " at " +  " [" + this.getLine() + "," + this.getColumn() + "]");

            mie.setReferenceName( rootString );
            throw mie;
        }
    }

    /**
     *  gets the value of the reference and outputs it to the
     *  writer.
     *
     *  @param context  context of data to use in getting value
     *  @param writer   writer to render to
     */
    public boolean render( InternalContextAdapter context, Writer writer)
        throws IOException, MethodInvocationException
    {
        Object value = execute(null, context);
        
        /*
         *  if this reference is escaped (\$foo) then we want to do one of two things :
         *  1) if this is a reference in the context, then we want to print $foo
         *  2) if not, then \$foo  (its considered shmoo, not VTL)
         */

        if ( escaped )
        {
            if ( value == null )
            {
                writer.write( escPrefix );
                writer.write( "\\" );
                writer.write( nullString );
            }
            else
            {
                writer.write( escPrefix );
                writer.write( nullString );
            }
        
            return true;
        }

        /*
         *  the normal processing
         *
         *  if we have an event cartridge, get a new value object
         */

        EventCartridge ec = context.getEventCartridge();

        if (ec != null)
        {
            value =  ec.referenceInsert( nullString, value );
        }

        /*
         *  if value is null...
         */

        if (value == null)
        {
            /* 
             *  write prefix twice, because it's shmoo, so the \ don't escape each other...
             */
              
            writer.write( escPrefix );
            writer.write( escPrefix );
            writer.write( morePrefix );          
            writer.write( nullString );
          
            if (referenceType != QUIET_REFERENCE 
                && rsvc.getBoolean( 
                                      RuntimeConstants.RUNTIME_LOG_REFERENCE_LOG_INVALID, true) )
            {
               rsvc.warn(new ReferenceException("reference : template = " 
                                                    + context.getCurrentTemplateName(), this));
            }

            return true;
        }
        else
        {
            /*
             *  non-null processing
             */

            writer.write( escPrefix );
            writer.write( morePrefix );
            writer.write( value.toString() );
        
            return true;
        }
    }
       
    /**
     *   Computes boolean value of this reference
     *   Returns the actual value of reference return type
     *   boolean, and 'true' if value is not null
     *
     *   @param context context to compute value with
     */
    public boolean evaluate( InternalContextAdapter context)
        throws MethodInvocationException
    {
        Object value = execute(null, context);
        
        if (value == null)
        {
            return false;
        }
        else if (value instanceof Boolean)
        {
            if (((Boolean) value).booleanValue())
                return true;
            else
                return false;
        }
        else
            return true;
    }

    public Object value( InternalContextAdapter context)
        throws MethodInvocationException
    {
        return ( computableReference ? execute(null, context) : null );
    }

    /**
     *  Sets the value of a complex reference (something like $foo.bar)
     *  Currently used by ASTSetReference()
     *
     *  @see ASTSetDirective
     *
     *  @param context context object containing this reference
     *  @param value Object to set as value
     *  @return true if successful, false otherwise
     */
    public boolean setValue( InternalContextAdapter context, Object value)
      throws MethodInvocationException
    {
        /*
         *  The rootOfIntrospection is the object we will
         *  retrieve from the Context. This is the base
         *  object we will apply reflection to.
         */

        Object result = getVariableValue(context, rootString);
        
        if (result == null)
        {
            rsvc.error(new ReferenceException("reference set : template = " + context.getCurrentTemplateName(), this));
            return false;
        }                          
        
        /*
         * How many child nodes do we have?
         */

        for (int i = 0; i < numChildren - 1; i++)
        {
            result = jjtGetChild(i).execute(result, context);
            
            if (result == null)
            {
                rsvc.error(new ReferenceException("reference set : template = " + context.getCurrentTemplateName(), this));
                return false;
            }                          
        }            

        /*
         *  We support two ways of setting the value in a #set($ref.foo = $value ) :
         *  1) ref.setFoo( value )
         *  2) ref,put("foo", value ) to parallel the get() map introspection
         */

        try
        {
            /*
             *  first, we introspect for the set<identifier> setter method
             */

            Object[] params = { value };

            Class c = result.getClass();
            Method m = null;

            try
            {
                m = rsvc.getIntrospector().getMethod( c, "set" + identifier, params);

                if (m == null)
                {
                    throw new NoSuchMethodException();
                }
            }
            catch( NoSuchMethodException nsme2)
            {
                StringBuffer sb = new StringBuffer( "set" );
                sb.append( identifier );

                if(  Character.isLowerCase( sb.charAt(3)))
                {
                    sb.setCharAt( 3 ,  Character.toUpperCase( sb.charAt( 3 ) ) );
                }
                else
                {
                    sb.setCharAt( 3 ,  Character.toLowerCase( sb.charAt( 3 ) ) );
                }
               
                m = rsvc.getIntrospector().getMethod( c, sb.toString(), params);

                if (m == null)
                {
                    throw new NoSuchMethodException();
                }
            }

            /*
             *  and if we get here, getMethod() didn't chuck an exception...
             */
            
            Object[] args = { value };
            m.invoke(result, args);
        }
        catch (NoSuchMethodException nsme)
        {
            /*
             *  right now, we only support the Map interface
             */

            if (result instanceof Map)
            {
                try
                {
                    ((Map) result).put(identifier, value);
                }
                catch (Exception ex)
                {
                    rsvc.error("ASTReference Map.put : exception : " + ex 
                                  + " template = " + context.getCurrentTemplateName() 
                                  + " [" + this.getLine() + "," + this.getColumn() + "]");
                    return false;
                }
            }
            else
            {
                rsvc.error("ASTReference : cannot find " + identifier + " as settable property or key to Map in"
                              + " template = " + context.getCurrentTemplateName() 
                              + " [" + this.getLine() + "," + this.getColumn() + "]");
                return false;
                
            }
        }
        catch( InvocationTargetException ite )
        {
            /*
             *  this is possible 
             */

            throw  new MethodInvocationException( 
                "ASTReference : Invocation of method '" 
                + identifier + "' in  " + result.getClass()
                + " threw exception " 
                + ite.getTargetException().getClass(), 
               ite.getTargetException(), identifier );
        }
        catch( Exception e )
        {
            /*
             *  maybe a security exception?
             */
            rsvc.error("ASTReference setValue() : exception : " + e 
                                  + " template = " + context.getCurrentTemplateName() 
                                  + " [" + this.getLine() + "," + this.getColumn() + "]");
            return false;
         }
        
        return true;
    }

    private String getRoot()
    {
        Token t = getFirstToken();

        /*
         *  we have a special case where something like 
         *  $(\\)*!, where the user want's to see something
         *  like $!blargh in the output, but the ! prevents it from showing.
         *  I think that at this point, this isn't a reference.
         */

        /* so, see if we have "\\!" */

        int slashbang = t.image.indexOf("\\!");

        if ( slashbang != -1 )
        {
            /*
             *  lets do all the work here.  I would argue that if this occurrs, it's 
             *  not a reference at all, so preceeding \ characters in front of the $
             *  are just schmoo.  So we just do the escape processing trick (even | odd)
             *  and move on.  This kind of breaks the rule pattern of $ and # but '!' really
             *  tosses a wrench into things.
             */

             /* 
              *  count the escapes : even # -> not escaped, odd -> escaped
              */

            int i = 0;
            int len = t.image.length();

            i = t.image.indexOf("$");

            if (i == -1)
            {
                /* yikes! */
                rsvc.error("ASTReference.getRoot() : internal error : no $ found for slashbang.");
                computableReference = false;
                nullString = t.image;
                return nullString;
            }

            while( i < len && t.image.charAt(i) != '\\')
                i++;

            /*  ok, i is the first \ char */

            int start = i;
            int count = 0;
 
            while( i < len && t.image.charAt(i++) == '\\' )
                count++;
           
            /*
             *  now construct the output string.  We really don't care about leading 
             *  slashes as this is not a reference.  It's quasi-schmoo
             */

            nullString = t.image.substring(0,start); // prefix up to the first 
            nullString += t.image.substring(start, start + count-1 ); // get the slashes
            nullString += t.image.substring(start+count); // and the rest, including the 

            /*
             *  this isn't a valid reference, so lets short circuit the value and set calcs
             */

            computableReference = false;

            return nullString;
        }

        /*
         *  we need to see if this reference is escaped.  if so
         *  we will clean off the leading \'s and let the 
         *  regular behavior determine if we should output this
         *  as \$foo or $foo later on in render(). Lazyness..
         */
      
        escaped = false;

        if ( t.image.startsWith("\\"))
        {
            /* 
             *  count the escapes : even # -> not escaped, odd -> escaped
             */

            int i = 0;
            int len = t.image.length();

            while( i < len && t.image.charAt(i) == '\\' )
                i++;

 
            if ( (i % 2) != 0 )                
                escaped = true;

            if (i > 0)
                escPrefix = t.image.substring(0, i / 2 );
                                     
            t.image = t.image.substring(i);
        }

        /*
         *  Look for preceeding stuff like '#' and '$'
         *  and snip it off, except for the
         *  last $
         */
              
        int loc1 = t.image.lastIndexOf('$');
             
        /*
         *  if we have extra stuff, loc > 0
         *  ex. '#$foo' so attach that to 
         *  the prefix.
         */
        if( loc1 > 0)
        {
            morePrefix = morePrefix + t.image.substring(0, loc1);
            t.image = t.image.substring(loc1);
        }
        
        /*
         *  Now it should be clean. Get the literal in case this reference 
         *  isn't backed by the context at runtime, and then figure out what
         *  we are working with.
         */

        nullString = literal();
        
        if (t.image.startsWith("$!"))
        {
            referenceType = QUIET_REFERENCE;
 
            /*
             *  only if we aren't escaped do we want to null the output
             */

            if (!escaped)
                nullString = "";
 
            if (t.image.startsWith("$!{"))
            {
                /*
                 *  ex : $!{provider.Title} 
                 */

                return t.next.image;
            }
            else
            {
                /*
                 *  ex : $!provider.Title
                 */
 
                return t.image.substring(2);
            }
        }
        else if (t.image.equals("${"))
        {
            /*
             *  ex : ${provider.Title}
             */

            referenceType = FORMAL_REFERENCE;
            return t.next.image;
        }            
        else
        {
            /*
             *  just nip off the '$' so we have 
             *  the root
             */
             
            referenceType = NORMAL_REFERENCE;   
            return t.image.substring(1);
        }            
    }

    public Object getVariableValue(Context context, String variable)
    {
        return context.get(variable);
    }


    /**
     *  Routine to allow the literal representation to be
     *  externally overridden.  Used now in the VM system
     *  to override a reference in a VM tree with the
     *  literal of the calling arg to make it work nicely
     *  when calling arg is null.  It seems a bit much, but
     *  does keep things consistant.
     *
     *  Note, you can only set the literal once...
     *
     *  @param literal String to render to when null
     */
    public void setLiteral( String literal )
    {
        /*
         * do only once
         */

        if( this.literal == null)
            this.literal = literal;
    }

    /**
     *  Override of the SimpleNode method literal()
     *  Returns the literal representation of the 
     *  node.  Should be something like
     *  $<token>.
     */
    public String literal()
    {
        if (literal != null)
            return literal;

        return super.literal();
    }
}

