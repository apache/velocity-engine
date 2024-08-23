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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.util.DuckType;
import org.apache.velocity.util.StringUtils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * handles the range 'operator'  [ n .. m ]
 *
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 */
public class ASTIntegerRange extends SimpleNode
{
    /**
     * @param id
     */
    public ASTIntegerRange(int id)
    {
        super(id);
    }

    /**
     * @param p
     * @param id
     */
    public ASTIntegerRange(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#jjtAccept(org.apache.velocity.runtime.parser.node.StandardParserVisitor, java.lang.Object)
     */
    @Override
    public Object jjtAccept(StandardParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public static class IntegerRange extends AbstractList<Integer>
    {
        public class RangeIterator implements ListIterator<Integer>
        {
            private int value;

            public RangeIterator()
            {
                value = left - delta;
            }

            public RangeIterator(int startIndex)
            {
                value = left + (startIndex - 1) * delta;
            }

            @Override
            public Integer next()
            {
                value += delta;
                return value;
            }

            @Override
            public boolean hasPrevious()
            {
                return value != left - delta;
            }

            @Override
            public Integer previous()
            {
                value -= delta;
                return value;
            }

            @Override
            public int nextIndex()
            {
                return (value + delta - left) * delta;
            }

            @Override
            public int previousIndex()
            {
                return (value - delta - left) * delta;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("integer range is read only");
            }

            @Override
            public void set(Integer integer)
            {
                throw new UnsupportedOperationException("integer range is read only");
            }

            @Override
            public void add(Integer integer)
            {
                throw new UnsupportedOperationException("integer range is read only");
            }

            @Override
            public boolean hasNext()
            {
                return value != right;
            }
        }

        private int left;
        private int right;
        private int delta;

        public IntegerRange(int left, int right, int delta)
        {
            this.left = left;
            this.right = right;
            this.delta = delta;
        }

        @Override
        public Iterator<Integer> iterator()
        {
            return new RangeIterator();
        }

        @Override
        public Integer get(int index)
        {
            int ret = left + delta * index;
            if (delta > 0 && ret > right || delta < 0 && ret < right)
            {
                throw new IndexOutOfBoundsException();
            }
            return ret;
        }

        @Override
        public int indexOf(Object o)
        {
            int v = DuckType.asNumber(o).intValue();
            v -= left;
            v *= delta;
            return v >= 0 && v < size() ? v : -1;
        }

        @Override
        public int lastIndexOf(Object o)
        {
            return indexOf(o);
        }

        @Override
        public ListIterator<Integer> listIterator()
        {
            return new RangeIterator();
        }

        @Override
        public ListIterator<Integer> listIterator(int index)
        {
            return new RangeIterator(index);
        }

        @Override
        public int size()
        {
            return Math.abs(right - left) + 1;
        }
    }

    /**
     *  does the real work.  Creates an Vector of Integers with the
     *  right value range
     *
     *  @param context  app context used if Left or Right of .. is a ref
     *  @return Object array of Integers
     * @throws MethodInvocationException
     */
    @Override
    public Object value(InternalContextAdapter context)
        throws MethodInvocationException
    {
        /*
         *  get the two range ends
         */

        Object left = jjtGetChild(0).value( context );
        Object right = jjtGetChild(1).value( context );

        /*
         *  if either is null, lets log and bail
         */

        if (left == null || right == null)
        {
            log.error((left == null ? "Left" : "Right")
                           + " side of range operator [n..m] has null value."
                           + " Operation not possible. "
                           + StringUtils.formatFileString(this));
            return null;
        }

        /*
         *  if not a Number, try to convert
         */

        try
        {
            left = DuckType.asNumber(left);
        }
        catch (NumberFormatException nfe) {}

        try
        {
            right = DuckType.asNumber(right);
        }
        catch (NumberFormatException nfe) {}

        /*
         *  if still not a Number, nothing we can do
         */

        if ( !( left instanceof Number )  || !( right instanceof Number ))
        {
            log.error((!(left instanceof Number) ? "Left" : "Right")
                           + " side of range operator is not convertible to a Number. "
                           + StringUtils.formatFileString(this));
            return null;
        }

        /*
         *  get the two integer values of the ends of the range
         */

        int l = ((Number) left).intValue() ;
        int r = ((Number) right).intValue();

        /*
         *  Determine whether the increment is positive or negative.
         */

        int delta = ( l >= r ) ? -1 : 1;

        /*
         * Build the corresponding integer range
         */

        IntegerRange range = new IntegerRange(l, r, delta);

        /*
         * Returns the range, or a concrete list if mutable ranges are requested by the configuration
         */

        boolean immutable = rsvc.getBoolean(RuntimeConstants.IMMUTABLE_RANGES, true);
        if (immutable)
        {
            return range;
        }
        else
        {
            // backward compatible behavior: the list is instanciated in memory
            int n = Math.abs(r - l) + 1;
            List mutableRange = new ArrayList<>(n);
            for (Iterator<Integer> it = range.iterator(); it.hasNext();)
            {
                mutableRange.add(it.next());
            }
            return mutableRange;
        }
    }

    /**
     * @throws TemplateInitException
     * @see org.apache.velocity.runtime.parser.node.Node#init(org.apache.velocity.context.InternalContextAdapter, java.lang.Object)
     */
    @Override
    public Object init(InternalContextAdapter context, Object data) throws TemplateInitException
    {
    	Object obj = super.init(context, data);
    	cleanupParserAndTokens(); // drop reference to Parser and all JavaCC Tokens
    	return obj;
    }

}
