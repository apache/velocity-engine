package org.apache.velocity.app.event;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Stores the event handlers. Event handlers can be assigned on a per
 * VelocityEngine instance basis by specifying the class names in the
 * velocity.properties file. Event handlers may also be assigned on a per-page
 * basis by creating a new instance of EventCartridge, adding the event
 * handlers, and then calling attachToContext. For clarity, it's recommended
 * that one approach or the other be followed, as the second method is primarily
 * presented for backwards compatibility.</p>
 * <p>Note that Event Handlers follow a filter pattern, with multiple event
 * handlers allowed for each event. When the appropriate event occurs, all the
 * appropriate event handlers are called in the sequence they were added to the
 * Event Cartridge. See the javadocs of the specific event handler interfaces
 * for more details.</p>
 *
 * @author <a href="mailto:wglass@wglass@forio.com">Will Glass-Husain </a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr. </a>
 * @author <a href="mailto:j_a_fernandez@yahoo.com">Jose Alberto Fernandez </a>
 * @version $Id$
 */
public class EventCartridge
{
    private List<ReferenceInsertionEventHandler> referenceHandlers = new ArrayList();
    private MethodExceptionEventHandler methodExceptionHandler = null;
    private List<IncludeEventHandler> includeHandlers = new ArrayList();
    private List<InvalidReferenceEventHandler> invalidReferenceHandlers = new ArrayList();

    /**
     * Ensure that handlers are not initialized more than once.
     */
    Set initializedHandlers = new HashSet();

    protected RuntimeServices rsvc = null;

    protected Logger getLog()
    {
        return rsvc == null ? LoggerFactory.getLogger(EventCartridge.class) : rsvc.getLog();
    }

    /**
     * runtime services setter, called during initialization
     *
     * @param rs runtime services
     * @since 2.0
     */
    public synchronized void setRuntimeServices(RuntimeServices rs)
    {
        if (rsvc == null)
        {
            rsvc = rs;
              /* allow for this method to be called *after* adding event handlers */
            for (EventHandler handler : referenceHandlers)
            {
                if (handler instanceof RuntimeServicesAware && !initializedHandlers.contains(handler))
                {
                    ((RuntimeServicesAware) handler).setRuntimeServices(rs);
                    initializedHandlers.add(handler);
                }
            }
            if (methodExceptionHandler != null &&
                methodExceptionHandler instanceof RuntimeServicesAware &&
                !initializedHandlers.contains(methodExceptionHandler))
            {
                ((RuntimeServicesAware) methodExceptionHandler).setRuntimeServices(rs);
                initializedHandlers.add(methodExceptionHandler);
            }
            for (EventHandler handler : includeHandlers)
            {
                if (handler instanceof RuntimeServicesAware && !initializedHandlers.contains(handler))
                {
                    ((RuntimeServicesAware) handler).setRuntimeServices(rs);
                    initializedHandlers.add(handler);
                }
            }
            for (EventHandler handler : invalidReferenceHandlers)
            {
                if (handler instanceof RuntimeServicesAware && !initializedHandlers.contains(handler))
                {
                    ((RuntimeServicesAware) handler).setRuntimeServices(rs);
                    initializedHandlers.add(handler);
                }
            }
        }
        else if (rsvc != rs)
        {
            throw new VelocityException("an event cartridge cannot be used by several different runtime services instances");
        }
    }

    /**
     * Adds an event handler(s) to the Cartridge.  This method
     * will find all possible event handler interfaces supported
     * by the passed in object.
     *
     * @param ev object implementing a valid EventHandler-derived interface
     * @return true if a supported interface, false otherwise or if null
     */
    public boolean addEventHandler(EventHandler ev)
    {
        if (ev == null)
        {
            return false;
        }

        boolean found = false;

        if (ev instanceof ReferenceInsertionEventHandler)
        {
            addReferenceInsertionEventHandler((ReferenceInsertionEventHandler) ev);
            found = true;
        }

        if (ev instanceof MethodExceptionEventHandler)
        {
            addMethodExceptionHandler((MethodExceptionEventHandler) ev);
            found = true;
        }

        if (ev instanceof IncludeEventHandler)
        {
            addIncludeEventHandler((IncludeEventHandler) ev);
            found = true;
        }

        if (ev instanceof InvalidReferenceEventHandler)
        {
            addInvalidReferenceEventHandler((InvalidReferenceEventHandler) ev);
            found = true;
        }

        if (found && rsvc != null && ev instanceof RuntimeServicesAware && !initializedHandlers.contains(ev))
        {
            ((RuntimeServicesAware) ev).setRuntimeServices(rsvc);
            initializedHandlers.add(ev);
        }

        return found;
    }

    /**
     * Add a reference insertion event handler to the Cartridge.
     *
     * @param ev ReferenceInsertionEventHandler
     * @since 1.5
     */
    public void addReferenceInsertionEventHandler(ReferenceInsertionEventHandler ev)
    {
        referenceHandlers.add(ev);
    }

    /**
     * Add a method exception event handler to the Cartridge.
     *
     * @param ev MethodExceptionEventHandler
     * @since 1.5
     */
    public void addMethodExceptionHandler(MethodExceptionEventHandler ev)
    {
        if (methodExceptionHandler == null)
        {
            methodExceptionHandler = ev;
        }
        else
        {
            getLog().warn("ignoring extra method exception handler");
        }
    }

    /**
     * Add an include event handler to the Cartridge.
     *
     * @param ev IncludeEventHandler
     * @since 1.5
     */
    public void addIncludeEventHandler(IncludeEventHandler ev)
    {
        includeHandlers.add(ev);
    }

    /**
     * Add an invalid reference event handler to the Cartridge.
     *
     * @param ev InvalidReferenceEventHandler
     * @since 1.5
     */
    public void addInvalidReferenceEventHandler(InvalidReferenceEventHandler ev)
    {
        invalidReferenceHandlers.add(ev);
    }


    /**
     * Removes an event handler(s) from the Cartridge. This method will find all
     * possible event handler interfaces supported by the passed in object and
     * remove them.
     *
     * @param ev object impementing a valid EventHandler-derived interface
     * @return true if event handler was previously registered, false if not
     * found
     */
    public boolean removeEventHandler(EventHandler ev)
    {
        if (ev == null)
        {
            return false;
        }

        if (ev instanceof ReferenceInsertionEventHandler)
        {
            return referenceHandlers.remove(ev);
        }

        if (ev instanceof MethodExceptionEventHandler)
        {
            if (ev == methodExceptionHandler)
            {
                methodExceptionHandler = null;
                return true;
            }
        }

        if (ev instanceof IncludeEventHandler)
        {
            return includeHandlers.remove(ev);
        }

        if (ev instanceof InvalidReferenceEventHandler)
        {
            return invalidReferenceHandlers.remove(ev);
        }

        return false;
    }

    /**
     * Call reference insertion handlers
     *
     * @return value returned by handlers
     * @since 2.0
     */
    public Object referenceInsert(InternalContextAdapter context, String reference, Object value)
    {
        for (ReferenceInsertionEventHandler handler : referenceHandlers)
        {
            value = handler.referenceInsert(context, reference, value);
        }
        return value;
    }

    /**
     * Check whether this event cartridge has a method exception event handler
     *
     * @return true if a method exception event handler has been registered
     * @since 2.0
     */
    boolean hasMethodExceptionEventHandler()
    {
        return methodExceptionHandler != null;
    }

    /**
     * Call method exception event handler
     *
     * @return value returned by handler
     * @since 2.0
     */
    public Object methodException(Context context, Class claz, String method, Exception e, Info info)
    {
        if (methodExceptionHandler != null)
        {
            return methodExceptionHandler.methodException(context, claz, method, e, info);
        }
        return null;
    }

    /**
     * Call include event handlers
     *
     * @return include path
     * @since 2.0
     */
    public String includeEvent(Context context, String includeResourcePath, String currentResourcePath, String directiveName)
    {
        for (IncludeEventHandler handler : includeHandlers)
        {
            includeResourcePath = handler.includeEvent(context, includeResourcePath, currentResourcePath, directiveName);
            /* reflect 1.x behavior: exit after at least one execution whenever a null include path has been found */
            if (includeResourcePath == null)
            {
                break;
            }
        }
        return includeResourcePath;
    }

    /**
     * Call invalid reference handlers for an invalid getter
     *
     * @return value returned by handlers
     * @since 2.0
     */
    public Object invalidGetMethod(Context context, String reference, Object object, String property, Info info)
    {
        Object result = null;
        for (InvalidReferenceEventHandler handler : invalidReferenceHandlers)
        {
            result = handler.invalidGetMethod(context, reference, object, property, info);
              /* reflect 1.x behavior: exit after at least one execution whenever a non-null value has been found */
            if (result != null)
            {
                break;
            }
        }
        return result;
    }

    /**
     * Call invalid reference handlers for an invalid setter
     *
     * @return whether to stop further chaining in the next cartridge
     * @since 2.0
     */
    public boolean invalidSetMethod(Context context, String leftreference, String rightreference, Info info)
    {
        for (InvalidReferenceEventHandler handler : invalidReferenceHandlers)
        {
            if (handler.invalidSetMethod(context, leftreference, rightreference, info))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Call invalid reference handlers for an invalid method call
     *
     * @return value returned by handlers
     * @since 2.0
     */
    public Object invalidMethod(Context context, String reference, Object object, String method, Info info)
    {
        Object result = null;
        for (InvalidReferenceEventHandler handler : invalidReferenceHandlers)
        {
            result = handler.invalidMethod(context, reference, object, method, info);
              /* reflect 1.x behavior: exit after at least one execution whenever a non-null value has been found */
            if (result != null)
            {
                break;
            }
        }
        return result;
    }

    /**
     * Attached the EventCartridge to the context
     *
     * Final because not something one should mess with lightly :)
     *
     * @param context context to attach to
     * @return true if successful, false otherwise
     */
    public final boolean attachToContext(Context context)
    {
        if (context instanceof InternalEventContext)
        {
            InternalEventContext iec = (InternalEventContext) context;

            iec.attachEventCartridge(this);

            /**
             * while it's tempting to call setContext on each handler from here,
             * this needs to be done before each method call.  This is
             * because the specific context will change as inner contexts
             * are linked in through macros, foreach, or directly by the user.
             */

            return true;
        }
        else
        {
            return false;
        }
    }
}
