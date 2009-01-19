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

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * BlockMacro directive is used to invoke Velocity macros with normal parameters and a macro body.
 * <p>
 * The macro can then refer to the passed body AST. This directive can be used as a 
 * "decorator". Body AST can contain any valid Velocity syntax.
 *
 * An example:
 * <pre>
 * #set($foobar = "yeah!")
 * 
 * #macro(strong $txt)
 * &lt;strong&gt;$bodyContent&lt;/strong&gt; $txt
 * #end
 *
 * #@strong($foobar)
 * &lt;u&gt;This text is underlined and bold&lt;/u&gt;
 * #end
 * </pre>
 * Will print:
 * <pre>
 * &lt;strong&gt;&lt;u&gt;This text is underlined and bold&lt;u&gt;&lt;/strong&gt; yeah!
 * </pre>
 * 
 * bodyContent reference name is configurable (see velocity.properties).
 *
 * @author <a href="mailto:wyla@removethis.sci.fi">Jarkko Viinamaki</a>
 * @since 1.6.2
 * @version $Id$
 */
public class BlockMacro extends Directive
{
    private String name;
    private RuntimeMacro macro;
    private Node macroBody;

    public BlockMacro(String name)
    {
        this.name = name;
    }
    
    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "blockmacro";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    public int getType()
    {
        return BLOCK;
    }

    /**
     * Initializes the directive.
     * 
     * @param rs
     * @param context
     * @param node
     * @throws TemplateInitException
     */
    public void init(RuntimeServices rs, InternalContextAdapter context,
                     Node node)
        throws TemplateInitException
    {
        super.init(rs, context, node);
        
        macroBody = node.jjtGetChild(node.jjtGetNumChildren() - 1);

        macro = new RuntimeMacro(name);
        macro.init(rs, context, node);
    }

    /**
     * Renders content using the selected macro and the passed AST body.
     *
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
        throws IOException
    {
        return macro.render(context, 
                            writer, 
                            node,
                            new BlockMacroContainer(macroBody, ParserTreeConstants.JJTBLOCK, writer));
    }

    /**
     * BlockMacro body is wrapped in this container. 
     *
     * With this we can have reference to the output Writer in ProxyVMContext.get call.
     * This approach lets us write directly to the output instead of using costly StringWriter.
     */
    public static class BlockMacroContainer extends SimpleNode
    {
        private Node node;
        private Writer w;
    
        public BlockMacroContainer(Node node, int type, Writer writer)
        {
            super(type);
            this.node = node;
            this.w = writer;
        }
        
        public boolean render( InternalContextAdapter context, Writer writer)
            throws IOException, MethodInvocationException, ParseErrorException, ResourceNotFoundException
        {
            return node.render(context, writer != null ? writer : w);
        }
    }
}

