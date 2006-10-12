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
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.NodeUtils;

/**
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
