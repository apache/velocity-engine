package org.apache.velocity.app.event;
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
 *  Reference 'Stream insertion' event handler.  Called with object
 *  that will be inserted into stream via value.toString().
 *
 *  Please return an Object that will toString() nicely :)
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ReferenceInsertionEventHandler.java,v 1.2.8.1 2004/03/03 23:22:53 geirm Exp $
 */
public interface  ReferenceInsertionEventHandler extends EventHandler
{
    /**
     * A call-back which is executed during Velocity merge before a
     * reference value is inserted into the output stream.
     *
     * @param reference Reference from template about to be inserted.
     * @param value Value about to be inserted (after its
     * <code>toString()</code> method is called).
     * @return Object on which <code>toString()</code> should be
     * called for output.
     */
    public Object referenceInsert( String reference, Object value  );
}
