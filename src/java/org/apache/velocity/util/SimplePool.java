package org.apache.velocity.util;

/*
 * $Header: /home/cvs/jakarta-velocity/src/java/org/apache/velocity/util/SimplePool.java,v 1.2 2001/03/31 05:25:22 geirm Exp $
 * $Revision: 1.2 $
 * $Date: 2001/03/31 05:25:22 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

/**
 * Simple object pool. Based on ThreadPool and few other classes
 *
 * The pool will ignore overflow and return null if empty.
 *
 * @author Gal Shachor
 * @author Costin
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: SimplePool.java,v 1.2 2001/03/31 05:25:22 geirm Exp $
 */
public final class SimplePool  
{
    /*
     * Where the objects are held.
     */
    private Object pool[];

    /**
     *  max amount of objects to be managed
     *  set via CTOR
     */
    private int max;    
    
    /**
     *  index of previous to next
     *  free slot
     */
    private int current=-1;
       
    public SimplePool(int max) 
    {
        this.max = max;
        pool = new Object[max];
    } 

    /**
     * Add the object to the pool, silent nothing if the pool is full
     */
    public void put(Object o) 
    {
        int idx=-1;
     
        synchronized( this ) 
        {
            /*
             *  if we aren't full
             */

            if( current < max - 1 )
            {
                /*
                 *  then increment the 
                 *  current index.
                 */
                idx = ++current;
            }

            if( idx >= 0 ) 
            {
                pool[idx] = o;
            }
        }
    }

    /**
     * Get an object from the pool, null if the pool is empty.
     */
    public  Object get() 
    {
        int idx = -1;
        
        synchronized( this ) 
        {
            /*
             *  if we have any in the pool
             */
            if( current >= 0 )
            {
                /*
                 *  take one out, so to speak -
                 *  separate the two operations
                 *  to make it clear that you
                 *  don't want idx = --current; :)
                 */

                idx = current;
                current--;
               
                /*
                 *  and since current was >= 0
                 *  to get in here, idx must be as well
                 *  so save the if() opration
                 */

                return pool[idx];
            }
        }
        
        return null;
    }

    /** Return the size of the pool
     */
    public int getMax() 
    {
        return max;
    }
}
