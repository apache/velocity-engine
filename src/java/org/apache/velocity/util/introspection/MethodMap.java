package org.apache.velocity.util.introspection;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;

import java.lang.reflect.Method;

/**
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:bob@werken.com">Bob McWhirter</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: MethodMap.java,v 1.13.2.3 2002/07/25 01:35:04 geirm Exp $
 */
public class MethodMap
{
    protected static final Object OBJECT = new Object();

    /**
     * Keep track of all methods with the same name.
     */
    Map methodByNameMap = new Hashtable();

    /**
     * Add a method to a list of methods by name.
     * For a particular class we are keeping track
     * of all the methods with the same name.
     */
    public void add(Method method)
    {
        String methodName = method.getName();

        List l = (List) methodByNameMap.get(methodName);

        if (l == null)
        {
            l = new ArrayList();
            methodByNameMap.put(methodName, l);
        }            

        l.add(method);

        return;
    }
    
    /**
     * Return a list of methods with the same name.
     *
     * @param String key
     * @return List list of methods
     */
    public List get(String key)
    {
        return (List) methodByNameMap.get(key);
    }

    /**
     *  <p>
     *  Find a method.  Attempts to find the 
     *  most appropriate method using the
     *  sense of 'specificity'.
     *  </p>
     * 
     *  <p>
     *  This turns out to be a relatively rare case
     *  where this is needed - however, functionality
     *  like this is needed.  This may not be the
     *  optimum approach, but it works.
     *  </p>
     *
     *  @param String name of method
     *  @param Object[] params
     *  @return Method
     */
    public Method find(String methodName, Object[] params)
        throws AmbiguousException
    {
        List methodList = (List) methodByNameMap.get(methodName);
        
        if (methodList == null)
        {
            return null;
        }

        Class[] parameterTypes = null;
        Method  method = null;

        int numMethods = methodList.size();
        
        int bestDistance  = -2;
        Method bestMethod = null;
        Twonk bestTwonk = null;
        boolean ambiguous = false;
        
        for (int i = 0; i < numMethods; i++)
        {
            method = (Method) methodList.get(i);
            parameterTypes = method.getParameterTypes();
            
            /*
             * The methods we are trying to compare must
             * the same number of arguments.
             */

            if (parameterTypes.length == params.length)
            {
                /*
                 *  use the calling parameters as the baseline
                 *  and calculate the 'distance' from the parameters
                 *  to the method args.  This will be useful when
                 *  determining specificity
                 */
                 
                Twonk twonk = calcDistance(params, parameterTypes);
                
                if (twonk != null)
                {
                    /*
                     *  if we don't have anything yet, take it
                     */
                     
                    if (bestTwonk == null)
                    {
                        bestTwonk = twonk;
                        bestMethod = method;
                    }
                    else
                    {
                        /*
                         * now see which is more specific, this current
                         * versus what we think of as the best candidate
                         */
                         
                        int val = twonk.moreSpecific(bestTwonk);
                         
                        //System.out.println("Val = " + val + " for " + method + " vs " + bestMethod );
                            
                        if (val == 0)
                        {
                            /*
                             * this means that the parameters 'crossed'
                             * therefore, it's ambiguous because one is as 
                             * good as the other
                             */
                            ambiguous = true;
                        }
                        else if (val == 1)
                        {
                            /*
                             *  the current method is clearly more
                             *  specific than the current best, so
                             *  we take the current we are testing
                             *  and clear the ambiguity flag
                             */
                            ambiguous = false;
                            bestTwonk = twonk;
                            bestMethod = method;
                        }
                    }
                }        
               
            }
        }

        /*
         *  if ambiguous is true, it means we couldn't decide
         *  so inform the caller...
         */

        if (ambiguous)
        {    
            throw new AmbiguousException();
        }
           
        return bestMethod;
    }

    /**
     *  Calculates the distance, expressed as a vector of inheritance
     *  steps, between the calling args and the method args.
     *  There still is an issue re interfaces...
     */
    private Twonk calcDistance(Object[] set, Class[] base)
    {
        if (set.length != base.length)
            return null;
            
        Twonk twonk = new Twonk(set.length);
        
        int distance = 0;
        
        for (int i = 0; i < set.length; i++)
        {
            /* 
             * can I get from here to there?
             */

            Object invocationArg = set[i];
            Class methodClass = base[i];

            if (invocationArg == null)
            {
                invocationArg = OBJECT;
            }

            Class setclass = invocationArg.getClass();

            if (!methodClass.isAssignableFrom(setclass))
            {
                /*
                 * if the arg is null and methodClass isn't primitive then
                 *  that's ok
                 */

                if (set[i] == null && !methodClass.isPrimitive())
                {
                    continue;
                }
                else if (checkPrimitive(methodClass, setclass))
                {
                    /*
                     * if we are dealing with primitives and it's ok...
                     */

                    continue;
                }
                else
                {
                    return null;
                }
            }

            /*
             * ok, I can.  How many steps?
             */
           
            Class c = setclass;
                      
            while (c != null)
            {      
                /*
                 * is this a valid step?
                 */
                 
                if (!methodClass.isAssignableFrom(c))
                {      
                    /*
                     *  it stopped being assignable - therefore we are looking at
                     *  an interface as our target, so move back one step
                     *  from the distance as the stop wasn't valid
                     */
                    break;
                }
                
                if (methodClass.equals(c))
                {
                    /*
                     *  we are equal, so no need to move forward
                     */
                     
                    break;
                }

                c = c.getSuperclass();
                twonk.distance++;
                twonk.vec[i]++;
            }
         }
                
        return twonk;
    }

    /**
     *  check for primitive and widening.  Take from the 1.4 code
     */
    private boolean checkPrimitive(Class formal, Class arg)
    {

        if(formal.isPrimitive())
        {
            if(formal == Boolean.TYPE && arg == Boolean.class)
            {
                return true;
            }

            if(formal == Character.TYPE && arg == Character.class)
            {
                return true;
            }

            if(formal == Byte.TYPE && arg == Byte.class)
            {
                return true;
            }

            if(formal == Short.TYPE &&
                    (arg == Short.class || arg == Byte.class))
            {
                return true;
            }

            if(formal == Integer.TYPE &&
               (arg == Integer.class || arg == Short.class ||
                arg == Byte.class))
            {
                return true;
            }

            if(formal == Long.TYPE &&
               (arg == Long.class || arg == Integer.class ||
                arg == Short.class || arg == Byte.class))
            {
                return true;
            }

            if(formal == Float.TYPE &&
               (arg == Float.class || arg == Long.class ||
                arg == Integer.class || arg == Short.class ||
                arg == Byte.class))
            {
                return true;
            }

            if(formal == Double.TYPE &&
               (arg == Double.class || arg == Float.class ||
                arg == Long.class || arg == Integer.class ||
                arg == Short.class || arg == Byte.class))
            {
                return true;
            }
        }

        return false;
    }

    /**
     *  simple distinguishable exception, used when 
     *  we run across ambiguous overloading
     */
    public class AmbiguousException extends Exception
    {
    }

    /**
     *  little class to hold 'distance' information
     *  for calling params, as well as determine
     *  specificity
     */
    private class Twonk
    {
        public int distance;
        public int[] vec;
        
        public Twonk(int size)
        {
            vec = new int[size];
        }
        
        public int moreSpecific(Twonk other)
        {
            if (other.vec.length != vec.length)
            {
                return -1;
            }

            boolean low = false;
            boolean high = false;
            
            for (int i = 0; i < vec.length; i++)
            {
                if ( vec[i] > other.vec[i])
                {
                    high = true;
                }
                else if (vec[i] < other.vec[i])
                {
                    low = true;
                }                    
            }
            
            /*
             *  this is a 'crossing' - meaning that
             *  we saw the parameter 'slopes' cross
             *  this means ambiguity
             */
            if (high && low)
            {
                return 0;
            }

            /*
             *  we saw that all args were 'high', meaning
             *  that the other method is more specific so
             *  we are less
             */
            if (high && !low)
            {
                return -1;
            }

            /*
             *  we saw that all points were lower, therefore
             *  we are more specific
             */
            if (!high && low)
            {
                return 1;
            }

            /*
             *  the remainder, neither high or low
             *  means we are the same.  This really can't 
             *  happen, as it implies the same args, right?
             */
             
            return 1;
        }
    }
}
