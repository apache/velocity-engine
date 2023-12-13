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
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
 *    your disk, use a link from somewhere under Template Root to that
 *    content.
 *
 *  2) There is a limited parse depth.  It is set as a property
 *    "directive.parse.max_depth = 10" by default.  This 10 deep
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
    @Override
    public String getName()
    {
        return "parse";
    }

    /**
     * Overrides the default to use "template", so that all templates
     * can use the same scope reference, whether rendered via #parse
     * or direct merge.
     */
    @Override
    public String getScopeName()
    {
        return "template";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    @Override
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
    @Override
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
    @Override
    public boolean render(InternalContextAdapter context,
                          Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException,
               MethodInvocationException
    {
        /*
         *  did we get an argument?
         */
        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#" + getName() + "(): argument missing at " +
                StringUtils.formatFileString(this), null, rsvc.getLogContext().getStackTrace());
        }

        /*
         *  does it have a value?  If you have a null reference, then no.
         */
        Object value =  node.jjtGetChild(0).value( context );
        if (value == null)
        {
            log.debug("#" + getName() + "(): null argument at {}", StringUtils.formatFileString(this));
        }

        /*
         *  get the path
         */
        String sourcearg = value == null ? null : value.toString();

        /*
         *  check to see if the argument will be changed by the event cartridge
         */
        String arg = EventHandlerUtil.includeEvent( rsvc, context, sourcearg, context.getCurrentTemplateName(), getName());

        /*
         * if strict mode and arg was not fixed by event handler, then complain
         */
        if (strictRef && value == null && arg == null)
        {
            throw new VelocityException("The argument to #" + getName() + " returned null at "
              + StringUtils.formatFileString(this), null, rsvc.getLogContext().getStackTrace());
        }

        /*
         *   a null return value from the event cartridge indicates we should not
         *   input a resource.
         */
        if (arg == null)
        {
            // abort early, but still consider it a successful rendering
            return true;
        }


        if (maxDepth > 0)
        {
            /*
             * see if we have exceeded the configured depth.
             */
            String[] templateStack = context.getTemplateNameStack();
            if (templateStack.length >= maxDepth)
            {
                StringBuilder path = new StringBuilder();
                for (String aTemplateStack : templateStack)
                {
                    path.append(" > ").append(aTemplateStack);
                }
                log.error("Max recursion depth reached ({}). File stack: {}",
                          templateStack.length, path);

                return false;
            }
        }

        /*
         *  now use the Runtime resource loader to get the template
         */

        Template t = null;

        try
        {
            t = getTemplate(arg, getInputEncoding(context));
        }
        catch ( ResourceNotFoundException rnfe )
        {
            /*
             * the arg wasn't found.  Note it and throw
             */
            log.error("#" + getName() + "(): cannot find template '{}', called at {}",
                      arg, StringUtils.formatFileString(this));
            throw rnfe;
        }
        catch ( ParseErrorException pee )
        {
            /*
             * the arg was found, but didn't parse - syntax error
             *  note it and throw
             */
            log.error("#" + getName() + "(): syntax error in #" + getName() + "()-ed template '{}', called at {}",
                      arg, StringUtils.formatFileString(this));
            throw pee;
        }
        /*
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            log.error("Exception rendering #" + getName() + "({}) at {}",
                      arg, StringUtils.formatFileString(this));
            throw e;
        }
        catch ( Exception e )
        {
            String msg = "Exception rendering #" + getName() + "(" + arg + ") at " +
                         StringUtils.formatFileString(this);
            log.error(msg, e);
            throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
        }

        /*
         * Add the template name to the macro libraries list
         */
        List<Template> macroLibraries = context.getMacroLibraries();

        /*
         * if macroLibraries are not set create a new one
         */
        if (macroLibraries == null)
        {
            macroLibraries = new ArrayList<>();
        }

        context.setMacroLibraries(macroLibraries);

        /* instead of adding the name of the template, add the Template reference */
        macroLibraries.add(t);

        /*
         *  and render it
         */
        try
        {
            preRender(context);
            context.pushCurrentTemplateName(arg);

            ((SimpleNode) t.getData()).render(context, writer);
        }
        catch( StopCommand stop )
        {
            if (!stop.isFor(this))
            {
                throw stop;
            }
        }
        /*
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            /*
             * Log #parse errors so the user can track which file called which.
             */
            log.error("Exception rendering #" + getName() + "({}) at {}",
                      arg, StringUtils.formatFileString(this));
            throw e;
        }
        catch ( Exception e )
        {
            String msg = "Exception rendering #" + getName() + "(" + arg + ") at " +
                         StringUtils.formatFileString(this);
            log.error(msg, e);
            throw new VelocityException(msg, e, rsvc.getLogContext().getStackTrace());
        }
        finally
        {
            context.popCurrentTemplateName();
            postRender(context);
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
    @Override
    public void checkArgs(ArrayList<Integer> argtypes, Token t, String templateName)
      throws ParseException
    {
        if (argtypes.size() != 1)
        {
            throw new MacroParseException("The #" + getName() + " directive requires one argument",
               templateName, t);
        }

        if (argtypes.get(0) == ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("The argument to #" + getName() + " is of the wrong type",
                templateName, t);
        }
    }

    /**
     * Find the template to render in the appropriate encoding
     * @param path template path
     * @param encoding template encoding
     * @return found template
     * @throws ResourceNotFoundException if template was not found
     * @since 2.4
     */
    protected Template getTemplate(String path, String encoding) throws ResourceNotFoundException
    {
        return rsvc.getTemplate(path, encoding);
    }
}
