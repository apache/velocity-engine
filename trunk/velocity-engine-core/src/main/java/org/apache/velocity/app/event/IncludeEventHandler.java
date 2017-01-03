package org.apache.velocity.app.event;

import org.apache.velocity.context.Context;
import org.apache.velocity.util.ContextAware;

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
 *  Event handler for include type directives (e.g. <code>#include()</code>, <code>#parse()</code>)
 *  Allows the developer to modify the path of the resource returned.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 * @since 1.5
 */
public interface  IncludeEventHandler extends EventHandler
{
    /**
     * Called when an include-type directive is encountered (
     * <code>#include</code> or <code>#parse</code>). May modify the path
     * of the resource to be included or may block the include entirely. All the
     * registered IncludeEventHandlers are called unless null is returned. If
     * none are registered the template at the includeResourcePath is retrieved.
     *
     * @param context current context
     * @param includeResourcePath  the path as given in the include directive.
     * @param currentResourcePath the path of the currently rendering template that includes the
     *            include directive.
     * @param directiveName  name of the directive used to include the resource. (With the
     *            standard directives this is either "parse" or "include").
     *
     * @return a new resource path for the directive, or null to block the
     *         include from occurring.
     */
    public String includeEvent(Context context, String includeResourcePath, String currentResourcePath, String directiveName);
}
