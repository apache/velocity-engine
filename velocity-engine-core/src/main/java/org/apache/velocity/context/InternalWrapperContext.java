package org.apache.velocity.context;

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
 *  interface for internal context wrapping functionality
 *
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @version $Id$
 */
public interface InternalWrapperContext
{

    /**
     * Returns the wrapped user context.
     * @return The wrapped user context.
     */
    Context getInternalUserContext();

    /**
     * Returns the base full context impl.
     * @return The base full context impl.
     *
     */
    InternalContextAdapter getBaseContext();

    /**
     * Retrieve the specified key value pair from the given scope.
     */
    Object put(String key, Object value);

    /**
     * Place key value pair into the context of the specified scope.
     */
    Object get(String key);

    /**
     * Tests if the key exists in the specified scope
     */
    boolean containsKey(String key);

}
