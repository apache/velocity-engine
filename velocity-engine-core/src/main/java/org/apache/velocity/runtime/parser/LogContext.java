package org.apache.velocity.runtime.parser;

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

import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.introspection.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * <p>Track location in template files during rendering by populating the slf4j MDC tags <code>file</code>, <code>line</code> and <code>column</code>.</p>
 * <p>An MDC-aware logger can then use this info to display the template location in the message</p>
 * <p>For instance with webapp-slf4j-logger, it's enough to use <code>%file</code>, <code>%line</code> and <code>%column</code> in the logger format string.</p>
 * <p>Since this feature can have a performance impact, it has to be enabled in <code>velocity.properties</code> using:</p>
 * <pre><code>runtime.log.track_location = true</code></pre>
 * <p>(typically in a development environment)</p>
 *
 * @author Claude Brisson
 * @version $Id:$
 * @since 2.2
 */

public class LogContext
{
    protected static Logger logger = LoggerFactory.getLogger("rendering");

    public static final String MDC_FILE = "file";
    public static final String MDC_LINE = "line";
    public static final String MDC_COLUMN = "column";

    private boolean trackLocation;

    public LogContext(boolean trackLocation)
    {
        this.trackLocation = trackLocation;
    }

    private static ThreadLocal<Deque<StackElement>> contextStack = new ThreadLocal<Deque<StackElement>>()
    {
        @Override
        public Deque<StackElement> initialValue()
        {
            return new ArrayDeque<>();
        }
    };

    private static class StackElement
    {
        protected StackElement(SimpleNode src, Info info)
        {
            this.src = src;
            this.info = info;
        }

        protected SimpleNode src;
        protected int count = 1;
        protected Info info;
    }

    public void pushLogContext(SimpleNode src, Info info)
    {
        if (!trackLocation)
        {
            return;
        }
        Deque<StackElement> stack = contextStack.get();
        StackElement last = stack.peek();
        if (last != null && last.src == src)
        {
            ++last.count;
        }
        else
        {
            stack.push(new StackElement(src, info));
            setLogContext(info);
        }
    }

    public void popLogContext()
    {
        if (!trackLocation)
        {
            return;
        }
        Deque<StackElement> stack = contextStack.get();
        StackElement last = stack.peek();
        if (last == null)
        {
            logger.error("log context is already empty");
            return;
        }
        if (--last.count == 0)
        {
            stack.pop();
            last = stack.peek();
            if (last == null)
            {
                clearLogContext();
            }
            else
            {
                setLogContext(last.info);
            }
        }
    }

    private void setLogContext(Info info)
    {
        MDC.put(MDC_FILE, info.getTemplateName());
        MDC.put(MDC_LINE, String.valueOf(info.getLine()));
        MDC.put(MDC_COLUMN, String.valueOf(info.getColumn()));
    }

    private void clearLogContext()
    {
        MDC.remove(MDC_FILE);
        MDC.remove(MDC_LINE);
        MDC.remove(MDC_COLUMN);
    }

    private static final String STACKTRACE_LINE = "    %s at %s[line %d, column %d]";

    public String[] getStackTrace()
    {
        if (!trackLocation)
        {
            return null;
        }
        Deque<StackElement> stack = contextStack.get();
        List<String> levels = new ArrayList<>();
        for (StackElement level : stack)
        {
            String line = String.format(STACKTRACE_LINE,
                level.src.literal(),
                level.info.getTemplateName(),
                level.info.getLine(),
                level.info.getColumn());
            levels.add(line);
        }
        return levels.size() > 0 ? levels.toArray(new String[levels.size()]) : null;
    }
}
