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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.VMContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.VMReferenceMungeVisitor;

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

    private boolean strictArguments;
    
    /**
     * Return name of this Velocimacro.
     * @return The name of this Velocimacro.
     */
    public String getName()
    {
        return  macroName;
    }

    /**
     * Velocimacros are always LINE
     * type directives.
     * @return The type of this directive.
     */
    public int getType()
    {
        return LINE;
    }

    /**
     *   sets the directive name of this VM
     * @param name
     */
    public void setName( String name )
    {
        macroName = name;
    }

    /**
     *  sets the array of arguments specified in the macro definition
     * @param arr
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
     * @param tree
     */
    public void setNodeTree( SimpleNode tree )
    {
        nodeTree = tree;
    }

    /**
     *  returns the number of ars needed for this VM
     * @return The number of ars needed for this VM
     */
    public int getNumArgs()
    {
        return numMacroArgs;
    }

    /**
     *   Sets the orignal macro body.  This is simply the cat of the macroArray, but the
     *   Macro object creates this once during parsing, and everyone shares it.
     *   Note : it must not be modified.
     * @param mb
     */
    public void setMacrobody( String mb )
    {
        macroBody = mb;
    }

    /**
     * @param ns
     */
    public void setNamespace( String ns )
    {
        this.namespace = ns;
    }

    /**
     *   Renders the macro using the context
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     * @throws MethodInvocationException
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
                rsvc.getLog().error("VM error " + macroName + ". Null AST");
            }
        }

        /*
         *  if it's a MIE, it came from the render.... throw it...
         */
        catch( MethodInvocationException e )
        {
            throw e;
        }

        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            throw e;
        }

        catch ( Exception e )
        {

            rsvc.getLog().error("VelocimacroProxy.render() : exception VM = #" +
                                macroName + "()", e);
        }

        return true;
    }

    /**
     *   The major meat of VelocimacroProxy, init() checks the # of arguments, patches the
     *   macro body, renders the macro into an AST, and then inits the AST, so it is ready
     *   for quick rendering.  Note that this is only AST dependant stuff. Not context.
     * @param rs
     * @param context
     * @param node
     * @throws TemplateInitException
     */
    public void init( RuntimeServices rs, InternalContextAdapter context, Node node)
       throws TemplateInitException
    {
        super.init( rs, context, node );

        /**
         * Throw exception for invalid number of arguments?
         */
        strictArguments = rs.getConfiguration().getBoolean(RuntimeConstants.VM_ARGUMENTS_STRICT,false);
        
        /*
         *  how many args did we get?
         */

        int i  = node.jjtGetNumChildren();

        /*
         *  right number of args?
         */

        if ( getNumArgs() != i )
        {
            // If we have a not-yet defined macro, we do get no arguments because
            // the syntax tree looks different than with a already defined macro.
            // But we do know that we must be in a macro definition context somewhere up the
            // syntax tree.
            // Check for that, if it is true, suppress the error message.
            // Fixes VELOCITY-71.

            for (Node parent = node.jjtGetParent(); parent != null; )
            {
                if ((parent instanceof ASTDirective) && 
                        StringUtils.equals(((ASTDirective) parent).getDirectiveName(), "macro"))
                {
                    return;
                }
                parent = parent.jjtGetParent();
            }
            
            String errormsg = "VM #" + macroName + ": error : too " +
            ((getNumArgs() > i) ? "few" : "many") + 
            " arguments to macro. Wanted " + getNumArgs() +
            " got " + i;

            if (strictArguments)
            {
                /**
                 *  indicate col/line assuming it starts at 0 - this will be
                 *  corrected one call up
                 */
                throw new TemplateInitException(errormsg,
                        context.getCurrentTemplateName(),
                        0,
                        0);
            }
            else
            {
                rsvc.getLog().error(errormsg);
                return;
            }
        }

        /*
         *  get the argument list to the instance use of the VM
         */

         callingArgs = getArgArray( node );

        /*
         *  now proxy each arg in the context
         */

         setupMacro( callingArgs, callingArgTypes );
    }

    /**
     *  basic VM setup.  Sets up the proxy args for this
     *  use, and parses the tree
     * @param callArgs
     * @param callArgTypes
     * @return True if the proxy was setup successfully.
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
     *   @param callArgs
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
        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            rsvc.getLog().error("VelocimacroManager.parseTree() : exception " +
                                macroName, e);
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
     *   @param node
     *   @return array of arguments
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










