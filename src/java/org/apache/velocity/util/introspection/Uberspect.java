package org.apache.velocity.util.introspection;

/*
 * Copyright 2002,2004 The Apache Software Foundation.
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

import org.apache.velocity.runtime.RuntimeLogger;

import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * 'Federated' introspection/reflection interface to allow the introspection
 *  behavior in Velocity to be customized.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magusson Jr.</a>
 * @version $Id: Uberspect.java,v 1.1.4.1 2004/03/03 23:23:08 geirm Exp $
 */
public interface Uberspect
{
    /**
     *  Initializer - will be called before use
     */
    public void init() throws Exception;

    /**
     *  To support iteratives - #foreach()
     */
    public Iterator getIterator(Object obj, Info info) throws Exception;

    /**
     *  Returns a general method, corresponding to $foo.bar( $woogie )
     */
    public VelMethod getMethod(Object obj, String method, Object[] args, Info info) throws Exception;

    /**
     * Property getter - returns VelPropertyGet appropos for #set($foo = $bar.woogie)
     */
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info info) throws Exception;

    /**
     * Property setter - returns VelPropertySet appropos for #set($foo.bar = "geir")
     */
    public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info info) throws Exception;
}
