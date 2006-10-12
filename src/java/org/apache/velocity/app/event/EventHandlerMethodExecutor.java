package org.apache.velocity.app.event;

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
 * Strategy object used to execute event handler method.  Will be called
 * while looping through all the chained event handler implementations.
 * Each EventHandler method call should have a parallel executor object
 * defined.  
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public interface EventHandlerMethodExecutor
{
    /**
     * Execute the event handler method.  If Object is not null, do not 
     * iterate further through the handler chain.
     * If appropriate, the returned Object will be the return value.
     *  
     * @param handler call the appropriate method on this handler
     */
    public void execute(EventHandler handler) throws Exception;

    /**
     * Called after execute() to see if iterating should stop. Should
     * always return false before method execute() is run.
     * @return
     */
    public boolean isDone();

    /**
     * Get return value at end of all the iterations
     * @return null if no return value is required
     */
    public Object getReturnValue();
}
