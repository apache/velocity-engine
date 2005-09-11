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
 *  Event handler called when a method throws an exception.  This gives the
 *  application a chance to deal with it and either
 *  return something nice, or throw.
 *
 *  Please return what you want rendered into the output stream.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public interface MethodExceptionEventHandler extends EventHandler
{
    /**
     * Called when a method throws an exception.
     * Only the first registered MethodExceptionEventHandler is called.  If
     * none are registered a MethodInvocationException is thrown.
     *
     * @param claz the class of the object the method is being applied to
     * @param method the method
     * @param e the thrown exception
     * @return an object to insert in the page
     * @throws Exception an exception to be thrown instead inserting an object
     */
    public Object methodException( Class claz, String method, Exception e )
         throws Exception;
}
