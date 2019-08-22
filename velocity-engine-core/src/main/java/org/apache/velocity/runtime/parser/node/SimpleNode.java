package org.apache.velocity.runtime.parser.node;

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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.velocity.Template;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.util.StringUtils;

import org.slf4j.Logger;

/**
 *
 */
public class SimpleNode implements Node
{
    /** */
    protected RuntimeServices rsvc = null;

    /** */
    protected Logger log = null;

    /** */
    protected Node parent;

    /** */
    protected Node[] children;

    /** */
    protected int id;

    /** */
    protected Parser parser;

    /** */
    protected int info;

    /** */
    public boolean state;

    /** */
    protected boolean invalid = false;

    /** */
    protected Token first;

    /** */
    protected Token last;

    protected Template template;

    /**
     * For caching the literal value.
     */
    protected String literal = null;

    /**
     * Line number for this Node in the vm source file.
     */

    protected int line;

    /**
     * Column number for this Node in the vm source file.
     */
    protected int column;

    /**
     * String image variable of the first Token element that was parsed and connected to this Node.
     */
    protected String firstImage;

    /**
     * String image variable of the last Token element that was parsed and connected to this Node.
     */
    protected String lastImage;

    public RuntimeServices getRuntimeServices()
    {
      return rsvc;
    }

    /**
     * @param i
     */
    public SimpleNode(int i)
    {
        id = i;
    }

    /**
     * @param p
     * @param i
     */
    public SimpleNode(Parser p, int i)
    {
        this(i);
        parser = p;
        template = parser.getCurrentTemplate();
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtOpen()
     */
    public void jjtOpen()
    {
        first = parser.getToken(1); // added
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtClose()
     */
    public void jjtClose()
    {
        last = parser.getToken(0); // added
    }

    /**
     * @param t
     */
    public void setFirstToken(Token t)
    {
        this.first = t;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#getFirstToken()
     */
    public Token getFirstToken()
    {
        return first;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#getLastToken()
     */
    public Token getLastToken()
    {
        return last;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtSetParent(org.apache.velocity.runtime.parser.node.Node)
     */
    public void jjtSetParent(Node n)
    {
        parent = n;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtGetParent()
     */
    public Node jjtGetParent()
    {
        return parent;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtAddChild(org.apache.velocity.runtime.parser.node.Node, int)
     */
    public void jjtAddChild(Node n, int i)
    {
        if (children == null)
        {
            children = new Node[i + 1];
        }
        else if (i >= children.length)
        {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtGetChild(int)
     */
    public Node jjtGetChild(int i)
    {
        return children[i];
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtGetNumChildren()
     */
    public int jjtGetNumChildren()
    {
        return (children == null) ? 0 : children.length;
    }


    /**
     * @see org.apache.velocity.runtime.parser.node.Node#jjtAccept(org.apache.velocity.runtime.parser.node.ParserVisitor, java.lang.Object)
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }


    /**
     * @see org.apache.velocity.runtime.parser.node.Node#childrenAccept(org.apache.velocity.runtime.parser.node.ParserVisitor, java.lang.Object)
     */
    public Object childrenAccept(ParserVisitor visitor, Object data)
    {
        if (children != null)
        {
            for (Node aChildren : children)
            {
                aChildren.jjtAccept(visitor, data);
            }
        }
        return data;
    }

    /* You can override these two methods in subclasses of SimpleNode to
        customize the way the node appears when the tree is dumped.  If
        your output uses more than one line you should override
        toString(String), otherwise overriding toString() is probably all
        you need to do. */
    /**
     * @param prefix display prefix
     * @return String representation of this node.
     */
    public String toString(String prefix)
    {
        return prefix + "_" + toString();
    }

    /**
     * <p>Dumps nodes tree on System.out.</p>
     * <p>Override {@link #dump(String, PrintWriter)} if you want to customize
     * how the node dumps out its children.
     *
     * @param prefix
     */
    public final void dump(String prefix)
    {
        dump(prefix, System.out);
    }

    /**
     * <p>Dumps nodes tree on System.out.</p>
     * <p>Override {@link #dump(String, PrintWriter)} if you want to customize
     * how the node dumps out its children.
     *
     * @param prefix display prefix
     * @param out output print stream
     */
    public final void dump(String prefix, PrintStream out)
    {
        Charset charset = null;
        if (rsvc != null) /* may be null if node isn't yet initialized */
        {
            String encoding = rsvc.getString(RuntimeConstants.INPUT_ENCODING);
            try
            {
                charset = Charset.forName(encoding);
            }
            catch (Exception e) {}
        }
        if (charset == null)
        {
            charset = Charset.defaultCharset();
        }
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, charset));
        dump(prefix, pw);
        pw.flush();
    }
    
    /**
     * <p>Dumps nodes tree on System.out.</p>
     * <p>Override this method if you want to customize how the node dumps
     * out its children.</p>
     *
     * @param prefix display prefix
     * @param out output print writer
     */
    public void dump(String prefix, PrintWriter out)
    {
        out.println(toString());
        if (children != null)
        {
            for (int i = 0; i < children.length; ++i)
            {
                SimpleNode n = (SimpleNode) children[i];
                out.print(prefix + " |_");
                if (n != null)
                {
                    n.dump(prefix + ( i == children.length - 1 ? "   " : " | " ), out);
                }
            }
        }
    }

    /**
     * Return a string that tells the current location of this node.
     * @param context
     * @return location
     */
    protected String getLocation(InternalContextAdapter context)
    {
        return StringUtils.formatFileString(this);
    }

    // All additional methods

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#literal()
     */
    public String literal()
    {
        if( literal != null )
        {
            return literal;
        }

        // if we have only one string, just return it and avoid
        // buffer allocation. VELOCITY-606
        if (first == last)
        {
            literal = NodeUtils.tokenLiteral(parser, first);
            return literal;
        }

        Token t = first;
        StringBuilder sb = new StringBuilder(NodeUtils.tokenLiteral(parser, t));
        while (t != last)
        {
            t = t.next;
            sb.append(NodeUtils.tokenLiteral(parser, t));
        }
        literal = sb.toString();
        return literal;
    }

    /**
     * @throws TemplateInitException
     * @see org.apache.velocity.runtime.parser.node.Node#init(org.apache.velocity.context.InternalContextAdapter, java.lang.Object)
     */
    public Object init( InternalContextAdapter context, Object data) throws TemplateInitException
    {
        /*
         * hold onto the RuntimeServices
         */

        rsvc = (RuntimeServices) data;
        log = rsvc.getLog("rendering");

        int i, k = jjtGetNumChildren();

        for (i = 0; i < k; i++)
        {
            jjtGetChild(i).init( context, data);
        }

        line = first.beginLine;
        column = first.beginColumn;

        return data;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#evaluate(org.apache.velocity.context.InternalContextAdapter)
     */
    public boolean evaluate( InternalContextAdapter  context)
        throws MethodInvocationException
    {
        return false;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#value(org.apache.velocity.context.InternalContextAdapter)
     */
    public Object value( InternalContextAdapter context)
        throws MethodInvocationException
    {
        return null;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#render(org.apache.velocity.context.InternalContextAdapter, java.io.Writer)
     */
    public boolean render( InternalContextAdapter context, Writer writer)
        throws IOException, MethodInvocationException, ParseErrorException, ResourceNotFoundException
    {
        int i, k = jjtGetNumChildren();

        for (i = 0; i < k; i++)
            jjtGetChild(i).render(context, writer);

        return true;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#execute(java.lang.Object, org.apache.velocity.context.InternalContextAdapter)
     */
    public Object execute(Object o, InternalContextAdapter context)
      throws MethodInvocationException
    {
        return null;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#getType()
     */
    public int getType()
    {
        return id;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#setInfo(int)
     */
    public void setInfo(int info)
    {
        this.info = info;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#getInfo()
     */
    public int getInfo()
    {
        return info;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#setInvalid()
     */
    public void setInvalid()
    {
        invalid = true;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#isInvalid()
     */
    public boolean isInvalid()
    {
        return invalid;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#getLine()
     */
    public int getLine()
    {
        return line;
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.Node#getColumn()
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * @since 1.5
     */
    public String toString()
    {
        StringBuilder tokens = new StringBuilder();

        for (Token t = getFirstToken(); t != null; )
        {
            tokens.append("[").append(t.image.replace("\n", "\\n")).append("]");
            if (t.next != null)
            {
                if (t.equals(getLastToken()))
                {
                    break;
                }
                else
                {
                    tokens.append(", ");
                }
            }
            t = t.next;
        }
        String tok = tokens.toString();
        if (tok.length() > 50) tok = tok.substring(0, 50) + "...";
        return getClass().getSimpleName() + " [id=" + id + ", info=" + info + ", invalid="
                + invalid
                + ", tokens=" + tok + "]";
    }

    public String getTemplateName()
    {
      return template.getName();
    }

    /**
     * Call before calling cleanupParserAndTokens() if you want to store image of
     * the first and last token of this node.
     */
    public void saveTokenImages()
    {
        if( first != null )
        {
            this.firstImage = first.image;
        }
        if( last != null )
        {
            this.lastImage = last.image;
        }
    }

    /**
     * Removes references to Parser and Tokens since they are not needed anymore at this point.
     *
     * This allows us to save memory quite a bit.
     */
    public void cleanupParserAndTokens()
    {
        this.parser = null;
        this.first = null;
        this.last = null;
    }

    /**
     * @return String image variable of the first Token element that was parsed and connected to this Node.
     */
    public String getFirstTokenImage()
    {
        return firstImage;
    }

    /**
     * @return String image variable of the last Token element that was parsed and connected to this Node.
     */
    public String getLastTokenImage()
    {
        return lastImage;
    }

    public Template getTemplate() { return template; }

    /**
     * @return the parser which created this node
     * @since 2.2
     */
    public Parser getParser()
    {
        return parser;
    }
}
