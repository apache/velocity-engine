package org.apache.velocity.runtime.parser.node;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.velocity.Template;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.util.StringBuilderWriter;
import org.apache.velocity.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/**
 * ASTStringLiteral support. Will interpolate!
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ASTStringLiteral extends SimpleNode
{
    /* cache the value of the interpolation switch */
    private boolean interpolate = true;

    private SimpleNode nodeTree = null;

    private String image = "";

    /**
     * @param id
     */
    public ASTStringLiteral(int id)
    {
        super(id);
    }

    /**
     * @param p
     * @param id
     */
    public ASTStringLiteral(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * init : we don't have to do much. Init the tree (there shouldn't be one)
     * and then see if interpolation is turned on.
     *
     * @param context
     * @param data
     * @return Init result.
     * @throws TemplateInitException
     */
    public Object init(InternalContextAdapter context, Object data)
            throws TemplateInitException
    {
        /*
         * simple habit... we prollie don't have an AST beneath us
         */

        super.init(context, data);

        /*
         * the stringlit is set at template parse time, so we can do this here
         * for now. if things change and we can somehow create stringlits at
         * runtime, this must move to the runtime execution path
         *
         * so, only if interpolation is turned on AND it starts with a " AND it
         * has a directive or reference, then we can interpolate. Otherwise,
         * don't bother.
         */

        interpolate = rsvc.getBoolean(
                RuntimeConstants.INTERPOLATE_STRINGLITERALS, true)
                && getFirstToken().image.startsWith("\"")
                && ((getFirstToken().image.indexOf('$') != -1) || (getFirstToken().image
                        .indexOf('#') != -1));

        /*
         * get the contents of the string, minus the '/" at each end
         */
        String img = getFirstToken().image;

        image = img.substring(1, img.length() - 1);

        if (img.startsWith("\""))
        {
            image = unescape(image);
        }
        if (img.charAt(0) == '"' || img.charAt(0) == '\'' )
        {
            // replace double-double quotes like "" with a single double quote "
            // replace double single quotes '' with a single quote '
            image = replaceQuotes(image, img.charAt(0));
        }

        if (interpolate)
        {
            /*
             * parse and init the nodeTree
             */
            StringReader br = new StringReader(image);

            /*
             * it's possible to not have an initialization context - or we don't
             * want to trust the caller - so have a fallback value if so
             *
             * Also, do *not* dump the VM namespace for this template
             */

            Template template = null;
            if (context != null)
            {
                template = (Template)context.getCurrentResource();
            }
            if (template == null)
            {
                template = new Template();
                template.setName("StringLiteral");
            }
            try
            {
                nodeTree = rsvc.parse(br, template);
            }
            catch (ParseException e)
            {
                String msg = "Failed to parse String literal at "+
                    StringUtils.formatFileString(template.getName(), getLine(), getColumn());
                throw new TemplateInitException(msg, e, template.getName(), getColumn(), getLine());
            }

            adjTokenLineNums(nodeTree);

            /*
             * init with context. It won't modify anything
             */

            nodeTree.init(context, rsvc);
        }

        cleanupParserAndTokens();

        return data;
    }

    public String literal()
    {
    	return image;
    }

    /**
     * Adjust all the line and column numbers that comprise a node so that they
     * are corrected for the string literals position within the template file.
     * This is neccessary if an exception is thrown while processing the node so
     * that the line and column position reported reflects the error position
     * within the template and not just relative to the error position within
     * the string literal.
     */
    public void adjTokenLineNums(Node node)
    {
        Token tok = node.getFirstToken();
        // Test against null is probably not neccessary, but just being safe
        while(tok != null && tok != node.getLastToken())
        {
            // If tok is on the first line, then the actual column is
            // offset by the template column.

            if (tok.beginLine == 1)
                tok.beginColumn += getColumn();

            if (tok.endLine == 1)
                tok.endColumn += getColumn();

            tok.beginLine += getLine()- 1;
            tok.endLine += getLine() - 1;
            tok = tok.next;
        }
    }

    /**
     * Replaces double double-quotes with a single double quote ("" to ").
     * Replaces double single quotes with a single quote ('' to ').
	 *
	 * @param s StringLiteral without the surrounding quotes
	 * @param literalQuoteChar char that starts the StringLiteral (" or ')
     */
    private String replaceQuotes(String s, char literalQuoteChar)
    {
        if( (literalQuoteChar == '"' && s.indexOf("\"") == -1) ||
            (literalQuoteChar == '\'' && s.indexOf("'") == -1) )
        {
            return s;
        }

        StringBuilder result = new StringBuilder(s.length());
        for(int i = 0, is = s.length(); i < is; i++)
        {
            char c = s.charAt(i);
            result.append(c);

            if( i + 1 < is )
            {
                char next =  s.charAt(i + 1);
				// '""' -> "", "''" -> ''
				// thus it is not necessary to double quotes if the "surrounding" quotes
				// of the StringLiteral are different. See VELOCITY-785
                if( (literalQuoteChar == '"' && (next == '"' && c == '"')) ||
				    (literalQuoteChar == '\'' && (next == '\'' && c == '\'')) )
                {
                    i++;
                }
           }
        }
        return result.toString();
    }

    /**
     * @since 1.6
     */
    public static String unescape(final String string)
    {
        int u = string.indexOf("\\u");
        if (u < 0) return string;

        StringBuilder result = new StringBuilder();

        int lastCopied = 0;

        for (;;)
        {
            result.append(string.substring(lastCopied, u));

            /* we don't worry about an exception here,
             * because the lexer checked that string is correct */
            char c = (char) Integer.parseInt(string.substring(u + 2, u + 6), 16);
            result.append(c);

            lastCopied = u + 6;

            u = string.indexOf("\\u", lastCopied);
            if (u < 0)
            {
                result.append(string.substring(lastCopied));
                return result.toString();
            }
        }
    }


    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#jjtAccept(org.apache.velocity.runtime.parser.node.ParserVisitor,
     *      java.lang.Object)
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     * Check to see if this is an interpolated string.
     * @return true if this is constant (not an interpolated string)
     * @since 1.6
     */
    public boolean isConstant()
    {
        return !interpolate;
    }

    /**
     * renders the value of the string literal If the properties allow, and the
     * string literal contains a $ or a # the literal is rendered against the
     * context Otherwise, the stringlit is returned.
     *
     * @param context
     * @return result of the rendering.
     */
    public Object value(InternalContextAdapter context)
    {
        if (interpolate)
        {
            try
            {
                /*
                 * now render against the real context
                 */

                Writer writer = new StringBuilderWriter();
                nodeTree.render(context, writer);

                /*
                 * and return the result as a String
                 */

                return writer.toString();
            }

            /**
             * pass through application level runtime exceptions
             */
            catch (RuntimeException e)
            {
                throw e;
            }

            catch (IOException e)
            {
                String msg = "Error in interpolating string literal";
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }

        }

        /*
         * ok, either not allowed to interpolate, there wasn't a ref or
         * directive, or we failed, so just output the literal
         */

        return image;
    }
}
