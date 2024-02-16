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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.*;
import org.apache.velocity.util.StringUtils;
import org.apache.velocity.util.introspection.Info;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Foreach directive used for moving through arrays,
 * or objects that provide an Iterator.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author Daniel Rall
 * @version $Id$
 */
public class Foreach extends Directive
{
    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    @Override
    public String getName()
    {
        return "foreach";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    @Override
    public int getType()
    {
        return BLOCK;
    }

    /**
     * The maximum number of times we're allowed to loop.
     */
    private int maxNbrLoops;

    /**
     * Whether or not to throw an Exception if the iterator is null.
     */
    private boolean skipInvalidIterator;

    /**
     * The reference name used to access each
     * of the elements in the list object. It
     * is the $item in the following:
     *
     * #foreach ($item in $list)
     *
     * This can be used class wide because
     * it is immutable.
     */
    private String elementKey;

    /**
     *  immutable, so create in init
     */
    protected Info uberInfo;

    /**
     *  simple init - init the tree and get the elementKey from
     *  the AST
     * @param rs
     * @param context
     * @param node
     * @throws TemplateInitException
     */
    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
        throws TemplateInitException
    {
        super.init(rs, context, node);

        maxNbrLoops = rsvc.getInt(RuntimeConstants.MAX_NUMBER_LOOPS,
                                  Integer.MAX_VALUE);
        if (maxNbrLoops < 1)
        {
            maxNbrLoops = Integer.MAX_VALUE;
        }
        skipInvalidIterator =
            rsvc.getBoolean(RuntimeConstants.SKIP_INVALID_ITERATOR, true);

        if (rsvc.getBoolean(RuntimeConstants.RUNTIME_REFERENCES_STRICT, false))
        {
          // If we are in strict mode then the default for skipInvalidItarator
          // is true.  However, if the property is explicitly set, then honor the setting.
          skipInvalidIterator = rsvc.getBoolean(RuntimeConstants.SKIP_INVALID_ITERATOR, false);
        }

        /*
         *  this is really the only thing we can do here as everything
         *  else is context sensitive
         */
        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);

        if (sn instanceof ASTReference)
        {
            elementKey = ((ASTReference) sn).getRootString();
        }
        else
        {
            /*
             * the default, error-prone way which we'll remove
             */
        	elementKey = sn.getFirstTokenImage().substring(1);
        }

        /*
         * make an uberinfo - saves new's later on
         */

        uberInfo = new Info(this.getTemplateName(),
                getLine(),getColumn());
    }

    /**
     * Extension hook to allow subclasses to control whether loop vars
     * are set locally or not. So, those in favor of VELOCITY-285, can
     * make that happen easily by overriding this and having it use
     * context.localPut(k,v). See VELOCITY-630 for more on this.
     * @param context
     * @param key
     * @param value
     */
    protected void put(InternalContextAdapter context, String key, Object value)
    {
        context.put(key, value);
    }

    /**
     * Retrieve the contextual iterator.
     * @param iterable
     * @param node
     * @return iterator
     */
    protected Iterator getIterator(Object iterable, Node node)
    {
        Iterator i = null;
        /*
         * do our introspection to see what our collection is
         */
        if (iterable != null)
        {
            try
            {
                i = rsvc.getUberspect().getIterator(iterable, uberInfo);
            }
            /*
             * pass through application level runtime exceptions
             */
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception ee)
            {
                String msg = "Error getting iterator for #foreach parameter "
                    + node.literal() + " at " + StringUtils.formatFileString(node);
                log.error(msg, ee);
                throw new VelocityException(msg, ee, rsvc.getLogContext().getStackTrace());
            }

            if (i == null && !skipInvalidIterator)
            {
                String msg = "#foreach parameter " + node.literal() + " at "
                    + StringUtils.formatFileString(node) + " is of type " + iterable.getClass().getName()
                    + " and cannot be iterated by " + rsvc.getUberspect().getClass().getName();
                log.error(msg);
                throw new VelocityException(msg, null, rsvc.getLogContext().getStackTrace());
            }
        }
        return i;
    }

    /**
     *  renders the #foreach() block
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     */
    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
        throws IOException
    {
        // Get the block ast tree which is always the last child ...
        Node block = node.jjtGetChild(node.jjtGetNumChildren()-1);

        // ... except if there is an #else clause
        Node elseBlock = null;
        Node previous = node.jjtGetChild(node.jjtGetNumChildren()-2);
        if (previous instanceof ASTBlock)
        {
            elseBlock = block;
            block = previous;
        }

        Node iterableNode = node.jjtGetChild(2);
        Object iterable = iterableNode.value(context);
        Iterator i = getIterator(iterable, iterableNode);
        if (i == null || !i.hasNext())
        {
            if (elseBlock != null)
            {
                renderBlock(context, writer, elseBlock);
            }
            return false;
        }

        /*
         * save the element key if there is one
         */
        Object o = context.get(elementKey);

        /*
         * roll our own scope class instead of using preRender(ctx)'s
         */
        ForeachScope foreach = null;
        if (isScopeProvided())
        {
            String name = getScopeName();
            foreach = new ForeachScope(this, context.get(name));
            context.put(name, foreach);
        }

        int count = 1;
        while (count <= maxNbrLoops && i.hasNext())
        {
            count++;

            put(context, elementKey, i.next());
            if (isScopeProvided())
            {
                // update the scope control
                foreach.index++;
                foreach.hasNext = i.hasNext();
            }

            try
            {
                renderBlock(context, writer, block);
            }
            catch (StopCommand stop)
            {
                if (stop.isFor(this))
                {
                    break;
                }
                else
                {
                    // clean up first
                    clean(context, o);
                    throw stop;
                }
            }
        }
        clean(context, o);
        /*
         * closes the iterator if it implements the Closeable interface
         */
        if (i instanceof Closeable && i != iterable) /* except if the iterable is the iterator itself */
        {
            ((Closeable)i).close();
        }
        return true;
    }

    protected void renderBlock(InternalContextAdapter context, Writer writer, Node block)
        throws IOException
    {
        block.render(context, writer);
    }

    protected void clean(InternalContextAdapter context, Object o)
    {
        /*
         *  restores element key if exists
         *  otherwise just removes
         */
        if (o != null)
        {
            context.put(elementKey, o);
        }
        else
        {
            context.remove(elementKey);
        }

        // clean up after the ForeachScope
        postRender(context);
    }

    /**
     * We do not allow a word token in any other arg position except for the 2nd since
     * we are looking for the pattern #foreach($foo in $bar).
     */
    @Override
    public void checkArgs(ArrayList<Integer> argtypes, Token t, String templateName)
      throws ParseException
    {
        if (argtypes.size() < 3)
        {
            throw new MacroParseException("Too few arguments to the #foreach directive",
              templateName, t);
        }
        else if (argtypes.get(0) != StandardParserTreeConstants.JJTREFERENCE)
        {
            throw new MacroParseException("Expected argument 1 of #foreach to be a reference",
                templateName, t);
        }
        else if (argtypes.get(1) != StandardParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("Expected word 'in' at argument position 2 in #foreach",
                templateName, t);
        }
        else if (argtypes.get(2) == StandardParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("Argument 3 of #foreach is of the wrong type",
                templateName, t);
        }
    }
}
