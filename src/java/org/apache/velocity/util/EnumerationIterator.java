package org.apache.velocity.util;

/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Iterator;
import java.util.Enumeration;

/**
 * An Iterator wrapper for an Enumeration.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: EnumerationIterator.java,v 1.1.14.1 2004/03/03 23:23:07 geirm Exp $
 */
public class EnumerationIterator implements Iterator
{
    /**
     * The enumeration to iterate.
     */
    private Enumeration enum = null;

    /**
     * Creates a new iteratorwrapper instance for the specified 
     * Enumeration.
     *
     * @param enum  The Enumeration to wrap.
     */
    public EnumerationIterator( Enumeration enum)
    {
        this.enum = enum;
    }

    /**
     * Move to next element in the array.
     *
     * @return The next object in the array.
     */
    public Object next()
    {
        return enum.nextElement();
    }
    
    /**
     * Check to see if there is another element in the array.
     *
     * @return Whether there is another element.
     */
    public boolean hasNext()
    {
        return enum.hasMoreElements();
    }

    /**
     *  Unimplemented.  No analogy in Enumeration
     */
    public void remove()
    {
        // not implemented
    }
   
}
