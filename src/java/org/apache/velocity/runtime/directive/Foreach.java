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
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.introspection.Info;

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
     * The name of the variable to use when placing
     * iterator hasNext() value into the context.Right
     * now the defailt is $velocityHasNext
     */
    private String hasNextName;

    /**
     * What value to start the loop counter at.
     */
    private int counterInitialValue;

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
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
        throws TemplateInitException
    {
        super.init(rs, context, node);

        counterName = rsvc.getString(RuntimeConstants.COUNTER_NAME);
        hasNextName = rsvc.getString(RuntimeConstants.HAS_NEXT_NAME);
        counterInitialValue = rsvc.getInt(RuntimeConstants.COUNTER_INITIAL_VALUE);
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
             *  TODO : remove if all goes well
             */
            elementKey = sn.getFirstToken().image.substring(1);
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
     */
    protected void put(InternalContextAdapter context, String key, Object value)
    {
        context.put(key, value);
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
            String msg = "Error getting iterator for #foreach at "+uberInfo;
            rsvc.getLog().error(msg, ee);
            throw new VelocityException(msg, ee);
        }

        if (i == null)
        {
            if (skipInvalidIterator)
            {
                return false;
            }
            else
            {
                Node pnode = node.jjtGetChild(2);
                String msg = "#foreach parameter " + pnode.literal() + " at "
                    + Log.formatFileString(pnode)
                    + " is of type " + listObject.getClass().getName()
                    + " and is either of wrong type or cannot be iterated.";
                rsvc.getLog().error(msg);
                throw new VelocityException(msg);
            }
        }

        int counter = counterInitialValue;
        boolean maxNbrLoopsExceeded = false;

        /*
         *  save the element key if there is one, and the loop counter
         */
        Object o = context.get(elementKey);
        Object savedCounter = context.get(counterName);
        Object nextFlag = context.get(hasNextName);
        
        while (!maxNbrLoopsExceeded && i.hasNext())
        {
            put(context, counterName , Integer.valueOf(counter));
            Object value = i.next();
            put(context, hasNextName, Boolean.valueOf(i.hasNext()));
            put(context, elementKey, value);

            try
            {
                node.jjtGetChild(3).render(context, writer);
            }
            catch (Break.BreakException ex)
            {
                // encountered #break directive inside #foreach loop
                break;
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

        /*
         * restores the "hasNext" boolean flag if it exists
         */         
        if( nextFlag != null )
        {
            context.put(hasNextName, nextFlag);
        }
        else
        {
            context.remove(hasNextName);
        }

        return true;
    }
    
    /**
     * We do not allow a word token in any other arg position except for the 2nd since
     * we are looking for the pattern #foreach($foo in $bar).
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
      throws ParseException
    {
        if (argtypes.size() < 3)
        {
            throw new MacroParseException("Too few arguments to the #foreach directive", 
              templateName, t);
        }        
        else if (argtypes.get(0) == ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("Argument 1 of #foreach is of the wrong type",
                templateName, t);
        }
      
        else if (argtypes.get(1) != ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("Expected word 'in' at argument position 2 in #foreach",
                templateName, t);
        }        
        else if (argtypes.get(2) == ParserTreeConstants.JJTWORD)
        {
            throw new MacroParseException("Argument 3 of #foreach is of the wrong type",
                templateName, t);
        }
    }    
    
}
