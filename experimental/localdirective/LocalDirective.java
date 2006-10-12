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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import org.apache.velocity.runtime.parser.node.ASTReference;

import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


/**
 *   Simple directive to provide ability to trap a local value for
 *   iteration.
 *
 *   Ex :
 *
 *   #set($foo = 1)
 *   $foo
 *   #local($foo)
 *      #set($foo = 2)
 *      $foo
 *   #end
 *    $foo
 *
 *    should output
 *
 *    1
 *       2
 *    1
 *
 * @version $Id$
 */
public class LocalDirective extends Directive
{
    public String getName()
    {
        return "local";
    }

        public int getType()
        {
            return BLOCK;
        }

        public boolean render(InternalContextAdapter context,
                               Writer writer, Node node)
        throws IOException,  MethodInvocationException, ResourceNotFoundException,
                ParseErrorException
        {
            Map data = new HashMap();

            int num = node.jjtGetNumChildren();

            /*
             * get the references
             */

            for (int i=0; i < num; i++)
            {
                SimpleNode child = (SimpleNode) node.jjtGetChild(i);

                /*
                 * if a block, just execute
                 */

                if (child.getType() == ParserTreeConstants.JJTBLOCK)
                {
                    child.render(context, writer);
                    break;
                }
                else
                {
                    /* save the values - for now, just w/ ref to test */

                    if (child.getType() == ParserTreeConstants.JJTREFERENCE)
                    {
                        data.put(child, child.execute(null, context));
                    }
                    else
                    {
                        System.out.println("unhandled type");
                    }
                }
            }

            Iterator it = data.keySet().iterator();

            while(it.hasNext())
            {
                ASTReference ref = (ASTReference) it.next();

                ref.setValue(context, data.get(ref));
            }

            return true;
        }
}
