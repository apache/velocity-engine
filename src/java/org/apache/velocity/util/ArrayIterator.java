package org.apache.velocity.util;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.reflect.Array;


/**
 *  <p>
 *  An Iterator wrapper for an Object[]. This will
 *  allow us to deal with all array like structures
 *  in a consistent manner.
 *  </p>
 *  <p>
 *  WARNING : this class's operations are NOT synchronized.
 *  It is meant to be used in a single thread, newly created
 *  for each use in the #foreach() directive.
 *  If this is used or shared, synchronize in the
 *  next() method.
 *  </p>
 *
 * @version $Id: ArrayIterator.java,v 1.7 2004/02/27 18:43:21 dlr Exp $
 */
public class ArrayIterator implements Iterator
{
    /**
     * The objects to iterate.
     */
    private Object array;

    /**
     * The current position and size in the array.
     */
    private int pos;
    private int size;

    /**
     * Creates a new iterator instance for the specified array.
     *
     * @param array The array for which an iterator is desired.
     */
    public ArrayIterator(Object array)
    {
        /*
         * if this isn't an array, then throw.  Note that this is 
         * for internal use - so this should never happen - if it does
         *  we screwed up.
         */
         
        if ( !array.getClass().isArray() )
        {   
            throw new IllegalArgumentException( 
                "Programmer error : internal ArrayIterator invoked w/o array");
        }
            
        this.array = array;
        pos = 0;
        size = Array.getLength( this.array );
    }

    /**
     * Move to next element in the array.
     *
     * @return The next object in the array.
     */
    public Object next()
    {
        if (pos < size )
            return Array.get( array, pos++);
                
        /*
         *  we screwed up...
         */
         
        throw new NoSuchElementException("No more elements: " + pos +
                                         " / " + size);
    }
    
    /**
     * Check to see if there is another element in the array.
     *
     * @return Whether there is another element.
     */
    public boolean hasNext()
    {
        return (pos < size );
    }

    /**
     * No op--merely added to satify the <code>Iterator</code> interface.
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
