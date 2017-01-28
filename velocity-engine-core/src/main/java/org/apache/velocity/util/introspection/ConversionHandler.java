package org.apache.velocity.util.introspection;

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

/**
 * A conversion handler adds admissible conversions between Java types whenever Velocity introspection has to map
 * VTL methods and property accessors to Java methods.
 * Both methods must be consistent: <code>getNeededConverter</code> must not return <code>null</code> whenever
 * <code>isExplicitlyConvertible</code> returned true with the same arguments.
 *
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id: ConversionHandler.java $
 * @since 2.0
 */

public interface ConversionHandler
{
    /**
     * Check to see if the conversion can be done using an explicit conversion
     * @param formal expected formal type
     * @param actual provided argument type
     * @return null if no conversion is needed, or the appropriate Converter object
     * @since 2.0
     */
    boolean isExplicitlyConvertible(Class formal, Class actual, boolean possibleVarArg);

    /**
     * Returns the appropriate Converter object needed for an explicit conversion
     * Returns null if no conversion is needed.
     *
     * @param formal expected formal type
     * @param actual provided argument type
     * @return null if no conversion is needed, or the appropriate Converter object
     * @since 2.0
     */
    Converter getNeededConverter(final Class formal, final Class actual);

    /**
     * Add the given converter to the handler. Implementation should be thread-safe.
     *
     * @param formal expected formal type
     * @param actual provided argument type
     * @param converter converter
     * @since 2.0
     */
    void addConverter(Class formal, Class actual, Converter converter);
}
