package org.apache.velocity.runtime.directive;

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

import java.util.List;
import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;

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
 * @version $Id: Macro.java,v 1.16.4.1 2004/03/03 23:22:56 geirm Exp $
 */
public class Macro extends Directive
{
    private static  boolean debugMode = false;

    /**
     * Return name of this directive.
     */
    public String getName()
    {
        return "macro";
    }        
    
    /**
     * Return type of this directive.
     */
    public int getType()
    {
        return BLOCK;
    }        
    
    /**
     *   render() doesn't do anything in the final output rendering.
     *   There is no output from a #macro() directive.
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
 
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
       throws Exception
    {
        super.init(rs, context, node);

        /*
         * again, don't do squat.  We want the AST of the macro 
         * block to hang off of this but we don't want to 
         * init it... it's useless...
         */
     
        return;
    }

    /**
     *  Used by Parser.java to process VMs withing the parsing process
     *
     *  processAndRegister() doesn't actually render the macro to the output
     *  Processes the macro body into the internal representation used by the
     *  VelocimacroProxy objects, and if not currently used, adds it
     *  to the macro Factory
     */ 
    public static void processAndRegister(RuntimeServices rs,  Node node,
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
            
            rs.error("#macro error : Velocimacro must have name as 1st " + 
                "argument to #macro(). #args = " + numArgs);

            throw new MacroParseException("First argument to #macro() must be " +
                    " macro name.");
        }

        /*
         *  lets make sure that the first arg is an ASTWord
         */

        int firstType = node.jjtGetChild(0).getType();

        if(firstType != ParserTreeConstants.JJTWORD)
        {
            Token t = node.jjtGetChild(0).getFirstToken();

            throw new MacroParseException("First argument to #macro() must be a"
                    + " token without surrounding \' or \", which specifies"
                    + " the macro name.  Currently it is a "
                    + ParserTreeConstants.jjtNodeName[firstType]);

        }

        /*
         *  get the arguments to the use of the VM
         */

        String argArray[] = getArgArray(node);
	 
        /*
         *   now, try and eat the code block. Pass the root.
         */
        
        List macroArray = 
            getASTAsStringArray(node.jjtGetChild(numArgs - 1));
  
        /*
         *  make a big string out of our macro
         */
  
        StringBuffer temp  = new StringBuffer();

        for (int i=0; i < macroArray.size(); i++)
        {
            temp.append(macroArray.get(i));
        }

        String macroBody = temp.toString();
   
        /*
         * now, try to add it.  The Factory controls permissions, 
         * so just give it a whack...
         */

        boolean bRet = rs.addVelocimacro(argArray[0], macroBody,
                        argArray, sourceTemplate);

        return;
    }

  
    /**
     *  creates an array containing the literal
     *  strings in the macro arguement
     */
    private static String[] getArgArray(Node node)
    {
        /*
         *  remember : this includes the block tree
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
            System.out.println("Macro.getArgArray() : #args = " + numArgs);
            System.out.print(argArray[0] + "(");
	    
            for (i = 1; i < numArgs; i++)
            {
                System.out.print(" " + argArray[i]);
            }

            System.out.println(" )");
        }
	
        return argArray;
    }

    /**
     *  Returns an array of the literal rep of the AST
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

        ArrayList list = new ArrayList();

        t = rootNode.getFirstToken();

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
}
