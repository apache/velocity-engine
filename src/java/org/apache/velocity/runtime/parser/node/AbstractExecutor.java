package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2004 The Apache Software Foundation.
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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.velocity.runtime.RuntimeLogger;

/**
 * Abstract class that is used to execute an arbitrary
 * method that is in introspected. This is the superclass
 * for the GetExecutor and PropertyExecutor.
 *
 * @version $Id: AbstractExecutor.java,v 1.13 2004/02/27 18:43:17 dlr Exp $
 */
public abstract class AbstractExecutor
{
    protected RuntimeLogger rlog = null;
    
    /**
     * Method to be executed.
     */
    protected Method method = null;
    
    /**
     * Execute method against context.
     */
     public abstract Object execute(Object o)
         throws IllegalAccessException, InvocationTargetException;

    /**
     * Tell whether the executor is alive by looking
     * at the value of the method.
     */
    public boolean isAlive()
    {
        return (method != null);
    }

    public Method getMethod()
    {
        return method;
    }
}
