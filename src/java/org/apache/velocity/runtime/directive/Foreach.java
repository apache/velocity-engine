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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.util.ArrayIterator;

import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.Node;

import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.util.introspection.Introspector;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

/**
 * Foreach directive used for moving through arrays,
 * or objects that provide an Iterator.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Foreach.java,v 1.33 2001/03/19 17:12:58 geirm Exp $
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

    private final static int UNKNOWN = -1;
    
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
     * Flag to indicate that the list object being used
     * is a Collection.
     */
    private final static int INFO_COLLECTION = 4;
  
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

    /**
     *  simple init - init the tree and get the elementKey from
     *  the AST
     */
    public void init( InternalContextAdapter context, Node node) 
        throws Exception
    {
        super.init( context, node );

        /*
         *  this is really the only thing we can do here as everything
         *  else is context sensitive
         */

        elementKey = node.jjtGetChild(0).getFirstToken().image.substring(1);
    }

    /**
     *  returns an Iterator to the collection in the #foreach()
     *
     *  @param context  current context
     *  @param node   AST node
     *  @return Iterator to do the dataset
     */
    private Iterator getIterator( InternalContextAdapter context, Node node )
        throws MethodInvocationException
    {
        /*
         *  get our list object, and punt if it's null.
         */

        Object listObject = node.jjtGetChild(2).value(context);
        
        if (listObject == null)
            return null;

        /*
         *  See if we already know what type this is. Use the introspection cache
         */

        int type = UNKNOWN;

        IntrospectionCacheData icd = context.icacheGet( this ); 
        Class c = listObject.getClass();

        /*
         *  if we have an entry in the cache, and the Class we have
         *  cached is the same as the Class of the data object
         *  then we are ok
         */

        if ( icd != null && icd.contextData == c )
        {
            /* dig the type out of the cata object */
            type = ((Integer) icd.thingy ).intValue();
        }

        /* 
         * If we still don't know what this is, 
         * figure out what type of object the list
         * element is, and get the iterator for it
         */

        if ( type == UNKNOWN )
        {
            if (listObject instanceof Object[])
                type = INFO_ARRAY;
            else if ( listObject instanceof Collection)
                type = INFO_COLLECTION;
            else if ( listObject instanceof Map )
                type = INFO_MAP;
            else if ( listObject instanceof Iterator )
                type = INFO_ITERATOR;

            /*
             *  if we did figure it out, cache it
             */

            if ( type != UNKNOWN )
            {
                icd = new IntrospectionCacheData();
                icd.thingy = new Integer( type );
                icd.contextData = c;
                context.icachePut( this, icd );
            }
        }

        /*
         *  now based on the type from either cache or examination...
         */

        switch( type ) {
            
        case INFO_COLLECTION :        
            return ( (Collection) listObject).iterator();        

        case INFO_ITERATOR :        
            Runtime.warn ("Warning! The reference " 
                          + node.jjtGetChild(2).getFirstToken().image
                          + " is an Iterator in the #foreach() loop at ["
                          + getLine() + "," + getColumn() + "]"
                          + " in template " + context.getCurrentTemplateName() 
                          + ". If used in more than once, this may lead to unexpected results.");

            return ( (Iterator) listObject);       

        case INFO_ARRAY:
            return new ArrayIterator( (Object [] )  listObject );

        case INFO_MAP:          
            return ( (Map) listObject).values().iterator();

        default:
        
            /*  we have no clue what this is  */
            Runtime.warn ("Could not determine type of iterator in " 
                          +  "#foreach loop for " +  node.jjtGetChild(2).getFirstToken().image 
                          + " at [" + getLine() + "," + getColumn() + "]"
                          + " in template " + context.getCurrentTemplateName() );            

            return null;
        }
    }

    /**
     *  renders the #foreach() block
     */
    public boolean render( InternalContextAdapter context, Writer writer, Node node)
        throws IOException,  MethodInvocationException
    {        
        /*
         *  do our introspection to see what our collection is
         */

        Iterator i = getIterator( context, node );
   
        if ( i == null )
            return false;
        
        int counter = COUNTER_INITIAL_VALUE;
        
        /*
         *  save the element key if there is one
         */

        Object o = context.get( elementKey );

        while (i.hasNext())
        {
            context.put(COUNTER_NAME, new Integer(counter));
            context.put(elementKey,i.next());
            node.jjtGetChild(3).render(context, writer);
            counter++;
        }

        context.remove(COUNTER_NAME);
        context.remove(elementKey);

        /*
         *  restores element key if exists
         */

        if (o != null)
            context.put( elementKey, o );

        return true;
    }

}



