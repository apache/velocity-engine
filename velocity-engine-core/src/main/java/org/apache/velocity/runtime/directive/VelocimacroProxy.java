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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MacroOverflowException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.Renderable;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Macro.MacroArg;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

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
    private List<Macro.MacroArg> macroArgs = null;
    private String[] literalArgArray = null;
    private SimpleNode nodeTree = null;
    private int numMacroArgs = 0;
    private boolean strictArguments;
    private int maxCallDepth;
    private String bodyReference;

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
     * @param args  Array of macro arguments, containing the
     *        #macro() arguments and default values.  the 0th is the name.
     */
    public void setMacroArgs(List<Macro.MacroArg> args)
    {
        macroArgs = args;
        
        // for performance reasons we precache these strings - they are needed in
        // "render literal if null" functionality
        literalArgArray = new String[macroArgs.size()];
        for(int i = 0; i < macroArgs.size(); i++)
        {
            literalArgArray[i] = ".literal.$" + macroArgs.get(i);
        }

        /*
         * get the arg count from the arg array. remember that the arg array has the macro name as
         * it's 0th element
         */

        numMacroArgs = macroArgs.size() - 1;
    }

    /**
     * Return the list of macro arguments associated with this macro
     */
    public List<Macro.MacroArg> getMacroArgs()
    {
      return macroArgs;
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
     * Initialize members of VelocimacroProxy.  called from MacroEntry
     */
    public void init(RuntimeServices rs)
    {
        rsvc = rs;
        log = rs.getLog("macro");
      
        // this is a very expensive call (ExtendedProperties is very slow)
        strictArguments = rsvc.getConfiguration().getBoolean(
            RuntimeConstants.VM_ARGUMENTS_STRICT, false);

        // get the macro call depth limit
        maxCallDepth = rsvc.getInt(RuntimeConstants.VM_MAX_DEPTH);

        // get name of the reference that refers to AST block passed to block macro call
        bodyReference = rsvc.getString(RuntimeConstants.VM_BODY_REFERENCE, "bodyContent");
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node)
        throws IOException
    {
        return render(context, writer, node, null);
    }

    /**
     * Renders the macro using the context.
     * 
     * @param context Current rendering context
     * @param writer Writer for output
     * @param node AST that calls the macro
     * @param body the macro body
     * @return true if the directive rendered successfully.
     * @throws IOException
     */
    public boolean render(InternalContextAdapter context, Writer writer,
                          Node node, Renderable body)
        throws IOException
    {
        int callArgNum = node.jjtGetNumChildren();
        
        // if this macro was invoked by a call directive, we might have a body AST here. 
        Object oldBodyRef = null;
        if (body != null)
        {
            oldBodyRef = context.get(bodyReference);
            context.put(bodyReference, body);
            callArgNum--;  // Remove the body AST from the arg count
        }

        // is everything copacetic?
        checkArgumentCount(node, callArgNum);
        checkDepth(context);

        // put macro arg values and save the returned old/new value pairs
        Object[] values = handleArgValues(context, node, callArgNum);
        try
        {
            // render the velocity macro
            context.pushCurrentMacroName(macroName);
            nodeTree.render(context, writer);
            context.popCurrentMacroName();
            return true;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            String msg = "VelocimacroProxy.render() : exception VM = #" + macroName + "()";
            log.error(msg, e);
            throw new VelocityException(msg, e);
        }
        finally
        {
            // clean up after the args and bodyRef
            // but only if they weren't overridden inside
            Object current = context.get(bodyReference);
            if (current == body)
            {
                if (oldBodyRef != null)
                {
                    context.put(bodyReference, oldBodyRef);
                }
                else
                {
                    context.remove(bodyReference);
                }
            }

            for (int i = 1; i < macroArgs.size(); i++)
            {
                MacroArg macroArg = macroArgs.get(i);
                current = context.get(macroArg.name);
                if (current == values[(i-1) * 2 + 1])
                {
                    Object old = values[(i-1) * 2];
                    if (old != null)
                    {
                        context.put(macroArg.name, old);
                    }
                    else
                    {
                        context.remove(macroArg.name);
                    }
                }
            }
        }
    }
    
    /**
     * Check whether the number of arguments given matches the number defined.
     */
    protected void checkArgumentCount(Node node, int callArgNum)
    {
        // Check if we have more calling arguments then the macro accepts
        if (callArgNum > macroArgs.size() - 1)
        {
            if (strictArguments)
            {
                throw new VelocityException("Provided " + callArgNum + " arguments but macro #" 
                    + macroArgs.get(0).name + " accepts at most " + (macroArgs.size()-1)
                    + " at " + StringUtils.formatFileString(node));
            }
            else if (log.isDebugEnabled())
            {
                // Backward compatibility logging, Mainly for MacroForwardDefinedTestCase
                log.debug("VM #" + macroArgs.get(0).name
                    + ": too many arguments to macro. Wanted " + (macroArgs.size()-1) 
                    + " got " + callArgNum);
            }
        }
    }

    /**
     * check that we aren't already at the max call depth and throws
     * a MacroOverflowException if we are there.
     */
    protected void checkDepth(InternalContextAdapter context)
    {
        if (maxCallDepth > 0 && maxCallDepth == context.getCurrentMacroCallDepth())
        {
            String[] stack = context.getMacroNameStack();

            StringBuffer out = new StringBuffer(100)
                .append("Max calling depth of ").append(maxCallDepth)
                .append(" was exceeded in macro '").append(macroName)
                .append("' with Call Stack:");
            for (int i = 0; i < stack.length; i++)
            {
                if (i != 0)
                {
                    out.append("->");
                }
                out.append(stack[i]);
            }
            out.append(" at " + StringUtils.formatFileString(this));
            log.error(out.toString());
            
            // clean out the macro stack, since we just broke it
            while (context.getCurrentMacroCallDepth() > 0)
            {
                context.popCurrentMacroName();
            }
            throw new MacroOverflowException(out.toString());
        }
    }

    /**
     * Gets the macro argument values and puts them in the context under
     * the argument names.  Store and return an array of old and new values
     * paired for each argument name, for later cleanup.
     */
    protected Object[] handleArgValues(InternalContextAdapter context,
                                         Node node, int callArgNum)
    {
    	// Changed two dimensional array to single dimensional to optimize memory lookups
        Object[] values = new Object[macroArgs.size() * 2];
          
        // Move arguments into the macro's context. Start at one to skip macro name
        for (int i = 1; i < macroArgs.size(); i++)
        {
            MacroArg macroArg = macroArgs.get(i);
            values[(i-1) * 2] = context.get(macroArg.name);

            // put the new value in
            Object newVal = null;
            if (i - 1 < callArgNum)
            {
                // There's a calling value.
                newVal = node.jjtGetChild(i - 1).value(context);
            }
            else if (macroArg.defaultVal != null)
            {
                // We don't have a calling value, but the macro defines a default value
                newVal = macroArg.defaultVal.value(context);
            }
            else if (strictArguments)
            {
                // We come to this point if we don't have a calling value, and
                // there is no default value. Not enough arguments defined.
                int minArgNum = -1; //start at -1 to skip the macro name
                // Calculate minimum number of args required for macro
                for (MacroArg marg : macroArgs)
                {
                    if (marg.defaultVal == null) minArgNum++;
                }
                throw new VelocityException("Need at least " + minArgNum + " argument for macro #"
                    + macroArgs.get(0).name + " but only " + callArgNum + " where provided at "
                    + StringUtils.formatFileString(node));
            }
            else
            {
                // Backward compatibility logging, Mainly for MacroForwardDefinedTestCase
                if (log.isDebugEnabled())
                {
                    log.debug("VM #" + macroArgs.get(0).name
                     + ": too few arguments to macro. Wanted " + (macroArgs.size()-1) 
                     + " got " + callArgNum);
                }
                break;
            }

            context.put(macroArg.name, newVal);
            values[(i-1) * 2 + 1] = newVal;
        }

        // return the array of replaced and new values
        return values;
    }
    
}

