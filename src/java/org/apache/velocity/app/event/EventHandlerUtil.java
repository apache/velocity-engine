package org.apache.velocity.app.event;

import java.util.Iterator;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;

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
 * Calls on request all registered event handlers for a particular event. Each
 * method accepts two event cartridges (typically one from the application and
 * one from the context). All appropriate event handlers are executed in order
 * until a stopping condition is met. See the docs for the individual methods to
 * see what the stopping condition is for that method.
 *
 * @author <a href="mailto:wglass@wglass@forio.com">Will Glass-Husain </a>
 * @version $Id$
 */
public class EventHandlerUtil {

    /**
     * Called before a reference is inserted. All event handlers are called in
     * sequence. The default implementation inserts the reference as is.
     *
     * @param reference reference from template about to be inserted
     * @param value value about to be inserted (after toString() )
     * @return Object on which toString() should be called for output.
     */
    public static Object referenceInsert(RuntimeServices rsvc,
            InternalContextAdapter context, String reference, Object value)
    {
        EventCartridge ev1 = rsvc.getApplicationEventCartridge();

        /**
         * retrieve and initialize the event cartridge handlers attached to the
         * context (if they have not been already)
         */
        EventCartridge ev2 = context.getEventCartridge();

        if (ev2 != null)
        {
            try {
                ev2.initialize(rsvc);
            }
            catch (Exception E)
            {
                rsvc.error("Couldn't initialize event handler.  " + E);
            }
        }

        Object returnValue = value;

        if (ev1 != null)
            for (Iterator i = ev1.getReferenceInsertionEventHandlers(); i.hasNext();)
            {
                ReferenceInsertionEventHandler eh = (ReferenceInsertionEventHandler) i.next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                returnValue = eh.referenceInsert(reference, returnValue);
            }

        if (ev2 != null)
            for (Iterator i = ev2.getReferenceInsertionEventHandlers(); i
                    .hasNext();)
            {
                ReferenceInsertionEventHandler eh = (ReferenceInsertionEventHandler) i.next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                returnValue = eh.referenceInsert(reference, returnValue);
            }

        return returnValue;
    }

    /**
     * Called when a null is evaluated during a #set. All event handlers are
     * called in sequence until a false is returned. The default implementation
     * always returns true.
     *
     * @param reference
     *            reference from template about to be inserted
     * @return true if to be logged, false otherwise
     */
    public static boolean shouldLogOnNullSet(RuntimeServices rsvc,
            InternalContextAdapter context, String lhs, String rhs)
    {
        EventCartridge ev1 = rsvc.getApplicationEventCartridge();

        /**
         * retrieve and initialize the event cartridge handlers attached to the
         * context (if they have not been already)
         */
        EventCartridge ev2 = context.getEventCartridge();

        if (ev2 != null)
        {
            try
            {
                ev2.initialize(rsvc);
            }
            catch (Exception E)
            {
                rsvc.error("Couldn't initialize event handler.  " + E);
            }
        }

        boolean returnValue = true;

        if (ev1 != null)
            for (Iterator i = ev1.getNullSetEventHandlers(); i.hasNext();)
            {
                NullSetEventHandler eh = (NullSetEventHandler) i.next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                returnValue = returnValue
                        && eh.shouldLogOnNullSet(lhs, rhs);
            }

        if (ev2 != null)
            for (Iterator i = ev2.getNullSetEventHandlers(); i.hasNext();)
            {
                NullSetEventHandler eh = (NullSetEventHandler) i.next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                returnValue = returnValue
                        && eh.shouldLogOnNullSet(lhs, rhs);
            }

        return returnValue;
    }

    /**
     * Called when a method exception is generated during Velocity merge. Only
     * the first valid event handler in the sequence is called. The default
     * implementation simply rethrows the exception.
     *
     * @param claz
     *            Class that is causing the exception
     * @param method
     *            method called that causes the exception
     * @param e
     *            Exception thrown by the method
     * @return Object to return as method result
     * @throws exception
     *             to be wrapped and propogated to app
     */
    public static Object methodException(RuntimeServices rsvc,
            InternalContextAdapter context, Class claz, String method,
            Exception e) throws Exception {
        EventCartridge ev1 = rsvc.getApplicationEventCartridge();

        /**
         * retrieve and initialize the event cartridge handlers attached to the
         * context (if they have not been already)
         */
        EventCartridge ev2 = context.getEventCartridge();

        if (ev2 != null)
        {
            try
            {
                ev2.initialize(rsvc);
            }
            catch (Exception E)
            {
                rsvc.error("Couldn't initialize event handler.  " + E);
            }
        }

        if (ev1 != null)
            for (Iterator i = ev1.getMethodExceptionEventHandlers(); i
                    .hasNext();)
            {
                MethodExceptionEventHandler eh = (MethodExceptionEventHandler) i
                        .next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                return eh.methodException(claz, method, e);
            }

        if (ev2 != null)
            for (Iterator i = ev2.getMethodExceptionEventHandlers(); i
                    .hasNext();)
            {
                MethodExceptionEventHandler eh = (MethodExceptionEventHandler) i
                        .next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                return eh.methodException(claz, method, e);
            }

        throw e;
    }

    /**
     * Called when an include-type directive is encountered (#include or
     * #parse). All the registered event handlers are called unless null is
     * returned. The default implementation always processes the included
     * resource.
     *
     * @param includeResourcePath
     *            the path as given in the include directive.
     * @param currentResourcePath
     *            the path of the currently rendering template that includes the
     *            include directive.
     * @param directiveName
     *            name of the directive used to include the resource. (With the
     *            standard directives this is either "parse" or "include").
     *
     * @return a new resource path for the directive, or null to block the
     *         include from occurring.
     */
    public static String includeEvent(RuntimeServices rsvc,
            InternalContextAdapter context, String includeResourcePath,
            String currentResourcePath, String directiveName)
    {
        EventCartridge ev1 = rsvc.getApplicationEventCartridge();

        /**
         * retrieve and initialize the event cartridge handlers attached to the
         * context (if they have not been already)
         */
        EventCartridge ev2 = context.getEventCartridge();

        if (ev2 != null)
        {
            try
            {
                ev2.initialize(rsvc);
            }
            catch (Exception E)
            {
                rsvc.error("Couldn't initialize event handler.  " + E);
            }
        }

        String returnValue = includeResourcePath;

        if (ev1 != null)
            for (Iterator i = ev1.getIncludeEventHandlers(); i.hasNext();)
            {
                IncludeEventHandler eh = (IncludeEventHandler) i.next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                returnValue = eh.includeEvent(returnValue, currentResourcePath, directiveName);
            }

        if (ev2 != null)
            for (Iterator i = ev2.getIncludeEventHandlers(); i.hasNext();)
            {
                IncludeEventHandler eh = (IncludeEventHandler) i.next();
                if (eh instanceof ContextAware)
                    ((ContextAware) eh).setContext(context);
                returnValue = eh.includeEvent(returnValue, currentResourcePath, directiveName);
            }

        return returnValue;
    }

}