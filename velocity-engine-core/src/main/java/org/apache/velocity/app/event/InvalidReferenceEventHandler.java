package org.apache.velocity.app.event;

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

import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

/**
 * Event handler called when an invalid reference is encountered.  Allows 
 * the application to report errors or substitute return values. May be chained
 * in sequence; the behavior will differ per method.
 * 
 * <p>This feature should be regarded as experimental.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 * @since 1.5
 */
public interface InvalidReferenceEventHandler extends EventHandler
{
    
    /**
     * Called when object is null or there is no getter for the given 
     * property.  Also called for invalid references without properties.  
     * invalidGetMethod() will be called in sequence for
     * each link in the chain until the first non-null value is
     * returned.
     * 
     * @param context the context when the reference was found invalid
     * @param reference string with complete invalid reference
     * @param object the object referred to, or null if not found
     * @param property the property name from the reference
     * @param info contains template, line, column details
     * @return substitute return value for missing reference, or null if no substitute
     */
    public Object invalidGetMethod(Context context, String reference, 
            Object object, String property, Info info);

    /**
     * Called when object is null or there is no setter for the given 
     * property.  invalidSetMethod() will be called in sequence for
     * each link in the chain until a true value is returned.  It's
     * recommended that false be returned as a default to allow
     * for easy chaining.
     * 
     * @param context the context when the reference was found invalid
     * @param leftreference left reference being assigned to
     * @param rightreference invalid reference on the right
     * @param info contains info on template, line, col
     * 
     * @return if true then stop calling invalidSetMethod along the 
     * chain.
     */
    public boolean invalidSetMethod(Context context, String leftreference, 
            String rightreference, Info info);

    /**
     * Called when object is null or the given method does not exist.
     * invalidMethod() will be called in sequence for each link in 
     * the chain until the first non-null value is returned. 
     * 
     * @param context the context when the reference was found invalid
     * @param reference string with complete invalid reference
     * @param object the object referred to, or null if not found
     * @param method the name of the (non-existent) method
     * @param info contains template, line, column details
     * @return substitute return value for missing reference, or null if no substitute
     */
    public Object invalidMethod(Context context, String reference,  
            Object object, String method, Info info);
}
