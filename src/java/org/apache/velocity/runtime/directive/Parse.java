package org.apache.velocity.runtime.directive;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Pluggable directive that handles the <code>#parse()</code>
 * statement in VTL.
 *
 * <pre>
 * Notes:
 * -----
 *  1) The parsed source material can only come from somewhere in 
 *    the TemplateRoot tree for security reasons. There is no way 
 *    around this.  If you want to include content from elsewhere on
 *    your disk, use a link from somwhere under Template Root to that 
 *    content.
 *
 *  2) There is a limited parse depth.  It is set as a property 
 *    "parse_directive.maxdepth = 10"  for example.  There is a 20 iteration
 *    safety in the event that the parameter isn't set.
 * </pre>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @version $Id: Parse.java,v 1.27.4.1 2004/03/03 23:22:56 geirm Exp $
 */
public class Parse extends InputBase
{
    private boolean ready = false;

    /**
     * Return name of this directive.
     */
    public String getName()
    {
        return "parse";
    }        
    
    /**
     * Return type of this directive.
     */
    public int getType()
    {
        return LINE;
    }        
    
    /**
     *  iterates through the argument list and renders every
     *  argument that is appropriate.  Any non appropriate
     *  arguments are logged, but render() continues.
     */
    public boolean render( InternalContextAdapter context, 
                           Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException,
               MethodInvocationException
    {
        /*
         *  did we get an argument?
         */
        if ( node.jjtGetChild(0) == null)
        {
            rsvc.error( "#parse() error :  null argument" );
            return false;
        }
        
        /*
         *  does it have a value?  If you have a null reference, then no.
         */
        Object value =  node.jjtGetChild(0).value( context );

        if ( value == null)
        {
            rsvc.error( "#parse() error :  null argument" );
            return  false;
        }

        /*
         *  get the path
         */
        String arg = value.toString();
        
        /*
         *   see if we have exceeded the configured depth.
         *   If it isn't configured, put a stop at 20 just in case.
         */

        Object[] templateStack = context.getTemplateNameStack();

        if ( templateStack.length >= 
                rsvc.getInt(RuntimeConstants.PARSE_DIRECTIVE_MAXDEPTH, 20) )
        {
            StringBuffer path = new StringBuffer();

            for( int i = 0; i < templateStack.length; ++i)
            {
                path.append( " > " + templateStack[i] );
            }

            rsvc.error( "Max recursion depth reached (" + 
                templateStack.length + ")"  + " File stack:" + path );
            return false;
        }

        /*
         *  now use the Runtime resource loader to get the template
         */
       
        Template t = null;

        try 
        {
            t = rsvc.getTemplate( arg, getInputEncoding(context) );
        }
        catch ( ResourceNotFoundException rnfe )
        {
       		/*
       		 * the arg wasn't found.  Note it and throw
       		 */
       		 
        	rsvc.error("#parse(): cannot find template '" + arg +
                       "', called from template " +
                       context.getCurrentTemplateName() + " at (" +
                       getLine() + ", " + getColumn() + ")" );
        	throw rnfe;
        }
        catch ( ParseErrorException pee )
        {
        	/*
        	 * the arg was found, but didn't parse - syntax error
        	 *  note it and throw
        	 */

        	rsvc.error("#parse(): syntax error in #parse()-ed template '" +
                       arg + "', called from template " +
                       context.getCurrentTemplateName() + " at (" +
                       getLine() + ", " + getColumn() + ")" );
        		
        	throw pee;
        } 
        catch ( Exception e)
        {	
        	rsvc.error("#parse() : arg = " + arg + ".  Exception : " + e);
            return false;
        }
    
        /*
         *  and render it
         */
        try
        {
            context.pushCurrentTemplateName(arg);
            ((SimpleNode) t.getData()).render( context, writer );
        }
        catch ( Exception e )
        {        
            /*
             *  if it's a MIE, it came from the render.... throw it...
             */

            if ( e instanceof MethodInvocationException)
            {
                throw (MethodInvocationException) e;
            }

            rsvc.error( "Exception rendering #parse( " + arg + " )  : " + e );
            return false;
        }
        finally
        {
            context.popCurrentTemplateName();
        }

        return true;
    }
}

