package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
import java.util.TreeMap;

import org.apache.velocity.Context;

import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.RuntimeConstants;

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
 * @version $Id: Macro.java,v 1.8 2000/12/20 07:38:16 jvanzyl Exp $
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
    public boolean render(Context context, Writer writer, Node node)
        throws IOException 
    {
        /*
         *  do nothing : We never render.  The VelocimacroProxy object does that
         */

        return true;
    }
 
    public void init(Context context, Node node) 
       throws Exception
    {
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
    public void processAndRegister( Node node, String sourceTemplate )
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
            
            Runtime.error("#macro error : Velocimacro must have name as 1st " + 
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
        
        String macroArray[] = getASTAsStringArray( node.jjtGetChild( numArgs - 1) );
    
        /*
         *  make a big string out of our macro
         */
  
        StringBuffer temp  = new StringBuffer();
        
        for( int i=0; i < macroArray.length; i++)
            temp.append( macroArray[i] );

        String macroBody = temp.toString();    
    
        /*
         * now, using the macro body string and the arg list, index 
         * all the tokens in the arglist
         */

        TreeMap argIndexMap = getArgIndexMap( macroBody, argArray );

        /*
         * now, try to add it.  The Factory controls permissions, 
         * so just give it a whack...
         */

        boolean bRet = Runtime.addVelocimacro( argArray[0], macroBody, 
            argArray, macroArray, argIndexMap, sourceTemplate );

        return;
    }

    /**
     * using the macro body and the arg list, creates a TreeMap 
     * of the indices of the args in the body Makes for fast 
     * and efficient patching at runtime
     */
    private TreeMap getArgIndexMap( String macroBody, String argArray[] )
    {
        TreeMap tm = new TreeMap();
 
        /*
         *  run through the buffer for each paramter, and remember 
         * where they go.  We have to do this  all at once to 
         * avoid confusing later replacement attempts with the 
         * activity of earlier ones
         */

        for (int i=1; i<argArray.length; i++)
        {
            /*
             *  keep going until we don't get any matches
             */

            int index = 0;

            while( ( index = macroBody.indexOf( argArray[i], index )) != -1 )
            {
                tm.put(new Integer( index ), new Integer( i ));
                index++;
            }                    
        }

        return tm;
    }

    /**
     *  creates an array containing the literal
     *  strings in the macro arguement
     */
    private String[] getArgArray( Node node )
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
    private String [] getASTAsStringArray( Node rootNode )
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









