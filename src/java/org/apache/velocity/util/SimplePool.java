package org.apache.velocity.util;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Simple object pool. Based on ThreadPool and few other classes
 *
 * The pool will ignore overflow and return null if empty.
 *
 * @author Gal Shachor
 * @author Costin
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
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

        synchronized(this)
        {
            /*
             *  if we aren't full
             */

            if (current < max - 1)
            {
                /*
                 *  then increment the
                 *  current index.
                 */
                idx = ++current;
            }

            if (idx >= 0)
            {
                pool[idx] = o;
            }
        }
    }

    /**
     * Get an object from the pool, null if the pool is empty.
     */
    public Object get()
    {
        synchronized(this)
        {
            /*
             *  if we have any in the pool
             */
            if( current >= 0 )
            {
                /*
                 *  remove the current one
                 */

                Object o = pool[current];
                pool[current] = null;

                current--;

                return o;
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

    /**
     *   for testing purposes, so we can examine the pool
     *
     * @return
     */
    Object[] getPool()
    {
        return pool;
    }
}
