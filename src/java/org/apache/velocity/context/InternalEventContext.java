

package org.apache.velocity.context;

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

import org.apache.velocity.app.event.EventCartridge;

/**
 *  Interface for event support.  Note that this is a public internal
 *  interface, as it is something that will be accessed from outside 
 *  of the .context package.
 *
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @version $Id: InternalEventContext.java,v 1.2.12.1 2004/03/03 23:22:54 geirm Exp $
 */
public interface InternalEventContext
{
    public EventCartridge attachEventCartridge( EventCartridge ec);
    public EventCartridge getEventCartridge();
}
