package org.apache.velocity.runtime.directive;


/*
 * Copyright 2002,2004 The Apache Software Foundation.
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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Simple implementation of a 'context protector', as desired by
 * Christoph Reck.  Used like
 *
 * #local($a $b $c )
 *  ...
 * #end
 *
 * and restores the value of the reference at the end of the block
 * Assumes that you can #set() the value of any reference in the list
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Local.java,v 1.1.4.1 2004/03/04 00:18:30 geirm Exp $
 */
public class Local extends Directive
{
    protected Object[] refList;
    protected Node block;

    /**
     * Return name of this directive.
     */
    public String getName()
    {
        return "local";
    }

    /**
     * Return type of this directive.
     */
    public int getType()
    {
        return BLOCK;
    }

    /**
     *
     */
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
        throws Exception
    {
        super.init( rs, context, node );

        int numchildren = node.jjtGetNumChildren();

        ArrayList al = new ArrayList();

        /*
         *  get all the references in our arg list
         */
        for (int i=0; i < numchildren-1; i++)
        {
            SimpleNode sn = (SimpleNode) node.jjtGetChild(i);

            if (sn instanceof ASTReference )
            {
                al.add(sn);
            }
            else
            {
                throw new Exception("Arg " + i + " not a reference.");
            }
        }

        refList = al.toArray();

        block = node.jjtGetChild(node.jjtGetNumChildren() - 1);

    }

    /**
     *
     */
    public boolean render(InternalContextAdapter context,
                           Writer writer, Node node)
        throws IOException, ResourceNotFoundException,
            ParseErrorException, MethodInvocationException
    {

        /*
         * get the current values for the references we have
         */

        ArrayList vals = new ArrayList();

        for (int i = 0; i < refList.length; i++)
        {
            Object o = ((ASTReference) refList[i]).value(context);
            vals.add(o);
        }

        /*
         *  now render the block
         */
        boolean retVal = block.render(context, writer);

        /*
         * now restore the original values
         */

        for (int i = 0; i < refList.length; i++)
        {
            ASTReference astr = (ASTReference) refList[i];
            astr.setValue(context, vals.get(i));
        }

        return true;
    }
}
