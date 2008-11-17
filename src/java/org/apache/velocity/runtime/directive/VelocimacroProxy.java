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

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.ProxyVMContext;
import org.apache.velocity.exception.MacroOverflowException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 *  VelocimacroProxy.java
 *
 *   a proxy Directive-derived object to fit with the current directive system
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class VelocimacroProxy extends Directive
{
    private String macroName;
    private String[] argArray = null;
    private String[] literalArgArray = null;
    private SimpleNode nodeTree = null;
    private int numMacroArgs = 0;
    private boolean preInit = false;
    private boolean strictArguments;
    private boolean localContextScope = false;
    private int maxCallDepth;

    /**
     * Return name of this Velocimacro.
     * @return The name of this Velocimacro.
     */
    public String getName()
    {
        return  macroName;
    }

    /**
     * Velocimacros are always LINE type directives.
     * @return The type of this directive.
     */
    public int getType()
    {
        return LINE;
    }

    /**
     * sets the directive name of this VM
     * 
     * @param name
     */
    public void setName(String name)
    {
        macroName = name;
    }

    /**
     * sets the array of arguments specified in the macro definition
     * 
     * @param arr
     */
    public void setArgArray(String[] arr)
    {
        argArray = arr;
        
        // for performance reasons we precache these strings - they are needed in
        // "render literal if null" functionality
        literalArgArray = new String[arr.length];
        for(int i = 0; i < arr.length; i++)
        {
            literalArgArray[i] = ".literal.$" + argArray[i];
        }

        /*
         * get the arg count from the arg array. remember that the arg array has the macro name as
         * it's 0th element
         */

        numMacroArgs = argArray.length - 1;
    }

    /**
     * @param tree
     */
    public void setNodeTree(SimpleNode tree)
    {
        nodeTree = tree;
    }

    /**
     * returns the number of ars needed for this VM
     * 
     * @return The number of ars needed for this VM
     */
    public int getNumArgs()
    {
        return numMacroArgs;
    }

    /**
     * Renders the macro using the context.
     * 
     * @param context Current rendering context
     * @param writer Writer for output
     * @param node AST that calls the macro
     * @return True if the directive rendered successfully.
     * @throws IOException
     * @throws MethodInvocationException
     * @throws MacroOverflowException
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, MethodInvocationException, MacroOverflowException
    {
        // wrap the current context and add the macro arguments

        // the creation of this context is a major bottleneck (incl 2x HashMap)
        final ProxyVMContext vmc = new ProxyVMContext(context, rsvc, localContextScope);

        int callArguments = node.jjtGetNumChildren();

        if (callArguments > 0)
        {
            // the 0th element is the macro name
            for (int i = 1; i < argArray.length && i <= callArguments; i++)
            {
                Node macroCallArgument = node.jjtGetChild(i - 1);

                /*
                 * literalArgArray[i] is needed for "render literal if null" functionality.
                 * The value is used in ASTReference render-method.
                 * 
                 * The idea is to avoid generating the literal until absolutely necessary.
                 * 
                 * This makes VMReferenceMungeVisitor obsolete and it would not work anyway 
                 * when the macro AST is shared
                 */
                vmc.addVMProxyArg(context, argArray[i], literalArgArray[i], macroCallArgument);
            }
        }

        /*
         * check that we aren't already at the max call depth
         */
        if (maxCallDepth > 0 && maxCallDepth == vmc.getCurrentMacroCallDepth())
        {
            String templateName = vmc.getCurrentTemplateName();
            Object[] stack = vmc.getMacroNameStack();

            StringBuffer out = new StringBuffer(100)
                .append("Max calling depth of ").append(maxCallDepth)
                .append(" was exceeded in Template:").append(templateName)
                .append(" and Macro:").append(macroName)
                .append(" with Call Stack:");
            for (int i = 0; i < stack.length; i++)
            {
                if (i != 0)
                {
                    out.append("->");
                }
                out.append(stack[i]);
            }
            rsvc.getLog().error(out.toString());

            try
            {
                throw new MacroOverflowException(out.toString());
            }
            finally
            {
                // clean out the macro stack, since we just broke it
                while (vmc.getCurrentMacroCallDepth() > 0)
                {
                    vmc.popCurrentMacroName();
                }
            }
        }

        try
        {
            // render the velocity macro
            vmc.pushCurrentMacroName(macroName);
            nodeTree.render(vmc, writer);
            vmc.popCurrentMacroName();
            return true;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            String msg = "VelocimacroProxy.render() : exception VM = #" + macroName + "()";
            rsvc.getLog().error(msg, e);
            throw new VelocityException(msg, e);
        }
    }

    /**
     * The major meat of VelocimacroProxy, init() checks the # of arguments.
     * 
     * @param rs
     * @param context
     * @param node
     * @throws TemplateInitException
     */
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
            throws TemplateInitException
    {
        // there can be multiple threads here so avoid double inits
        synchronized (this)
        {
            if (!preInit)
            {
                super.init(rs, context, node);

                // this is a very expensive call (ExtendedProperties is very slow)
                strictArguments = rs.getConfiguration().getBoolean(
                        RuntimeConstants.VM_ARGUMENTS_STRICT, false);

                // support for local context scope feature, where all references are local
                // we do not have to check this at every invocation of ProxyVMContext
                localContextScope = rsvc.getBoolean(RuntimeConstants.VM_CONTEXT_LOCALSCOPE, false);

                // get the macro call depth limit
                maxCallDepth = rsvc.getInt(RuntimeConstants.VM_MAX_DEPTH);

                // initialize the parsed AST
                // since this is context independent we need to do this only once so
                // do it here instead of the render method
                nodeTree.init(context, rs);

                preInit = true;
            }
        }

        // check how many arguments we got
        int i = node.jjtGetNumChildren();

        // Throw exception for invalid number of arguments?
        if (getNumArgs() != i)
        {
            // If we have a not-yet defined macro, we do get no arguments because
            // the syntax tree looks different than with a already defined macro.
            // But we do know that we must be in a macro definition context somewhere up the
            // syntax tree.
            // Check for that, if it is true, suppress the error message.
            // Fixes VELOCITY-71.

            for (Node parent = node.jjtGetParent(); parent != null;)
            {
                if ((parent instanceof ASTDirective)
                        && StringUtils.equals(((ASTDirective) parent).getDirectiveName(), "macro"))
                {
                    return;
                }
                parent = parent.jjtGetParent();
            }

            String msg = "VM #" + macroName + ": too "
                    + ((getNumArgs() > i) ? "few" : "many") + " arguments to macro. Wanted "
                    + getNumArgs() + " got " + i;

            if (strictArguments)
            {
                /**
                 * indicate col/line assuming it starts at 0 - this will be corrected one call up
                 */
                throw new TemplateInitException(msg, context.getCurrentTemplateName(), 0, 0);
            }
            else
            {
                rsvc.getLog().debug(msg);
                return;
            }
        }

        /* now validate that none of the arguments are plain words, (VELOCITY-614)
         * they should be string literals, references, inline maps, or inline lists */
        for (int n=0; n < i; n++)
        {
            Node child = node.jjtGetChild(n);
            if (child.getType() == ParserTreeConstants.JJTWORD)
            {
                /* indicate col/line assuming it starts at 0
                 * this will be corrected one call up  */
                throw new TemplateInitException("Invalid arg #"
                    + n + " in VM #" + macroName, context.getCurrentTemplateName(), 0, 0);
            }
        }
    }
}

