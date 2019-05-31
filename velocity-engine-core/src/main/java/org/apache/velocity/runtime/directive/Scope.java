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

import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.Parser;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This handles context scoping and metadata for directives.
 *
 * @author Nathan Bubna
 * @version $Id$
 */
public class Scope extends AbstractMap
{
    private static final String setReturnValue = "";
    private Map storage;
    private Object replaced;
    private Scope parent;
    private Info info;
    protected final Object owner;

    /**
     * @param owner
     * @param previous
     */
    public Scope(Object owner, Object previous)
    {
        this.owner = owner;
        if (previous != null)
        {
            try
            {
                this.parent = (Scope)previous;
            }
            catch (ClassCastException cce)
            {
                this.replaced = previous;
            }
        }
    }

    private Map getStorage()
    {
        if (storage == null)
        {
            storage = new HashMap();
        }
        return storage;
    }

    /**
     * @return entry set
     */
    public Set entrySet()
    {
        return getStorage().entrySet();
    }

    /**
     * getter
     * @param key
     * @return found value
     */
    @Override
    public Object get(Object key)
    {
        Object o = super.get(key);
        if (o == null && parent != null && !containsKey(key))
        {
            return parent.get(key);
        }
        return o;
    }

    /**
     * setter
     * @param key
     * @param value
     * @return previous value
     */
    @Override
    public Object put(Object key, Object value)
    {
        return getStorage().put(key, value);
    }

    /**
     * Convenience method to call put(key,val) in a template
     * without worrying about what is returned/rendered by the call.
     * This should ALWAYS return an empty string.
     * @param key
     * @param value
     * @return empty string
     */
    public String set(Object key, Object value)
    {
        put(key, value);
        return setReturnValue;
    }

    /**
     * Allows #stop to easily trigger the proper StopCommand for this scope.
     */
    protected void stop()
    {
        throw new StopCommand(owner);
    }

    /**
     * Returns the number of control arguments of this type
     * that are stacked up.  This is the distance between this
     * instance and the topmost instance, plus one. This value
     * will never be negative or zero.
     * @return depth
     */
    protected int getDepth()
    {
        if (parent == null)
        {
            return 1;
        }
        return parent.getDepth() + 1;
    }

    /**
     * Returns the topmost parent control reference, retrieved
     * by simple recursion on {@link #getParent}.
     * @return top-most scope
     */
    public Scope getTopmost()
    {
        if (parent == null)
        {
            return this;
        }
        return parent.getTopmost();
    }

    /**
     * Returns the parent control reference overridden by the placement
     * of this instance in the context.
     * @return parent scope
     */
    public Scope getParent()
    {
        return parent;
    }

    /**
     * Returns the user's context reference overridden by the placement
     * of this instance in the context.  If there was none (as is hoped),
     * then this will return null.  This never returns parent controls;
     * those are returned by {@link #getParent}.
     * @return replaced reference value, or null
     */
    public Object getReplaced()
    {
        if (replaced == null && parent != null)
        {
            return parent.getReplaced();
        }
        return replaced;
    }

    /**
     * Returns info about the current scope for debugging purposes.
     * @return template debugging infos
     */
    public Info getInfo()
    {
        if (info == null)
        {
            info = new Info(this, owner);
        }
        return info;
    }

    /**
     * Class to encapsulate and provide access to info about
     * the current scope for debugging.
     */
    public static class Info
    {
        private Scope scope;
        private Directive directive;
        private Template template;

        /**
         * c'tor
         * @param scope
         * @param owner
         */
        public Info(Scope scope, Object owner)
        {
            if (owner instanceof Directive)
            {
                directive = (Directive)owner;
            }
            if (owner instanceof Template)
            {
                template = (Template)owner;
            }
            this.scope = scope;
        }

        /**
         * name getter
         * @return name
         */
        public String getName()
        {
            if (directive != null)
            {
                return directive.getName();
            }
            if (template != null)
            {
                return template.getName();
            }
            return null;
        }

        /**
         * type getter
         * @return scope type
         */
        public String getType()
        {
            if (directive != null)
            {
                switch (directive.getType())
                {
                    case Directive.BLOCK:
                        return "block";
                    case Directive.LINE:
                        return "line";
                }
            }
            if (template != null)
            {
                return template.getEncoding();
            }
            return null;
        }

        /**
         * current depth
         * @return depth
         */
        public int getDepth()
        {
            return scope.getDepth();
        }

        /**
         * template name getter
         * @return template name
         */
        public String getTemplate()
        {
            if (directive != null)
            {
                return directive.getTemplateName();
            }
            if (template != null)
            {
                return template.getName();
            }
            return null;
        }

        /**
         * line getter
         * @return line number
         */
        public int getLine()
        {
            if (directive != null)
            {
                return directive.getLine();
            }
            return 0;
        }

        /**
         * column getter
         * @return column number
         */
        public int getColumn()
        {
            if (directive != null)
            {
                return directive.getColumn();
            }
            return 0;
        }

        /**
         * string representation getter
         * @return string representation
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            if (directive != null)
            {
                sb.append('#'); // parser characters substitution is not heandled here
            }
            sb.append(getName());
            sb.append("[type:").append(getType());
            int depth = getDepth();
            if (depth > 1)
            {
                sb.append(" depth:").append(depth);
            }
            if (template == null)
            {
                String vtl = getTemplate();
                sb.append(" template:");
                if (!vtl.contains(" "))
                {
                    sb.append(vtl);
                }
                else
                {
                    sb.append('"').append(vtl).append('"');
                }
                sb.append(" line:").append(getLine());
                sb.append(" column:").append(getColumn());
            }
            sb.append(']');
            return sb.toString();
        }
    }

}
