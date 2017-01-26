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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.introspection.Info;


/**
 * Calls on request all registered event handlers for a particular event. Each
 * method accepts two event cartridges (typically one from the application and
 * one from the context). All appropriate event handlers are executed in order
 * until a stopping condition is met. See the docs for the individual methods to
 * see what the stopping condition is for that method.
 *
 * @author <a href="mailto:wglass@wglass@forio.com">Will Glass-Husain </a>
 * @version $Id$
 * @since 1.5
 */
public class EventHandlerUtil {


    /**
     * Called before a reference is inserted. All event handlers are called in
     * sequence. The default implementation inserts the reference as is.
     *
     * This is a major hotspot method called by ASTReference render.
     *
     * @param reference reference from template about to be inserted
     * @param value value about to be inserted (after toString() )
     * @param rsvc current instance of RuntimeServices
     * @param context The internal context adapter.
     * @return Object on which toString() should be called for output.
     */
    public static Object referenceInsert(RuntimeServices rsvc,
            InternalContextAdapter context, String reference, Object value)
    {
        try
        {
            value = rsvc.getApplicationEventCartridge().referenceInsert(context, reference, value);
            EventCartridge contextCartridge = context.getEventCartridge();
            if (contextCartridge != null)
            {
                contextCartridge.setRuntimeServices(rsvc);
                value = contextCartridge.referenceInsert(context, reference, value);
            }
            return value;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new VelocityException("Exception in event handler.",e);
        }
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
     * @param rsvc current instance of RuntimeServices
     * @param context The internal context adapter.
     * @return Object to return as method result
     * @throws Exception
     *             to be wrapped and propagated to app
     */
    public static Object methodException(RuntimeServices rsvc,
            InternalContextAdapter context, Class claz, String method,
            Exception e, Info info) throws Exception
    {
        try
        {
            EventCartridge ev = rsvc.getApplicationEventCartridge();
            if (ev.hasMethodExceptionEventHandler())
            {
                return ev.methodException(context, claz, method, e, info);
            }
            EventCartridge contextCartridge = context.getEventCartridge();
            if (contextCartridge != null)
            {
                contextCartridge.setRuntimeServices(rsvc);
                return contextCartridge.methodException(context, claz, method, e, info);
            }
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception ex)
        {
            throw new VelocityException("Exception in event handler.", ex);
        }

        /* default behaviour is to re-throw exception */
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
     * @param rsvc current instance of RuntimeServices
     * @param context The internal context adapter.
     *
     * @return a new resource path for the directive, or null to block the
     *         include from occurring.
     */
    public static String includeEvent(RuntimeServices rsvc,
            InternalContextAdapter context, String includeResourcePath,
            String currentResourcePath, String directiveName)
    {
        try
        {
            includeResourcePath = rsvc.getApplicationEventCartridge().includeEvent(context, includeResourcePath, currentResourcePath, directiveName);
            EventCartridge contextCartridge = context.getEventCartridge();
            if (contextCartridge != null)
            {
                contextCartridge.setRuntimeServices(rsvc);
                includeResourcePath = contextCartridge.includeEvent(context, includeResourcePath, currentResourcePath, directiveName);
            }
            return includeResourcePath;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new VelocityException("Exception in event handler.",e);
        }
    }


    /**
     * Called when an invalid get method is encountered.
     *
     * @param rsvc current instance of RuntimeServices
     * @param context the context when the reference was found invalid
     * @param reference complete invalid reference
     * @param object object from reference, or null if not available
     * @param property name of property, or null if not relevant
     * @param info contains info on template, line, col
     * @return substitute return value for missing reference, or null if no substitute
     */
    public static Object invalidGetMethod(RuntimeServices rsvc,
            InternalContextAdapter context, String reference,
            Object object, String property, Info info)
    {
        try
        {
            Object result = rsvc.getApplicationEventCartridge().invalidGetMethod(context, reference, object, property, info);
            EventCartridge contextCartridge = context.getEventCartridge();
            if (contextCartridge != null)
            {
                contextCartridge.setRuntimeServices(rsvc);
                result = contextCartridge.invalidGetMethod(context, reference, object, property, info);
            }
            return result;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new VelocityException("Exception in event handler.",e);
        }
    }

   /**
     * Called when an invalid set method is encountered.
     *
     * @param rsvc current instance of RuntimeServices
     * @param context the context when the reference was found invalid
     * @param leftreference left reference being assigned to
     * @param rightreference invalid reference on the right
     * @param info contains info on template, line, col
     */
    public static void invalidSetMethod(RuntimeServices rsvc,
            InternalContextAdapter context, String leftreference,
            String rightreference, Info info)
    {
        try
        {
            if (!rsvc.getApplicationEventCartridge().invalidSetMethod(context, leftreference, rightreference, info))
            {
                EventCartridge contextCartridge = context.getEventCartridge();
                if (contextCartridge != null)
                {
                    contextCartridge.setRuntimeServices(rsvc);
                    contextCartridge.invalidSetMethod(context, leftreference, rightreference, info);
                }
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new VelocityException("Exception in event handler.",e);
        }
    }

    /**
     * Called when an invalid method is encountered.
     *
     * @param rsvc current instance of RuntimeServices
     * @param context the context when the reference was found invalid
     * @param reference complete invalid reference
     * @param object object from reference, or null if not available
     * @param method name of method, or null if not relevant
     * @param info contains info on template, line, col
     * @return substitute return value for missing reference, or null if no substitute
     */
    public static Object invalidMethod(RuntimeServices rsvc,
            InternalContextAdapter context,  String reference,
            Object object, String method, Info info)
    {
        try
        {
            Object result = rsvc.getApplicationEventCartridge().invalidMethod(context, reference, object, method, info);
            EventCartridge contextCartridge = context.getEventCartridge();
            if (contextCartridge != null)
            {
                contextCartridge.setRuntimeServices(rsvc);
                result = contextCartridge.invalidMethod(context, reference, object, method, info);
            }
            return result;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new VelocityException("Exception in event handler.",e);
        }
    }
}
