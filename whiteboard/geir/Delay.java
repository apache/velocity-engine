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
 * 4. The names "The Jakarta Project", "Velocity","DVSL" and "Apache Software
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
package org.apache.velocity.runtime.directive;

import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.NodeUtils;

/**
 *  
 *
 *  @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 */
public class Delay extends Directive
{
    List astStrings = null;

    public String getName()
    {
        return "delay";
    }        
    
    public int getType()
    {
        return BLOCK;
    }        

    public void init( RuntimeServices rs, InternalContextAdapter context, Node node) 
        throws Exception
    {
        super.init( rs, context, node );

        astStrings =  getASTAsStringArray( node.jjtGetChild(1) );
  
    }


    public boolean render( InternalContextAdapter context, 
                           Writer writer, Node node)
        throws IOException
    {
        
        /*
         *  what is our arg?
         */

        int ival = 0;
        try
        {
            Integer val =  (Integer) node.jjtGetChild(0).value(context);
            ival = val.intValue();
        }
        catch( Exception ee )
            {}


        System.out.println( "value : " + ival );

        if ( ival > 1 )
        {
            writer.write("#delay( " + --ival  + " )\n " );
            
            for( int i = 0; i < astStrings.size(); i++ )
            {
                writer.write( (String) astStrings.get( i ) );
            }

            writer.write("#end");
        }
        else
        {
            for( int i = 0; i < astStrings.size(); i++ )
            {
                writer.write( (String)  astStrings.get( i ) );
            }
        }

        return true;
    }

 
    private static List getASTAsStringArray( Node rootNode )
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

        while( t != tLast ) 
        {
            list.add( NodeUtils.tokenLiteral( t ) );
            t = t.next;
        }

        /*
         *  make sure we get the last one...
         */

        list.add( NodeUtils.tokenLiteral( t ) );

        return list;
    }
}
