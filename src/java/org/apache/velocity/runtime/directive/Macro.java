package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.context.InternalContextAdapter;

import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.parser.Token;
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
 * @version $Id: Macro.java,v 1.13 2001/08/07 21:57:56 geirm Exp $
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
    public boolean render( InternalContextAdapter context, 
                           Writer writer, Node node)
        throws IOException 
    {
        /*
         *  do nothing : We never render.  The VelocimacroProxy object does that
         */

        return true;
    }
 
    public void init( RuntimeServices rs, InternalContextAdapter context, Node node) 
       throws Exception
    {
        super.init( rs, context, node );

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
    public static  void processAndRegister( RuntimeServices rs,  Node node, String sourceTemplate )
        throws IOException
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
                "argument to #macro()");
            
            return;
        }

        /*
         *  get the arguments to the use of the VM
         */

        String argArray[] = getArgArray( node );
	 
        /*
         *   now, try and eat the code block. Pass the root.
         */
        
        String macroArray[] = 
            getASTAsStringArray( node.jjtGetChild( numArgs - 1) );
    
        /*
         *  make a big string out of our macro
         */
  
        StringBuffer temp  = new StringBuffer();
        
        for( int i=0; i < macroArray.length; i++)
            temp.append( macroArray[i] );

        String macroBody = temp.toString();    
   
        /*
         * now, try to add it.  The Factory controls permissions, 
         * so just give it a whack...
         */

        boolean bRet = rs.addVelocimacro( argArray[0], macroBody,  
                        argArray, sourceTemplate );

        return;
    }

  
    /**
     *  creates an array containing the literal
     *  strings in the macro arguement
     */
    private static String[] getArgArray( Node node )
    {
        /*
         *  remember : this includes the block tree
         */
        
        int numArgs = node.jjtGetNumChildren();
	
        numArgs--;  // avoid the block tree...
	
        String argArray[] = new String[ numArgs ];
	
        int i = 0;
	
        /*
         *  eat the args
         */
	
        while( i <  numArgs ) 
        {
            argArray[i] = node.jjtGetChild(i).getFirstToken().image;

            /*
             *  trim off the leading $ for the args after the macro name.
             *  saves everyone else from having to do it
             */

            if ( i > 0)
            {
                if ( argArray[i].startsWith("$"))
                    argArray[i] = argArray[i]
                        .substring(1, argArray[i].length());
            }

            i++;
        }
	
        if ( debugMode ) 
        {
            System.out.println("Macro.getArgArray() : #args = " + numArgs );
            System.out.print( argArray[0] + "(" );
	    
            for (  i = 1; i < numArgs; i++) 
                System.out.print(" " + argArray[i] );
	    
            System.out.println(" )");
        }
	
        return argArray;
    }

   /**
     *  Returns an array of the literal rep of the AST
     */
    private static String [] getASTAsStringArray( Node rootNode )
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

        int count = 0;
        
        //! Should this use the node.literal() ?
        
        while( t != null && t != tLast ) 
        {
            count++;
            t = t.next;
        }

        /*
         *  account for the last one
         */

        count++;

        /*
         *  now, do it for real
         */

        String arr[] = new String[count];

        count = 0;
        t = rootNode.getFirstToken();

        while( t != tLast ) 
        {
            arr[count++] = t.image;
            t = t.next;
        }

        /*
         *  make sure we get the last one...
         */

        arr[count] = t.image;

        return arr;
    }
}









