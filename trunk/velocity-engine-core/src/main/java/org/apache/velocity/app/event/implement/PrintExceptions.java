package org.apache.velocity.app.event.implement;

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

import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Simple event handler that renders method exceptions in the page
 * rather than throwing the exception.  Useful for debugging.
 *
 * <P>By default this event handler renders an error message containing the class and method which generated
 * the exception, the exception name and its message.
 *
 * To render the reference and the location in the tempkate, set the property <code>eventhandler.methodexception.templateinfo</code>
 * to <code>true</code>.
 *
 * To render the stack trace, set the property <code>eventhandler.methodexception.stacktrace</code>
 * to <code>true</code>.
 *
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id$
 * @since 1.5
 */
public class PrintExceptions implements MethodExceptionEventHandler, RuntimeServicesAware
{

    private static String SHOW_TEMPLATE_INFO = "eventhandler.methodexception.templateinfo";
    private static String SHOW_STACK_TRACE = "eventhandler.methodexception.stacktrace";

    /** Reference to the runtime service */
    private RuntimeServices rs = null;

    /**
     * Render the method exception, and optionally the exception message and stack trace.
     *
     * @param context current context
     * @param claz the class of the object the method is being applied to
     * @param method the method
     * @param e the thrown exception
     * @param info template name and line, column informations
     * @return an object to insert in the page
     */
    public Object methodException(Context context, Class claz, String method, Exception e, Info info)
    {
        boolean showTemplateInfo = rs.getBoolean(SHOW_TEMPLATE_INFO, false);
        boolean showStackTrace = rs.getBoolean(SHOW_STACK_TRACE,false);

        StringBuffer st = new StringBuffer();
        st.append("Exception while executing method ").append(claz.toString()).append(".").append(method);
        st.append(": ").append(e.getClass().getName()).append(": ").append(e.getMessage());

        if (showTemplateInfo)
        {
            st.append(" at ").append(info.getTemplateName()).append(" (line ").append(info.getLine()).append(", column ").append(info.getColumn()).append(")");
        }
        if (showStackTrace)
        {
            st.append("\n").append(getStackTrace(e));
        }
        return st.toString();

    }

    private static String getStackTrace(Throwable throwable)
    {
        PrintWriter printWriter = null;
        try
        {
            StringWriter stackTraceWriter = new StringWriter();
            printWriter = new PrintWriter(stackTraceWriter);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            return stackTraceWriter.toString();
        }
        finally
        {
            if (printWriter != null)
            {
                printWriter.close();
            }
        }
    }


    /**
     * @see org.apache.velocity.util.RuntimeServicesAware#setRuntimeServices(org.apache.velocity.runtime.RuntimeServices)
     */
    public void setRuntimeServices(RuntimeServices rs)
    {
        this.rs = rs;
    }

}
