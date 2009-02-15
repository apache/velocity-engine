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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 *  Macro implements the macro definition directive of VTL.
 *
 *  example :
 *
 *  #macro( isnull $i )
 *     #if( $i )
 *         $i
 *      #end
 *  #end
 *
 *  This object is used at parse time to mainly process and register the
 *  macro.  It is used inline in the parser when processing a directive.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class Macro extends Directive
{
    private static  boolean debugMode = false;

    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "macro";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    public int getType()
    {
        return BLOCK;
    }

    /**
     *   render() doesn't do anything in the final output rendering.
     *   There is no output from a #macro() directive.
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     */
    public boolean render(InternalContextAdapter context,
                           Writer writer, Node node)
        throws IOException
    {
        /*
         *  do nothing : We never render.  The VelocimacroProxy object does that
         */

        return true;
    }

    /**
     * @see org.apache.velocity.runtime.directive.Directive#init(org.apache.velocity.runtime.RuntimeServices, org.apache.velocity.context.InternalContextAdapter, org.apache.velocity.runtime.parser.node.Node)
     */
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
       throws TemplateInitException
    {
        super.init(rs, context, node);

        
        // Add this macro to the VelocimacroManager now that it has been initialized.        
        String argArray[] = getArgArray(node, rs);
        int numArgs = node.jjtGetNumChildren();
        rs.addVelocimacro(argArray[0], node.jjtGetChild(numArgs - 1), argArray, node.getTemplateName());
    }
    
    /**
     * Check the argument types of a macro call, called by the parser to do validation
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
        throws ParseException
    {        
        if (argtypes.size() < 1)
        {
            throw new MacroParseException("A macro definition requires at least a macro name"
                , templateName, t);
        }

        /*
         *  lets make sure that the first arg is an ASTWord
         */
        if(argtypes.get(0) != ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("Macro argument 1"
                    + " must be a token without surrounding \' or \""
                    , templateName, t);
        }
        
        // All arguments other then the first must be a reference
        for (int argPos = 1; argPos < argtypes.size(); argPos++)
        {
            if (argtypes.get(argPos) != ParserTreeConstants.JJTREFERENCE)
            {
                throw new MacroParseException("Macro argument " + (argPos + 1)
                  + " must be a reference", templateName, t);
            }
        }
    }

    /**
     * Creates an array containing the literal text from the macro
     * arguement(s) (including the macro's name as the first arg).
     *
     * @param node The parse node from which to grok the argument
     * list.  It's expected to include the block node tree (for the
     * macro body).
     * @param rsvc For debugging purposes only.
     * @return array of arguments
     */
    private static String[] getArgArray(Node node, RuntimeServices rsvc)
    {
        /*
         * Get the number of arguments for the macro, excluding the
         * last child node which is the block tree containing the
         * macro body.
         */
        int numArgs = node.jjtGetNumChildren();
        numArgs--;  // avoid the block tree...

        String argArray[] = new String[numArgs];

        int i = 0;

        /*
         *  eat the args
         */

        while (i < numArgs)
        {
            argArray[i] = node.jjtGetChild(i).getFirstToken().image;

            /*
             *  trim off the leading $ for the args after the macro name.
             *  saves everyone else from having to do it
             */

            if (i > 0)
            {
                if (argArray[i].startsWith("$"))
                {
                    argArray[i] = argArray[i]
                        .substring(1, argArray[i].length());
                }
            }

            argArray[i] = argArray[i].intern();
            i++;
        }

        if (debugMode)
        {
            StringBuffer msg = new StringBuffer("Macro.getArgArray() : nbrArgs=");
            msg.append(numArgs).append(" : ");
            macroToString(msg, argArray);
            rsvc.getLog().debug(msg);
        }

        return argArray;
    }

    /**
     * For debugging purposes.  Formats the arguments from
     * <code>argArray</code> and appends them to <code>buf</code>.
     *
     * @param buf A StringBuffer. If null, a new StringBuffer is allocated.
     * @param argArray The Macro arguments to format
     *
     * @return A StringBuffer containing the formatted arguments. If a StringBuffer
     *         has passed in as buf, this method returns it.
     * @since 1.5
     */
    public static final StringBuffer macroToString(final StringBuffer buf,
                                                   final String[] argArray)
    {
        StringBuffer ret = (buf == null) ? new StringBuffer() : buf;

        ret.append('#').append(argArray[0]).append("( ");
        for (int i = 1; i < argArray.length; i++)
        {
            ret.append(' ').append(argArray[i]);
        }
        ret.append(" )");
        return ret;
    }    
    
}
