package org.apache.velocity.util.introspection;

/*
 * Copyright 2002-2006 The Apache Software Foundation.
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

/**
 *  JDK 1.5 extension of the Uberspector that allows Iterable Objects to be
 *  put into the Context to be used with #foreach
 *
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class JDK15UberspectImpl
	extends UberspectImpl
{
    /**
     *  To support iterative objects used in a <code>#foreach()</code>
     *  loop.
     *
     * @param obj The iterative object.
     * @param i Info about the object's location.
     */
    public Iterator getIterator(Object obj, Info i)
            throws Exception
    {
    	if (obj instanceof Iterable)
        {
        	return ((Iterable) obj).iterator();
        }
        else
        {
            return super.getIterator(obj, i); 
        }
    }
 }
