package org.apache.velocity.runtime.directive;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
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

import java.io.*;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;

import org.apache.velocity.runtime.RuntimeServices;

/**
 * A very simple directive that leverages the Node.literal()
 * to grab the literal rendition of a node. We basically
 * grab the literal value on init(), then repeatedly use
 * that during render().
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: Literal.java,v 1.7.4.1 2004/03/03 23:22:56 geirm Exp $
 */
public class Literal extends Directive
{
    String literalText;
    
    /**
     * Return name of this directive.
     */
    public String getName()
    {
        return "literal";
    }        
    
    /**
     * Return type of this directive.
     */
    public int getType()
    {
        return BLOCK;
    }        

    /**
     * Store the literal rendition of a node using
     * the Node.literal().
     */
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
        throws Exception
    {
        super.init( rs, context, node );

        literalText = node.jjtGetChild(0).literal();
    }    

    /**
     * Throw the literal rendition of the block between
     * #literal()/#end into the writer.
     */
    public boolean render( InternalContextAdapter context, 
                           Writer writer, Node node)
        throws IOException
    {
        writer.write(literalText);
        return true;
    }
}
