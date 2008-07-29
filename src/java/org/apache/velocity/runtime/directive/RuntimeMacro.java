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
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
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
 * a implementation for the macro call. Ifn a implementation cannot be
 * found the literal text is rendered.
 */

public class RuntimeMacro extends Directive
{
    /**
     * Name of the macro
     */
    private String macroName = "";

    /**
     * source template name
     */
    private String sourceTemplate = "";

    /**
     * Internal context adapter of macro caller.
     */
    private InternalContextAdapter context = null;

    /**
     * Literal text of the macro
     */
    private String literal = "";

    /**
     * Node of the macro call
     */
    private Node node = null;

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
     * just save the values to use at the rende time.
     *
     * @param rs runtime services
     * @param context InternalContexAdapter
     * @param node node conating the macro call
     */
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
    {
        super.init(rs, context, node);
        rsvc = rs;
        this.context = context;
        this.node = node;

        Token t = node.getFirstToken();

        if (t == node.getLastToken())
        {
            literal = t.image;
        }
        else
        {
            // guessing that most macros are much longer than
            // the 32 char default capacity.  let's guess 4x bigger :)
            StrBuilder text = new StrBuilder(128);
            /**
             * Retrieve the literal text
             */
            while (t != null && t != node.getLastToken())
            {
                text.append(t.image);
                t = t.next;
            }
            if (t != null)
            {
                text.append(t.image);
            }

            /**
             * Store the literal text
             */
            literal = text.toString();
        }
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
        VelocimacroProxy vmProxy = getProxy(context);
        if (vmProxy == null)
        {
            /**
             * If we cannot find an implementation write the literal text
             */
            writer.write(literal);
            return true;
        }

        /**
         * init and render the proxy
         * is the init call always necessary?
         * if so, why are we using this.context instead of context?
         */
        synchronized (vmProxy)
        {
            try
            {
                vmProxy.init(rsvc, this.context, this.node);
            }
            catch (TemplateInitException die)
            {
                Info info = new Info(sourceTemplate, node.getLine(), node.getColumn());
                throw new ParseErrorException(die.getMessage(), info);
            }
            return vmProxy.render(context, writer, node);
        }
    }

    private VelocimacroProxy getProxy(InternalContextAdapter context)
    {
        Object vm = rsvc.getVelocimacro(macroName, sourceTemplate);
        if (vm == null && context.getMacroLibraries() != null)
        {
            List libs = context.getMacroLibraries();
            for (int i = libs.size()-1; vm == null && i >= 0; i--)
            {
                vm = rsvc.getVelocimacro(macroName, (String)libs.get(i));
            }
        }
        return (VelocimacroProxy)vm;
    }

}