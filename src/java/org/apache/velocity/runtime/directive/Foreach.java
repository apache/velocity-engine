package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import org.apache.velocity.Context;
import org.apache.velocity.runtime.configuration.*;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.util.ArrayIterator;

import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.Node;

import org.apache.velocity.runtime.exception.NodeException;
import org.apache.velocity.runtime.exception.ReferenceException;

import org.apache.velocity.util.introspection.Introspector;

/**
 * Foreach directive used for moving through arrays,
 * or objects that provide an Iterator.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Foreach.java,v 1.26 2000/11/28 04:16:21 jvanzyl Exp $
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
     * Flag to indicate that the list object being used
     * in an array.
     */
    private final static int INFO_ARRAY = 1;
    
    /**
     * Flag to indicate that the list object being used
     * provides an Iterator.
     */
    private final static int INFO_ITERATOR = 2;
    
    /**
     * Flag to indicate that the list object being used
     * is a Map.
     */
    private final static int INFO_MAP = 3;
    
    /**
     * Flag to indicate that the list object is
     * empty. This is perfectly valid, and nothing
     * will be rendered. When the list object
     * contains values, then the flag will be
     * altered and the rendering will occur.
     */
    private final static int INFO_EMPTY_LIST_OBJECT = 4;
    
    /**
     * The name of the variable to use when placing
     * the counter value into the context. Right
     * now the default is $velocityCount.
     */
    private final static String COUNTER_NAME =
        Runtime.getString(Runtime.COUNTER_NAME);
    
    /**
     * What value to start the loop counter at.
     */
    private final static int COUNTER_INITIAL_VALUE =
        Runtime.getInt(Runtime.COUNTER_INITIAL_VALUE);

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
    
    public void init(Context context, Node node) throws Exception
    {
        Object sampleElement = null;
        Object listObject = null;
        
        elementKey = node.jjtGetChild(0).getFirstToken().image.substring(1);

        /*
         * This is a refence node and it needs to
         * be inititialized.
         */
        node.jjtGetChild(2).init(context, null);
        listObject = node.jjtGetChild(2).value(context);
        
        /* 
         * If the listObject is null then we know that this
         * whole foreach directive is useless. We need to
         * throw a ReferenceException which is caught by
         * the SimpleNode.init() and logged. But we also need
         * to set a flag so that the rendering of this node
         * is ignored as the output would be useless.
         */
        
        /* 
         * Slight problem with this approach. The list object
         * may have been #set previously, this usually wouldn't
         * be the case, but this check should be moved into
         * the rendering phase.
         */
        if (listObject == null)
        {
            node.setInvalid();
            throw new ReferenceException("#foreach", node.jjtGetChild(2));
        }                

        /* 
         * Figure out what type of object the list
         * element is so that we don't have to do it
         * everytime the node is traversed.
         * if (listObject instanceof Object[])
         */
        if (listObject instanceof Object[])
        {
            Object[] arrayObject = ((Object[]) listObject);
            
            if (arrayObject.length == 0)
            {
                node.setInfo(INFO_EMPTY_LIST_OBJECT);
            }                
            else
            {
                node.setInfo(INFO_ARRAY);
                sampleElement = arrayObject[0];
            }                    
        }            
        else if (Introspector.implementsMethod(listObject, "iterator"))
        {
            if (((Collection) listObject).size() == 0)
            {
                node.setInfo(INFO_EMPTY_LIST_OBJECT);
            }                
            else
            {
                node.setInfo(INFO_ITERATOR);
                sampleElement = ((Collection) listObject).iterator().next();
            }                    
        }
        else if (Introspector.implementsMethod(listObject, "values"))
        {
            if (((Map) listObject).size() == 0)
            {
                node.setInfo(INFO_EMPTY_LIST_OBJECT);
            }
            else
            {
                node.setInfo(INFO_MAP);
                sampleElement = ((Map) listObject).values().iterator().next();
            }                    
        }
        else
        {
            // If it's not an array or an object that provides
            // an iterator then the node is invalid and should
            // not be rendered.
            node.setInvalid();
            Runtime.warn ("Could not determine type of iterator for #foreach loop ");
            throw new NodeException ("Could not determine type of iterator for #foreach loop " , node);
        }            
        
        // This is a little trick so that we can initialize
        // all the blocks in the foreach  properly given
        // that there are references that refer to the
        // elementKey name.
        if (sampleElement != null)
        {
            context.put(elementKey, sampleElement);
            super.init(context, node);
            context.remove(elementKey);
        }            
    }

    public boolean render(Context context, Writer writer, Node node)
        throws IOException
    {
        int counter;
        Iterator i;
        Object listObject = null;
        
        /*
         * If the node has been set to invalid then it is because
         * the list object placed in the context is not an array,
         * does not provide an Iterator, or is not Map. In these
         * cases there is nothing we can do, and nothing will
         * be rendered.
         */
        if (node.isInvalid())
            return false;
            
        if (node.getInfo() == INFO_EMPTY_LIST_OBJECT)
        {
            /*
             * If the list object had no elements
             * then lets try to init again. If the list
             * object is still empty then an exception
             * will be thrown again. But we will keep
             * trying!
             */
            synchronized(this)
            {
                /* 
                 * Check again for other threads that
                 * got through above. Don't want to
                 * have to synchronize on every render,
                 * but we don't want to init() multiple
                 * times here either. A few threads might
                 * get into this block, but only one thread
                 * will try the init().
                 */
                if (node.getInfo() == INFO_EMPTY_LIST_OBJECT)
                {
                    try
                    {
                        init(context, node);
                        
                        /*
                         * Check again, if the list is still
                         * empty then return false.
                         */
                        if (node.getInfo() == INFO_EMPTY_LIST_OBJECT)
                            return false;
                    }
                    catch (Exception e)
                    {
                        return false;
                    }
                }
            }
        }
    
        listObject = node.jjtGetChild(2).value(context);

        if (node.getInfo() == INFO_ARRAY)
            i = new ArrayIterator((Object[]) listObject);
        else if (node.getInfo() == INFO_MAP)
            i = ((Map) listObject).values().iterator();
        else            
            i = ((Collection) listObject).iterator();
        
        counter = COUNTER_INITIAL_VALUE;
        while (i.hasNext())
        {
            context.put(COUNTER_NAME, new Integer(counter));
            context.put(elementKey,i.next());
            node.jjtGetChild(3).render(context, writer);
            counter++;
        }

        context.remove(COUNTER_NAME);
        context.remove(elementKey);
    
        return true;
    }

}
