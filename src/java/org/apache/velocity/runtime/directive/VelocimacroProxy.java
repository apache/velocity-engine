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
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.StringReader;

import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.VMContext;
import org.apache.velocity.context.Context;

import org.apache.velocity.runtime.visitor.VMReferenceMungeVisitor;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.exception.MethodInvocationException;

/**
 *  VelocimacroProxy.java
 *
 *   a proxy Directive-derived object to fit with the current directive system
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelocimacroProxy.java,v 1.25 2001/08/07 21:57:56 geirm Exp $ 
 */
public class VelocimacroProxy extends Directive
{
    private String macroName = "";
    private String macroBody = "";
    private String[] argArray = null;
    private SimpleNode nodeTree = null;
    private int numMacroArgs = 0;
    private String namespace = "";

    private boolean init = false;
    private String[] callingArgs;
    private int[]  callingArgTypes;
    private HashMap proxyArgHash = new HashMap();


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

    public void setNodeTree( SimpleNode tree )
    {
        nodeTree = tree;
    }

    /**
     *  returns the number of ars needed for this VM
     */
    public int getNumArgs()
    {
        return numMacroArgs;
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

    public void setNamespace( String ns )
    {
        this.namespace = ns;
    }

    /**
     *   Renders the macro using the context
     */
    public boolean render( InternalContextAdapter context, Writer writer, Node node)
        throws IOException, MethodInvocationException
    {
        try 
        {
            /*
             *  it's possible the tree hasn't been parsed yet, so get 
             *  the VMManager to parse and init it
             */
       
            if (nodeTree != null)
            {
                if ( !init )
                {
                    nodeTree.init( context, rsvc);
                    init = true;
                }
                
                /*
                 *  wrap the current context and add the VMProxyArg objects
                 */

                VMContext vmc = new VMContext( context, rsvc );

                for( int i = 1; i < argArray.length; i++)
                {
                    /*
                     *  we can do this as VMProxyArgs don't change state. They change
                     *  the context.
                     */

                    VMProxyArg arg = (VMProxyArg) proxyArgHash.get( argArray[i] ); 
                    vmc.addVMProxyArg( arg );
                }
         
                /*
                 *  now render the VM
                 */

                nodeTree.render( vmc, writer );               
            }
            else
            {
                rsvc.error( "VM error : " + macroName + ". Null AST");
            }
        } 
        catch ( Exception e ) 
        {
            /*
             *  if it's a MIE, it came from the render.... throw it...
             */

            if ( e instanceof MethodInvocationException)
            {
                throw (MethodInvocationException) e;
            }

            rsvc.error("VelocimacroProxy.render() : exception VM = #" + macroName + 
            "() : "  + StringUtils.stackTrace(e));
        }

        return true;
    }

    /**
     *   The major meat of VelocimacroProxy, init() checks the # of arguments, patches the
     *   macro body, renders the macro into an AST, and then inits the AST, so it is ready 
     *   for quick rendering.  Note that this is only AST dependant stuff. Not context.
     */
    public void init( RuntimeServices rs, InternalContextAdapter context, Node node) 
       throws Exception
    {
        super.init( rs, context, node );

        /*
         *  how many args did we get?
         */
       
        int i  = node.jjtGetNumChildren();
        
        /*
         *  right number of args?
         */        
     
        if ( getNumArgs() != i ) 
        {
            rsvc.error("VM #" + macroName + ": error : too few arguments to macro. Wanted " 
                         + getNumArgs() + " got " + i + "  -->");
            return;
        }

        /*
         *  get the argument list to the instance use of the VM
         */

         callingArgs = getArgArray( node );
       
        /*
         *  now proxy each arg in the context
         */

         setupMacro( callingArgs, callingArgTypes );
         return;
    }

    /**
     *  basic VM setup.  Sets up the proxy args for this
     *  use, and parses the tree
     */
    public boolean setupMacro( String[] callArgs, int[] callArgTypes )
    {
        setupProxyArgs( callArgs, callArgTypes );
        parseTree( callArgs );

        return true;
    }

    /**
     *   parses the macro.  We need to do this here, at init time, or else
     *   the local-scope template feature is hard to get to work :)
     */
    private void parseTree( String[] callArgs )
    {
        try 
        {                
            BufferedReader br = new BufferedReader( new StringReader( macroBody ) );

            /*
             *  now parse the macro - and don't dump the namespace
             */

            nodeTree = rsvc.parse( br, namespace, false );

            /*
             *  now, to make null references render as proper schmoo
             *  we need to tweak the tree and change the literal of
             *  the appropriate references
             *
             *  we only do this at init time, so it's the overhead
             *  is irrelevant
             */

            HashMap hm = new HashMap();

            for( int i = 1; i < argArray.length; i++)
            {
                String arg = callArgs[i-1];

                /*
                 *  if the calling arg is indeed a reference
                 *  then we add to the map.  We ignore other
                 *  stuff
                 */

                if (arg.charAt(0) == '$')
                {
                    hm.put( argArray[i], arg );
                }
            }

            /*
             *  now make one of our reference-munging visitor, and 
             *  let 'er rip
             */

            VMReferenceMungeVisitor v = new VMReferenceMungeVisitor( hm );
            nodeTree.jjtAccept( v, null );
        } 
        catch ( Exception e ) 
        {
            rsvc.error("VelocimacroManager.parseTree() : exception " + macroName + 
                          " : "  + StringUtils.stackTrace(e));
        }
    }
  
    private void setupProxyArgs( String[] callArgs, int [] callArgTypes )
    {
        /*
         * for each of the args, make a ProxyArg
         */

        for( int i = 1; i < argArray.length; i++)
        {
            VMProxyArg arg = new VMProxyArg( rsvc, argArray[i], callArgs[i-1], callArgTypes[i-1] );
            proxyArgHash.put( argArray[i], arg );
        }
    }
  
    /**
     *   gets the args to the VM from the instance-use AST
     */
    private String[] getArgArray( Node node )
    {
        int numArgs = node.jjtGetNumChildren();
        
        String args[] = new String[ numArgs ];
        callingArgTypes = new int[numArgs];

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

            callingArgTypes[i] = node.jjtGetChild(i).getType();
 
           
            if (false &&  node.jjtGetChild(i).getType() == ParserTreeConstants.JJTSTRINGLITERAL )
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
}










