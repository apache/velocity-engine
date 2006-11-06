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

import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;

import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.NodeUtils;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 *   Macro.java
 *
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

        /*
         * again, don't do squat.  We want the AST of the macro
         * block to hang off of this but we don't want to
         * init it... it's useless...
         */
    }

    /**
     *  Used by Parser.java to process VMs during the parsing process.
     *
     *  This method does not render the macro to the output stream,
     *  but rather <i>processes the macro body</i> into the internal
     *  representation used by {#link
     *  org.apache.velocity.runtime.directive.VelocimacroProxy}
     *  objects, and if not currently used, adds it to the macro
     *  Factory.
     * @param rs
     * @param t
     * @param node
     * @param sourceTemplate
     * @throws IOException
     * @throws ParseException
     */
    public static void processAndRegister(RuntimeServices rs,  Token t, Node node,
                                          String sourceTemplate)
        throws IOException, ParseException
    {
        /*
         *  There must be at least one arg to  #macro,
         *  the name of the VM.  Note that 0 following
         *  args is ok for naming blocks of HTML
         */

        int numArgs = node.jjtGetNumChildren();

        /*
         *  this number is the # of args + 1.  The + 1
         *  is for the block tree
         */

        if (numArgs < 2)
        {

            /*
             *  error - they didn't name the macro or
             *  define a block
             */

            rs.getLog().error("#macro error : Velocimacro must have name as 1st " +
                              "argument to #macro(). #args = " + numArgs);

            throw new MacroParseException("First argument to #macro() must be " +
                    " macro name.", sourceTemplate, t);
        }

        /*
         *  lets make sure that the first arg is an ASTWord
         */

        int firstType = node.jjtGetChild(0).getType();

        if(firstType != ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("First argument to #macro() must be a"
                    + " token without surrounding \' or \", which specifies"
                    + " the macro name.  Currently it is a "
                    + ParserTreeConstants.jjtNodeName[firstType], sourceTemplate, t);
        }

        /*
         *  get the arguments to the use of the VM
         */

        String argArray[] = getArgArray(node, rs);

        /*
         *   now, try and eat the code block. Pass the root.
         */

        List macroArray =
            getASTAsStringArray(node.jjtGetChild(numArgs - 1));

        /*
         *  make a big string out of our macro
         */

        StringBuffer macroBody = new StringBuffer();

        for (int i=0; i < macroArray.size(); i++)
        {
            macroBody.append(macroArray.get(i));
        }

        /*
         * now, try to add it.  The Factory controls permissions,
         * so just give it a whack...
         */

        boolean macroAdded = rs.addVelocimacro(argArray[0],
                                               macroBody.toString(),
                                               argArray, sourceTemplate);

        if (!macroAdded && rs.getLog().isWarnEnabled())
        {
            StringBuffer msg = new StringBuffer("Failed to add macro: ");
            macroToString(msg, argArray);
            msg.append(" : source = ").append(sourceTemplate);
            rs.getLog().warn(msg);
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
     *  Returns an array of the literal rep of the AST
     *  @param rootNode
     *  @return list of Strings
     */
    private static List getASTAsStringArray(Node rootNode)
    {
        /*
         *  this assumes that we are passed in the root
         *  node of the code block
         */

        Token t = rootNode.getFirstToken();
        Token tLast = rootNode.getLastToken();

        /*
         *  now, run down the part of the tree bounded by
         *  our first and last tokens
         */

        List list = new ArrayList();

        while (t != tLast)
        {
            list.add(NodeUtils.tokenLiteral(t));
            t = t.next;
        }

        /*
         *  make sure we get the last one...
         */

        list.add(NodeUtils.tokenLiteral(t));

        return list;
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
