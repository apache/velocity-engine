package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.Writer;
import java.io.IOException;

import java.lang.reflect.Method;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.util.ArrayIterator;
import org.apache.velocity.util.EnumerationIterator;

import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.Node;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.IntrospectionCacheData;
import org.apache.velocity.util.introspection.Info;

/**
 * Foreach directive used for moving through arrays,
 * or objects that provide an Iterator.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Foreach.java,v 1.41 2002/04/21 21:00:56 geirm Exp $
 */
public class Foreach extends Directive
{
    /**
     * Return name of this directive.
     */
    public String getName()
    {
        return "foreach";
    }        
    
    /**
     * Return type of this directive.
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
     */
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
        throws Exception
    {
        super.init(rs, context, node);

        counterName = rsvc.getString(RuntimeConstants.COUNTER_NAME);
        counterInitialValue = rsvc.getInt(RuntimeConstants.COUNTER_INITIAL_VALUE);
 
        /*
         *  this is really the only thing we can do here as everything
         *  else is context sensitive
         */

        elementKey = node.jjtGetChild(0).getFirstToken().image.substring(1);

        /*
         * make an uberinfo - saves new's later on
         */

        uberInfo = new Info(context.getCurrentTemplateName(),
                getLine(),getColumn());
    }

    /**
     *  renders the #foreach() block
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
        catch(Exception ee)
        {
            System.out.println(ee);
        }

        if (i == null)
        {
            return false;
        }

        int counter = counterInitialValue;
        
        /*
         *  save the element key if there is one,
         *  and the loop counter
         */

        Object o = context.get(elementKey);
        Object ctr = context.get( counterName);

        while (i.hasNext())
        {
            context.put( counterName , new Integer(counter));
            context.put(elementKey,i.next());
            node.jjtGetChild(3).render(context, writer);
            counter++;
        }

        /*
         * restores the loop counter (if we were nested)
         * if we have one, else just removes
         */
        
        if (ctr != null)
        {
            context.put(counterName, ctr);
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
