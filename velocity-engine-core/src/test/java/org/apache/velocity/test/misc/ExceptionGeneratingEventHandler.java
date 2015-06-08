package org.apache.velocity.test.misc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

/**
 * Event handlers that always throws an exception.  Used to test
 * that RuntimeExceptions are passed through.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class ExceptionGeneratingEventHandler
    implements IncludeEventHandler, MethodExceptionEventHandler, ReferenceInsertionEventHandler
{

    public String includeEvent( String includeResourcePath, String currentResourcePath, String directiveName )
    {
        throw new RuntimeException( "exception" );
    }

    public Object methodException( Class<?> claz, String method, Exception e )
    {
        throw new RuntimeException( "exception" );
    }

    public Object referenceInsert( String reference, Object value )
    {
        throw new RuntimeException( "exception" );
    }

}
