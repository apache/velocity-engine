package org.apache.velocity.runtime.directive;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.app.event.EventHandlerUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

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
 *    "directive.parse.max.depth = 10" by default.  This 10 deep
 *    limit is a safety feature to prevent infinite loops.
 * </pre>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @version $Id$
 */
public class Parse extends InputBase
{
    private int maxDepth;
    
    /**
     * Indicates if we are running in strict reference mode.
     */
    public boolean strictRef = false;    
    
    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "parse";
    }

    /**
     * Overrides the default to use "template", so that all templates
     * can use the same scope reference, whether rendered via #parse
     * or direct merge.
     */
    public String getScopeName()
    {
        return "template";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    public int getType()
    {
        return LINE;
    }

    /**
     * Init's the #parse directive.
     * @param rs
     * @param context
     * @param node
     * @throws TemplateInitException
     */
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
        throws TemplateInitException
    {
        super.init(rs, context, node);

        this.maxDepth = rsvc.getInt(RuntimeConstants.PARSE_DIRECTIVE_MAXDEPTH, 10);
        
        strictRef = rsvc.getBoolean(RuntimeConstants.RUNTIME_REFERENCES_STRICT, false);        
    }

    /**
     *  iterates through the argument list and renders every
     *  argument that is appropriate.  Any non appropriate
     *  arguments are logged, but render() continues.
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     */
    public boolean render( InternalContextAdapter context,
                           Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException,
               MethodInvocationException
    {
        /*
         *  did we get an argument?
         */
        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("parameter missing: template name at "
                 + Log.formatFileString(this));
        }

        /*
         *  does it have a value?  If you have a null reference, then no.
         */
        Object value =  node.jjtGetChild(0).value( context );

        if ( value == null)
        {
            if (strictRef)
            {
                throw new VelocityException("The argument to #parse returned null at "
                  + Log.formatFileString(this));
            }
            
            rsvc.getLog().error("#parse() null argument");
            return  false;
        }

        /*
         *  get the path
         */
        String sourcearg = value.toString();

        /*
         *  check to see if the argument will be changed by the event cartridge
         */


        String arg = EventHandlerUtil.includeEvent( rsvc, context, sourcearg, context.getCurrentTemplateName(), getName());

        /*
         *   a null return value from the event cartridge indicates we should not
         *   input a resource.
         */
        boolean blockinput = false;
        if (arg == null)
            blockinput = true;


        if (maxDepth > 0)
        {
            /* 
             * see if we have exceeded the configured depth.
             */
            Object[] templateStack = context.getTemplateNameStack();
            if (templateStack.length >= maxDepth)
            {
                StringBuffer path = new StringBuffer();
                for( int i = 0; i < templateStack.length; ++i)
                {
                    path.append( " > " + templateStack[i] );
                }
                rsvc.getLog().error("Max recursion depth reached (" +
                                    templateStack.length + ')' + " File stack:" +
                                    path);
                return false;
            }
        }

        /*
         *  now use the Runtime resource loader to get the template
         */

        Template t = null;

        try
        {
            if (!blockinput)
                t = rsvc.getTemplate( arg, getInputEncoding(context) );
        }
        catch ( ResourceNotFoundException rnfe )
        {
            /*
             * the arg wasn't found.  Note it and throw
             */
            rsvc.getLog().error("#parse(): cannot find template '" + arg +
                                "', called at " + Log.formatFileString(this));
            throw rnfe;
        }
        catch ( ParseErrorException pee )
        {
            /*
             * the arg was found, but didn't parse - syntax error
             *  note it and throw
             */
            rsvc.getLog().error("#parse(): syntax error in #parse()-ed template '"
                                + arg + "', called at " + Log.formatFileString(this));
            throw pee;
        }
        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            rsvc.getLog().error("Exception rendering #parse(" + arg + ") at " +
                                Log.formatFileString(this));
            throw e;
        }
        catch ( Exception e)
        {
            String msg = "Exception rendering #parse(" + arg + ") at " +
                         Log.formatFileString(this);
            rsvc.getLog().error(msg, e);
            throw new VelocityException(msg, e);
        }

        /**
         * Add the template name to the macro libraries list
         */
        List macroLibraries = context.getMacroLibraries();

        /**
         * if macroLibraries are not set create a new one
         */
        if (macroLibraries == null)
        {
            macroLibraries = new ArrayList();
        }

        context.setMacroLibraries(macroLibraries);

        macroLibraries.add(arg);

        /*
         *  and render it
         */
        try
        {
            if (!blockinput) {
                preRender(context);
                context.pushCurrentTemplateName(arg);
                
                ((SimpleNode) t.getData()).render(context, writer);
            }
        }
        catch( StopCommand stop )
        {
            if (!stop.isFor(this))
            {
                throw stop;
            }
        }
        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            /**
             * Log #parse errors so the user can track which file called which.
             */
            rsvc.getLog().error("Exception rendering #parse(" + arg + ") at " +
                                Log.formatFileString(this));
            throw e;
        }
        catch ( Exception e )
        {
            String msg = "Exception rendering #parse(" + arg + ") at " +
                         Log.formatFileString(this);
            rsvc.getLog().error(msg, e);
            throw new VelocityException(msg, e);
        }
        finally
        {
            if (!blockinput)
            {
                context.popCurrentTemplateName();
                postRender(context);
            }
        }

        /*
         *    note - a blocked input is still a successful operation as this is
         *    expected behavior.
         */

        return true;
    }
    
    /**
     * Called by the parser to validate the argument types
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
      throws ParseException
    {
        if (argtypes.size() != 1)
        {
            throw new MacroParseException("The #parse directive requires one argument",
               templateName, t);
        }
        
        if (argtypes.get(0) == ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("The argument to #parse is of the wrong type",
                templateName, t);          
        }
    }
}

