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
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

/**
 *  VelocimacroProxy.java
 *
 *   a proxy Directive-derived object to fit with the current directive system
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelocimacroProxy.java,v 1.16 2000/12/20 07:41:11 jvanzyl Exp $ 
 */
public class VelocimacroProxy extends Directive
{
    private String macroName = "";
    private String macroBody = "";
    private String[] argArray = null;
    private String[] macroArray = null;
    private TreeMap  argIndexMap = null;
    private SimpleNode nodeTree = null;
    private int numMacroArgs = 0;

    private boolean init = false;

    /**
     * Return name of this Velocimacro.
     */
    public String getName() 
    { 
        return  macroName; 
    }
    
    /**
     * Velocimacros are always LINE
     * type directives.
     */
    public int getType()
    { 
        return LINE; 
    }
 
    /**
     *   sets the directive name of this VM
     */
    public void setName( String name )
    {
        macroName = name;
    }
   
    /**
     *  sets the array of arguments specified in the macro definition
     */
    public void setArgArray( String [] arr )
    {
        argArray = arr;

        /*
         *  get the arg count from the arg array.  remember that the arg array 
         *  has the macro name as it's 0th element
         */
        numMacroArgs = argArray.length - 1;
    }

    /**
     *  returns the number of ars needed for this VM
     */
    public int getNumArgs()
    {
        return numMacroArgs;
    }

    /**
     *   sets the array of macro elements. (The macro body is an array of token literals..)
     *   Currently, this isn't used in init or render, but keeping it around if
     *   someone needs it later.
     */
    public void setMacroArray( String [] arr )
    {
        macroArray = arr;
    }

    /**
     *   sets the argIndexMap, a map of indexes of macro arg locations in the macro string
     *   to the argument index.  Makes patching the macro really easy.
     */
    public void setArgIndexMap( TreeMap tm)
    {
        argIndexMap = tm;
    }

    /**
     *   Sets the orignal macro body.  This is simply the cat of the macroArray, but the 
     *   Macro object creates this once during parsing, and everyone shares it.
     *   Note : it must not be modified.
     */
    public void setMacrobody( String mb )
    {
        macroBody = mb;
    }

    /**
     *   Renders the macro using the context
     */
    public boolean render(Context context, Writer writer, Node node)
        throws IOException
    {
        try 
        {
            if (nodeTree != null)
            {
                /*
                 *  to allow recursive VMs, we want to init them at render time, not init time
                 *  or else you wander down the VM calls forever.
                 *
                 *  need a context here to carry the template name down through the init
                 */
                if (!init)
                {
                    Context c = new Context();
                    c.setCurrentTemplateName( context.getCurrentTemplateName() );
                        
                    nodeTree.init( c ,null);
                    init = true;
                }
                nodeTree.render(context, writer );
            }
            else
            {
                Runtime.error( "VM error : " + macroName + ". Null AST");
            }
        } 
        catch ( Exception e ) 
        {
            Runtime.error("VelocimacroProxy.render() : exception VM = #" + macroName + 
            "() : "  + StringUtils.stackTrace(e));
        }

        return true;
    }

    /**
     *   The major meat of VelocimacroProxy, init() checks the # of arguments, patches the
     *   macro body, renders the macro into an AST, and then inits the AST, so it is ready 
     *   for quick rendering.  Note that this is only AST dependant stuff. Not context.
     */
    public void init(Context context, Node node) 
       throws Exception
    {
        /*
         *  how many args did we get?
         */
        int i  = node.jjtGetNumChildren();
        
        /*
         *  right number of args?
         */        
        if ( getNumArgs() != i ) 
        {
            Runtime.error("VM #" + macroName + ": error : too few arguments to macro. Wanted " 
                         + getNumArgs() + " got " + i + "  -->");
            return;
        }

        /*
         *  get the argument list to the instance use of the VM
         */
        String callingArgs[] = getArgArray( node );
         
        /*
         *  now, expand our macro out to a string patched with the instance arguments
         */
        expandAndParse( callingArgs );
        return;
    }

    /**
     *  takes an array of calling args, patches the VM and parses it.
     *  called by init() or callable alone if you know what you are doing.
     *
     * @param strCallingArgs array of reference literals
     */
    public void expandAndParse( String callingArgs[] )
    {
        StringBuffer expanded = expandMacroArray( callingArgs );
        
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
            ByteArrayInputStream  inStream = new ByteArrayInputStream( expanded.toString().getBytes() );
            nodeTree = Runtime.parse( inStream, "VM:" + macroName );
        } 
        catch ( Exception e ) 
        {
            Runtime.error("VelocimacroProxy.init() : exception " + macroName + 
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
        int numArgs = node.jjtGetNumChildren();
        
        String args[] = new String[ numArgs ];
		
        /*
         *  eat the args
         */
        int i = 0;
        Token t = null;
        Token tLast = null;
    
        while( i <  numArgs ) 
        {
            args[i] = "";
            /*
             *  we want string literalss to lose the quotes.  #foo( "blargh" ) should have 'blargh' patched 
             *  into macro body.  So for each arg in the use-instance, treat the stringlierals specially...
             */
            if ( node.jjtGetChild(i).getType() == ParserTreeConstants.JJTSTRINGLITERAL )
            {
                args[i] += node.jjtGetChild(i).getFirstToken().image.substring(1, node.jjtGetChild(i).getFirstToken().image.length() - 1);
            }
            else
            {
                /*
                 *  just wander down the token list, concatenating everything together
                 */
                t = node.jjtGetChild(i).getFirstToken();
                tLast = node.jjtGetChild(i).getLastToken();
 
                while( t != tLast ) 
                {
                    args[i] += t.image;
                    t = t.next;
                }

                /*
                 *  don't forget the last one... :)
                 */
                args[i] += t.image;
            }
            i++;
         }
        return args;
    }
   
  
    /**
     *   expands our macro out given our arg list. Uses
     *   the pre-created arg index map to run through and patch
     *   with our args
     */
    private StringBuffer expandMacroArray( String [] callingArgs )
    {
        /*
         *  build the output string by running through the map in sorted order and construct
         *  the new string.  Remember, don't modify the strMacro. Make a new one. The index
         *  elements are specific to the orignal macro body..
         */
        Set set = argIndexMap.keySet();
        Iterator it = set.iterator();
        StringBuffer sb = new StringBuffer();
        int loc = 0;

        while( it.hasNext() )
        {
            Integer index = (Integer) it.next();
            int which = ((Integer) argIndexMap.get( index )).intValue();

            int indexInt = index.intValue();

            //System.out.println( sbNew + ":" + strCallingArgs[iWhich-1]);

            sb.append( macroBody.substring( loc, indexInt ));
            sb.append( callingArgs[which - 1] );
            loc = indexInt + argArray[which].length();
        }
 
        /*
         *  and finish off the string
         */
        sb.append( macroBody.substring( loc ) );
        return sb;
    }
}










