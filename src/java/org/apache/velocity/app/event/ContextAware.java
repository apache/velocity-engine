package org.apache.velocity.app.event;

import org.apache.velocity.context.Context;

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
 * Event handlers implementing this interface will automatically
 * have the method setContext called before each event.  This
 * allows the event handler to use information in the latest context
 * when responding to the event.
 * 
 * <P>Important Note: Only local event handlers attached to the context
 * (as opposed to global event handlers initialized in the velocity.properties
 * file) should implement ContextAware.  Since global event handlers are
 * singletons individual requests will not be able to count on the 
 * correct context being loaded before a request.
 *
 * @author <a href="mailto:wglass@wglass@forio.com">Will Glass-Husain</a>
 * @version $Id: GlobalEventHandler.java,v 1.4 2004/03/19 17:13:33 dlr Exp $
 */
public interface  ContextAware
{
    /**
     * Initialize the EventHandler.
     */
    public void setContext( Context context );

}
