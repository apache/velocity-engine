package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Writer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.exception.ReferenceException;
import org.apache.velocity.runtime.parser.*;

import org.apache.velocity.util.introspection.VelPropertySet;
import org.apache.velocity.util.introspection.Info;

import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.app.event.EventCartridge;

/**
 * This class is responsible for handling the references in
 * VTL ($foo).
 *
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:kjohnson@transparent.com>Kent Johnson</a>
 * @version $Id: ASTReference.java,v 1.49.4.1 2004/03/03 23:22:59 geirm Exp $
*/
public class ASTReference extends SimpleNode
{
    /* Reference types */
    private static final int NORMAL_REFERENCE = 1;
    private static final int FORMAL_REFERENCE = 2;
    private static final int QUIET_REFERENCE = 3;
    private static final int RUNT = 4;

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

    protected Info uberInfo;

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

    public Object init(InternalContextAdapter context, Object data)
        throws Exception
    {
        /*
         *  init our children
         */

        super.init(context, data);

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

        /*
         * make an uberinfo - saves new's later on
         */

        uberInfo = new Info(context.getCurrentTemplateName(),
                getLine(),getColumn());

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

        if (referenceType == RUNT)
            return null;

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
        catch(MethodInvocationException mie)
        {
            /*
             *  someone tossed their cookies
             */

            rsvc.error("Method " + mie.getMethodName()
                        + " threw exception for reference $"
                        + rootString
                        + " in template " + context.getCurrentTemplateName()
                        + " at " +  " [" + this.getLine() + ","
                        + this.getColumn() + "]");

            mie.setReferenceName(rootString);
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
    public boolean render(InternalContextAdapter context, Writer writer)
        throws IOException, MethodInvocationException
    {

        if (referenceType == RUNT)
        {
            writer.write(rootString);
            return true;
        }

        Object value = execute(null, context);

        /*
         *  if this reference is escaped (\$foo) then we want to do one of two things :
         *  1) if this is a reference in the context, then we want to print $foo
         *  2) if not, then \$foo  (its considered shmoo, not VTL)
         */

        if (escaped)
        {
            if (value == null)
            {
                writer.write(escPrefix);
                writer.write("\\");
                writer.write(nullString);
            }
            else
            {
                writer.write(escPrefix);
                writer.write(nullString);
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
            value =  ec.referenceInsert(literal(), value);
        }

        /*
         *  if value is null...
         */

        if (value == null)
        {
            /*
             *  write prefix twice, because it's shmoo, so the \ don't escape each other...
             */

            writer.write(escPrefix);
            writer.write(escPrefix);
            writer.write(morePrefix);
            writer.write(nullString);

            if (referenceType != QUIET_REFERENCE
                && rsvc.getBoolean(RuntimeConstants.RUNTIME_LOG_REFERENCE_LOG_INVALID,
                        true))
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

            writer.write(escPrefix);
            writer.write(morePrefix);
            writer.write(value.toString());

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
    public boolean evaluate(InternalContextAdapter context)
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

    public Object value(InternalContextAdapter context)
        throws MethodInvocationException
    {
        return (computableReference ? execute(null, context) : null);
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
        if (jjtGetNumChildren() == 0)
        {
            context.put(rootString, value);
            return true;
        }

        /*
         *  The rootOfIntrospection is the object we will
         *  retrieve from the Context. This is the base
         *  object we will apply reflection to.
         */

        Object result = getVariableValue(context, rootString);

        if (result == null)
        {
            rsvc.error(new ReferenceException("reference set : template = "
                    + context.getCurrentTemplateName(), this));
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
                rsvc.error(new ReferenceException("reference set : template = "
                        + context.getCurrentTemplateName(), this));
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
            VelPropertySet vs =
                    rsvc.getUberspect().getPropertySet(result, identifier,
                            value, uberInfo);

            if (vs == null)
                return false;

            vs.invoke(result, value);
        }
        catch(InvocationTargetException ite)
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
        catch(Exception e)
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

        if (slashbang != -1)
        {
            /*
             *  lets do all the work here.  I would argue that if this occurrs,
             *  it's not a reference at all, so preceeding \ characters in front
             *  of the $ are just schmoo.  So we just do the escape processing
             *  trick (even | odd) and move on.  This kind of breaks the rule
             *  pattern of $ and # but '!' really tosses a wrench into things.
             */

             /*
              *  count the escapes : even # -> not escaped, odd -> escaped
              */

            int i = 0;
            int len = t.image.length();

            i = t.image.indexOf('$');

            if (i == -1)
            {
                /* yikes! */
                rsvc.error("ASTReference.getRoot() : internal error : "
                            + "no $ found for slashbang.");
                computableReference = false;
                nullString = t.image;
                return nullString;
            }

            while (i < len && t.image.charAt(i) != '\\')
            {
                i++;
            }

            /*  ok, i is the first \ char */

            int start = i;
            int count = 0;

            while (i < len && t.image.charAt(i++) == '\\')
            {
                count++;
            }

            /*
             *  now construct the output string.  We really don't care about
             *  leading  slashes as this is not a reference.  It's quasi-schmoo
             */

            nullString = t.image.substring(0,start); // prefix up to the first
            nullString += t.image.substring(start, start + count-1 ); // get the slashes
            nullString += t.image.substring(start+count); // and the rest, including the

            /*
             *  this isn't a valid reference, so lets short circuit the value
             *  and set calcs
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

        if (t.image.startsWith("\\"))
        {
            /*
             *  count the escapes : even # -> not escaped, odd -> escaped
             */

            int i = 0;
            int len = t.image.length();

            while (i < len && t.image.charAt(i) == '\\')
            {
                i++;
            }

            if ((i % 2) != 0)
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
        if (loc1 > 0)
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
        else if (t.image.startsWith("$"))
        {
            /*
             *  just nip off the '$' so we have
             *  the root
             */

            referenceType = NORMAL_REFERENCE;
            return t.image.substring(1);
        }
        else
        {
            /*
             * this is a 'RUNT', which can happen in certain circumstances where
             *  the parser is fooled into believeing that an IDENTIFIER is a real
             *  reference.  Another 'dreaded' MORE hack :).
             */
            referenceType = RUNT;
            return t.image;
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
    public void setLiteral(String literal)
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
