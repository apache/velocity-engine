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

/**
 *  VelocimacroProxy.java
 *
 *   a proxy Directive-derived object to fit with the current directive system
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelocimacroProxy.java,v 1.4 2000/11/24 23:35:57 jon Exp $ 
 */

package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;

import org.apache.velocity.Context;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

public class VelocimacroProxy extends  Directive
{
    private String strMacroName_ = "";
    private String strMacro_ = "";
    private String[] strArgArray_ = null;
    private String[] strMacroArray_ = null;
    private TreeMap  tmArgIndexMap_ = null;
    private SimpleNode nodeTree_ = null;

    public String getName() { return  strMacroName_; }
    public int getType() { return LINE; }
 
    /**
     *   sets the directive name of this VM
     */
    public void setName( String strName )
    {
        strMacroName_ = strName;
    }
   
    /**
     *  sets the array of arguments specified in the macro definition
     */
    public void setArgArray( String [] strArray )
    {
        strArgArray_ = strArray;
    }

    /**
     *   sets the array of macro elements. (The macro body is an array of token literals..)
     *   Currently, this isn't used in init or render, but keeping it around if
     *   someone needs it later.
     */
    public void setMacroArray( String [] strArray )
    {
        strMacroArray_ = strArray;
    }

    /**
     *   sets the ArgIndexMap, a map of indexes of macro arg locations in the macro string
     *   to the argument index.  Makes patching the macro really easy.
     */
    public void setArgIndexMap( TreeMap tm)
    {
        tmArgIndexMap_ = tm;
    }

    /**
     *   Sets the orignal macro body.  This is simply the cat of the strMacroArray, but the 
     *   Macro object creates this once during parsing, and everyone shares it.
     *   Note : it must not be modified.
     */
    public void setMacrobody( String strMacro )
    {
        strMacro_ = strMacro;
    }

    /**
     *   Renders the macro using the context
     */
    public boolean render(Context context, Writer writer, Node node)
        throws IOException
    {
        try 
        {
            if (nodeTree_ != null)
                nodeTree_.render(context, writer );
            else
                Runtime.error( "VM error : " + strMacroName_ + ". Null AST");
        } 
        catch ( Exception e ) 
        {
            Runtime.error("VelocimacroProxy.render() : exception " + strMacroName_ + 
            " : "  + StringUtils.stackTrace(e));
        }

        return true;
    }

    /**
     *   The major meat of VelocimacroProxy, init() checks the # of arguments, patches the
     *   macro body, renders the macro into an AST, and then inits the AST, so it is ready 
     *   for quick rendering.
     */
    public void init(Context context, Node node) 
       throws Exception
    {
         /*
         *  how many args did we get?
         */

        int i  = node.jjtGetNumChildren();
        
        /*
         *  get the arg count from the arg array.  remember that the arg array 
         *  has the macro name as it's 0th element
         *   if we don't have enough children specified...?
         */

        int iArgCount = strArgArray_.length - 1;
        
        if ( iArgCount != i ) 
        {
            Runtime.error("VM : error : too few arguments to macro. Wanted " 
                         + iArgCount + " got " + i + "  -->");
            return;
        }

        /*
         *  get the argument list to the instance use of the VM
         */

        String strCallingArgs[] = getArgArray( node );
         
        /*
         *  now, expand our macro out to a string patched with the instance arguments
         */
       
        StringBuffer strExpanded = expandMacroArray( strCallingArgs );
        
        /*
         *  ok. I have the expanded macro.
         *  now, all I have to do  is let the parser render it
         */

        try 
        {
            /*
             *  take the patched macro code, and render() and init()
             */

            //System.out.println("Expanded : " + strExpanded.toString() );

            ByteArrayInputStream  inStream = new ByteArrayInputStream( strExpanded.toString().getBytes() );
            nodeTree_ = Runtime.parse( inStream );
            nodeTree_.init( context, null );
        } 
        catch ( Exception e ) 
        {
            Runtime.error("VelocimacroProxy.init() : exception " + strMacroName_ + 
            " : "  + StringUtils.stackTrace(e));
        }

        /*
         *  we're done.  We have the correct AST
         */

       return;
    }

    /**
     *   gets the args to the VM from the instance-use AST
     */
    private String[] getArgArray( Node node )
    {
        int iNumArgs = node.jjtGetNumChildren();
        
        String strArgs[] = new String[ iNumArgs ];
		
        /*
         *  eat the args
         */
    
        int i = 0;
        Token t = null;
        Token tLast = null;
    
        while( i <  iNumArgs ) 
        {
            t = node.jjtGetChild(i).getFirstToken();
            tLast = node.jjtGetChild(i).getLastToken();
            strArgs[i] = "";

            while( t != tLast ) 
            {
                strArgs[i] += t.image;
                t = t.next;
            }
            
            strArgs[i] += t.image;
            i++;
         }
	      
        return strArgs;
    }
   
  
    /**
     *   expands our macro out given our arg list. Uses
     *   the pre-created arg index map to run through and patch
     *   with our args
     */
    private StringBuffer expandMacroArray( String [] strCallingArgs )
    {
        /*
         *  build the output string by running through the map in sorted order and construct
         *  the new string.  Remember, don't modify the strMacro. Make a new one. The index
         *  elements are specific to the orignal macro body..
         */

        Set set = tmArgIndexMap_.keySet();
        Iterator it = set.iterator();
        StringBuffer sbNew = new StringBuffer();
        int iLoc = 0;

        while( it.hasNext() )
        {
            Integer iIndex = (Integer) it.next();
            int iWhich = ((Integer) tmArgIndexMap_.get( iIndex )).intValue();

            int iIndexInt = iIndex.intValue();

            //System.out.println( sbNew + ":" + strCallingArgs[iWhich-1]);

            sbNew.append( strMacro_.substring( iLoc, iIndexInt ));
            sbNew.append( strCallingArgs[iWhich - 1] );
            iLoc = iIndexInt + strArgArray_[iWhich].length();
        }
 
        /*
         *  and finish off the string
         */

        sbNew.append( strMacro_.substring( iLoc ) );

        return sbNew;
    }
}










