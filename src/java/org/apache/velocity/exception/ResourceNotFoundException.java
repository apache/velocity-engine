package org.apache.velocity.exception;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 *  Application-level exception thrown when a resource of any type
 *  isn't found by the Velocity engine.
 *  <br>
 *  When this exception is thrown, a best effort will be made to have
 *  useful information in the exception's message.  For complete 
 *  information, consult the runtime log.
 *
 * @version $Id: ResourceNotFoundException.java,v 1.4 2004/02/27 18:43:15 dlr Exp $
 */
public class ResourceNotFoundException extends VelocityException
{
    public ResourceNotFoundException(String exceptionMessage)
    {
        super(exceptionMessage);
    }
}
