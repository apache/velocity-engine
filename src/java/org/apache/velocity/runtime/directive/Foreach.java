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
import java.util.Iterator;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

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
     * A special context to use when the foreach iterator returns a null.  This
     * is required since the standard context may not support nulls.
     * All puts and gets are passed through, except for the foreach iterator key.
     */
    protected static class NullHolderContext implements InternalContextAdapter
    {
        private InternalContextAdapter  innerContext = null;
        private String   loopVariableKey = "";
        private boolean  active = true;

        /**
         * Create the context as a wrapper to be used within the foreach
         * @param key the reference used in the foreach
         * @param context the parent context
         */
        private NullHolderContext( String key, InternalContextAdapter context )
        {
           innerContext = context;
           if( key != null )
               loopVariableKey = key;
        }

        /**
         * Get an object from the context, or null if the key is equal to the loop variable
         * @see org.apache.velocity.context.InternalContextAdapter#get(java.lang.String)
         * @exception MethodInvocationException passes on potential exception from reference method call
         */
        public Object get( String key ) throws MethodInvocationException
        {
            return ( active && loopVariableKey.equals(key) )
                ? null
                : innerContext.get(key);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#put(java.lang.String key, java.lang.Object value)
         */
        public Object put( String key, Object value )
        {
            if( loopVariableKey.equals(key) && (value == null) )
            {
                active = true;
            }

            return innerContext.put( key, value );
        }

        /**
         * Allows callers to explicitly put objects in the local context.
         * Objects added to the context through this method always end up
         * in the top-level context of possible wrapped contexts.
         *
         * @param key name of item to set.
         * @param value object to set to key.
         * @see org.apache.velocity.context.InternalWrapperContext#localPut(String, Object)
         */        
        public Object localPut(final String key, final Object value)
        {
            return put(key, value);
        }

       /**
         * Does the context contain the key
         * @see org.apache.velocity.context.InternalContextAdapter#containsKey(java.lang.Object key)
         */
        public boolean containsKey( Object key )
        {
            return innerContext.containsKey(key);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getKeys()
         */
        public Object[] getKeys()
        {
           return innerContext.getKeys();
        }

        /**
         * Remove an object from the context
         * @see org.apache.velocity.context.InternalContextAdapter#remove(java.lang.Object key)
         */
        public Object remove(Object key)
        {
           if( loopVariableKey.equals(key) )
           {
             active = false;
           }
           return innerContext.remove(key);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#pushCurrentTemplateName(java.lang.String s)
         */
        public void pushCurrentTemplateName(String s)
        {
            innerContext.pushCurrentTemplateName(s);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#popCurrentTemplateName()
         */
        public void popCurrentTemplateName()
        {
            innerContext.popCurrentTemplateName();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getCurrentTemplateName()
         */
        public String getCurrentTemplateName()
        {
            return innerContext.getCurrentTemplateName();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getTemplateNameStack()
         */
        public Object[] getTemplateNameStack()
        {
            return innerContext.getTemplateNameStack();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#icacheGet(java.lang.Object key)
         */
        public IntrospectionCacheData icacheGet(Object key)
        {
            return innerContext.icacheGet(key);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#icachePut(java.lang.Object key, org.apache.velocity.util.introspection.IntrospectionCacheData o)
         */
        public void icachePut(Object key, IntrospectionCacheData o)
        {
            innerContext.icachePut(key,o);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#setCurrentResource(org.apache.velocity.runtime.resource.Resource r)
         */
        public void setCurrentResource( Resource r )
        {
            innerContext.setCurrentResource(r);
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getCurrentResource()
         */
        public Resource getCurrentResource()
        {
            return innerContext.getCurrentResource();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getBaseContext()
         */
        public InternalContextAdapter getBaseContext()
        {
            return innerContext.getBaseContext();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getInternalUserContext()
         */
        public Context getInternalUserContext()
        {
            return innerContext.getInternalUserContext();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#attachEventCartridge(org.apache.velocity.app.event.EventCartridge ec)
         */
        public EventCartridge attachEventCartridge(EventCartridge ec)
        {
            EventCartridge cartridge = innerContext.attachEventCartridge( ec );

            return cartridge;
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getEventCartridge()
         */
        public EventCartridge getEventCartridge()
        {
            return innerContext.getEventCartridge();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#getAllowRendering()
         */
        public boolean getAllowRendering()
        {
            return innerContext.getAllowRendering();
        }

        /**
         * @see org.apache.velocity.context.InternalContextAdapter#setAllowRendering(boolean v)
         */
        public void setAllowRendering(boolean v)
        {
            innerContext.setAllowRendering(v);
        }

    }

    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "foreach";
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
     * The name of the variable to use when placing
     * the counter value into the context. Right
     * now the default is $velocityCount.
     */
    private String counterName;

    /**
     * What value to start the loop counter at.
     */
    private int counterInitialValue;

    /**
     * The maximum number of times we're allowed to loop.
     */
    private int maxNbrLoops;

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
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
        throws TemplateInitException
    {
        super.init(rs, context, node);

        counterName = rsvc.getString(RuntimeConstants.COUNTER_NAME);
        counterInitialValue = rsvc.getInt(RuntimeConstants.COUNTER_INITIAL_VALUE);
        maxNbrLoops = rsvc.getInt(RuntimeConstants.MAX_NUMBER_LOOPS,
                                  Integer.MAX_VALUE);
        if (maxNbrLoops < 1)
        {
            maxNbrLoops = Integer.MAX_VALUE;
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
             *  TODO : remove if all goes well
             */
            elementKey = sn.getFirstToken().image.substring(1);
        }

        /*
         * make an uberinfo - saves new's later on
         */

        uberInfo = new Info(context.getCurrentTemplateName(),
                getLine(),getColumn());
    }

    /**
     *  renders the #foreach() block
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     * @throws MethodInvocationException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     */
    public boolean render(InternalContextAdapter context,
                           Writer writer, Node node)
        throws IOException,  MethodInvocationException, ResourceNotFoundException,
        	ParseErrorException
    {
        /*
         *  do our introspection to see what our collection is
         */

        Object listObject = node.jjtGetChild(2).value(context);

        if (listObject == null)
             return false;

        Iterator i = null;

        try
        {
            i = rsvc.getUberspect().getIterator(listObject, uberInfo);
        }
        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            throw e;
        }
        catch(Exception ee)
        {
            rsvc.getLog().error("Error getting iterator for #foreach", ee);
        }

        if (i == null)
        {
            return false;
        }

        int counter = counterInitialValue;
        boolean maxNbrLoopsExceeded = false;

        /*
         *  save the element key if there is one, and the loop counter
         */
        Object o = context.get(elementKey);
        Object savedCounter = context.get(counterName);

        /*
         * Instantiate the null holder context if a null value
         * is returned by the foreach iterator.  Only one instance is
         * created - it's reused for every null value.
         */
        NullHolderContext nullHolderContext = null;

        while (!maxNbrLoopsExceeded && i.hasNext())
        {
            // TODO: JDK 1.4+ -> valueOf()
            context.localPut(counterName , new Integer(counter));
            Object value = i.next();
            context.localPut(elementKey, value);

            /*
             * If the value is null, use the special null holder context
             */
            if( value == null )
            {
                if( nullHolderContext == null )
                {
                    // lazy instantiation
                    nullHolderContext = new NullHolderContext(elementKey, context);
                }
                node.jjtGetChild(3).render(nullHolderContext, writer);
            }
            else
            {
                node.jjtGetChild(3).render(context, writer);
            }
            counter++;

            // Determine whether we're allowed to continue looping.
            // ASSUMPTION: counterInitialValue is not negative!
            maxNbrLoopsExceeded = (counter - counterInitialValue) >= maxNbrLoops;
        }

        /*
         * restores the loop counter (if we were nested)
         * if we have one, else just removes
         */

        if (savedCounter != null)
        {
            context.put(counterName, savedCounter);
        }
        else
        {
            context.remove(counterName);
        }


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

        return true;
    }
}
