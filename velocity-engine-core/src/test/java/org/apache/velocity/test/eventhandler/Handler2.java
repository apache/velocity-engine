package org.apache.velocity.test.eventhandler;

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

import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

/**
 * This is a test set of event handlers, used to test event handler sequences.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class Handler2
    implements ReferenceInsertionEventHandler, MethodExceptionEventHandler, IncludeEventHandler {

    /**
     * convert output to upper case
     */
    public Object referenceInsert(Context context, String reference, Object value)
    {
        if (value == null)
            return null;
        else
            return value.toString().toUpperCase();
    }

    /**
     * print the exception
     */
    public Object methodException(Context context, Class claz, String method, Exception e, Info info)
    {
        return "Exception: " + e;
    }

    /*
     * redirect all requests to a new directory "subdir" (simulates localization).
     */
    public String includeEvent(
        Context context,
        String includeResourcePath,
        String currentResourcePath,
        String directiveName)
    {

        return "subdir/" + includeResourcePath;

    }

}
