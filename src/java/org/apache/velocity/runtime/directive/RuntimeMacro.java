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

import org.apache.commons.lang.text.StrBuilder;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.util.introspection.Info;

import java.io.Writer;
import java.io.IOException;
import java.util.List;

/**
 * This class acts as a proxy for potential macros.  When the AST is built
 * this class is inserted as a placeholder for the macro (whether or not
 * the macro is actually defined).  At render time we check whether there is
 * a implementation for the macro call. If an implementation cannot be
 * found the literal text is rendered.
 * @since 1.6
 */
public class RuntimeMacro extends Directive
{
    /**
     * Name of the macro
     */
    private String macroName;

    /**
     * source template name
     */
    private String sourceTemplate;

    /**
     * Literal text of the macro
     */
    private String literal = null;

    /**
     * Node of the macro call
     */
    private Node node = null;

    /**
     * Indicates if we are running in strict reference mode.
     */
    protected boolean strictRef = false;
    
    /**
     * Create a RuntimeMacro instance. Macro name and source
     * template stored for later use.
     *
     * @param macroName name of the macro
     * @param sourceTemplate template where macro call is made
     */
    public RuntimeMacro(String macroName, String sourceTemplate)
    {
        if (macroName == null || sourceTemplate == null)
        {
            throw new IllegalArgumentException("Null arguments");
        }
        
        this.macroName = macroName;
        this.sourceTemplate = sourceTemplate;
    }

    /**
     * Return name of this Velocimacro.
     *
     * @return The name of this Velocimacro.
     */
    public String getName()
    {
        return macroName;
    }

    /**
     * Velocimacros are always LINE
     * type directives.
     *
     * @return The type of this directive.
     */
    public int getType()
    {
        return LINE;
    }


    /**
     * Intialize the Runtime macro. At the init time no implementation so we
     * just save the values to use at the render time.
     *
     * @param rs runtime services
     * @param context InternalContextAdapter
     * @param node node containing the macro call
     */
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
    {
        super.init(rs, context, node);
        rsvc = rs;
        this.node = node;
        
        /**
         * Only check for strictRef setting if this really looks like a macro,
         * so strict mode doesn't balk at things like #E0E0E0 in a template.
         */
        Token t = node.getLastToken();
        if (t.image.charAt(0) == ')')
        {
            strictRef = rsvc.getBoolean(RuntimeConstants.RUNTIME_REFERENCES_STRICT, false);
        }
    }

    /**
     * It is probably quite rare that we need to render the macro literal
     * so do it only on-demand and then cache the value. This tactic helps to
     * reduce memory usage a bit.
     */
    private String getLiteral()
    {
        if (literal == null)
        {
            StrBuilder buffer = new StrBuilder();
            Token t = node.getFirstToken();

            while (t != null && t != node.getLastToken())
            {
                buffer.append(t.image);
                t = t.next;
            }

            if (t != null)
            {
                buffer.append(t.image);
            }

            literal = buffer.toString();
        }
        return literal;
    }
    

    /**
     * Velocimacro implementation is not known at the init time. So look for
     * a implementation in the macro libaries and if finds one renders it. The
     * actual rendering is delegated to the VelocimacroProxy object. When
     * looking for a macro we first loot at the template with has the
     * macro call then we look at the macro lbraries in the order they appear
     * in the list. If a macro has many definitions above look up will
     * determine the precedence.
     *
     * @param context
     * @param writer
     * @param node
     * @return true if the rendering is successfull
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     */
    public boolean render(InternalContextAdapter context, Writer writer,
                          Node node)
            throws IOException, ResourceNotFoundException,
            ParseErrorException, MethodInvocationException
    {
        VelocimacroProxy vmProxy = null;
        String renderingTemplate = context.getCurrentTemplateName();
        
        /**
         * first look in the source template
         */
        Object o = rsvc.getVelocimacro(macroName, sourceTemplate, renderingTemplate);

        if( o != null )
        {
            // getVelocimacro can only return a VelocimacroProxy so we don't need the
            // costly instanceof check
            vmProxy = (VelocimacroProxy)o;
        }

        /**
         * if not found, look in the macro libraries.
         */
        if (vmProxy == null)
        {
            List macroLibraries = context.getMacroLibraries();
            if (macroLibraries != null)
            {
                for (int i = macroLibraries.size() - 1; i >= 0; i--)
                {
                    o = rsvc.getVelocimacro(macroName,
                            (String)macroLibraries.get(i), renderingTemplate);

                    // get the first matching macro
                    if (o != null)
                    {
                        vmProxy = (VelocimacroProxy) o;
                        break;
                    }
                }
            }
        }

        if (vmProxy != null)
        {
            try
            {
            	// mainly check the number of arguments
                vmProxy.init(rsvc, context, node);
            }
            catch (TemplateInitException die)
            {
                Info info = new Info(sourceTemplate, node.getLine(), node.getColumn());
                throw new ParseErrorException(die.getMessage() + " at "
                    + Log.formatFileString(info), info);
            }

            try
            {
                return vmProxy.render(context, writer, node);
            }
            catch (RuntimeException e)
            {
                /**
                 * We catch, the exception here so that we can record in
                 * the logs the template and line number of the macro call
                 * which generate the exception.  This information is
                 * especially important for multiple macro call levels.
                 * this is also true for the following catch blocks.
                 */
                rsvc.getLog().error("Exception in macro #" + macroName + " at " +
                  Log.formatFileString(sourceTemplate, getLine(), getColumn()));
                throw e;
            }
            catch (IOException e)
            {
                rsvc.getLog().error("Exception in macro #" + macroName + " at " +
                  Log.formatFileString(sourceTemplate, getLine(), getColumn()));
                throw e;
            }
        }
        else if (strictRef)
        {
            Info info = new Info(sourceTemplate, node.getLine(), node.getColumn());
            throw new ParseErrorException("Macro '#" + macroName + "' is not defined at "
                + Log.formatFileString(info), info);
        }
        
        /**
         * If we cannot find an implementation write the literal text
         */
        writer.write(getLiteral());
        return true;
    }
}
