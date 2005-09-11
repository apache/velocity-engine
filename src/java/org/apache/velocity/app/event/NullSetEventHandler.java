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
 *  Event handler called when the RHS of #set is null.  Lets an app approve / veto
 *  writing a log message based on the specific reference.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public interface NullSetEventHandler extends EventHandler
{
    /**
     * Called when the RHS of a #set() is null, which will result
     * in a null LHS. All NullSetEventHandlers
     * are called in sequence until a false is returned.  If no NullSetEventHandler
     * is registered all nulls will be logged.
     *
     *  @param lhs  reference literal of left-hand-side of set statement
     *  @param rhs  reference literal of right-hand-side of set statement
     *  @return true if log message should be written, false otherwise
     */
    public boolean shouldLogOnNullSet( String lhs, String rhs );
}
