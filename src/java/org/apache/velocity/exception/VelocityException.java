package org.apache.velocity.exception;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

/**
 *  Base class for Velocity exceptions thrown to the 
 *  application layer.
 *
 * @author <a href="mailto:kdowney@amberarcher.com">Kyle F. Downey</a>
 * @version $Id: VelocityException.java,v 1.2.14.1 2004/03/03 23:22:54 geirm Exp $
 */
public class VelocityException extends Exception
{
    public VelocityException(String exceptionMessage )
    {
        super(exceptionMessage);
    }       
}
