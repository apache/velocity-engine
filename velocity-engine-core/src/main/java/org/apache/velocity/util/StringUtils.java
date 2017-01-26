package org.apache.velocity.util;

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

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.util.introspection.Info;

import java.util.List;

/**
 * This class provides some methods for dynamically
 * invoking methods in objects, and some string
 * manipulation and formatting methods.
 *
 *  @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 *  @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 *  @version $Id$
 */
public class StringUtils
{
    /**
     * Line separator for the OS we are operating on.
     */
    private static final String EOL = System.getProperty("line.separator");

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     * @return String normalized path
     */
    public static final String normalizePath(String path)
    {
        // Normalize the slashes and add leading slash if necessary
        String normalized = path;
        if (normalized.indexOf('\\') >= 0)
        {
            normalized = normalized.replace('\\', '/');
        }

        if (!normalized.startsWith("/"))
        {
            normalized = "/" + normalized;
        }

        // Resolve occurrences of "//" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 1);
        }

        // Resolve occurrences of "%20" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("%20");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + " " +
            normalized.substring(index + 3);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
            normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
            normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);
    }

    /**
     * Trim all strings in a List.  Changes the strings in the existing list.
     * @param list
     * @return List of trimmed strings.
     * @since 1.5
     */
    public static List trimStrings(List list)
    {
        if (list == null)
            return null;

        int sz = list.size();
        for (int i = 0; i < sz; i++)
            list.set(i,nullTrim((String) list.get(i)));
        return list;
    }

    /**
     * Trim the string, but pass a null through.
     * @param s
     * @return List of trimmed Strings.
     * @since 1.5
     */
    public static String nullTrim(String s)
    {
        if (s == null)
        {
            return null;
        }
        else
        {
            return s.trim();
        }
    }

    /**
     * Creates a string that formats the template filename with line number
     * and column of the given Directive. We use this routine to provide a cosistent format for displaying
     * file errors.
     */
    public static final String formatFileString(Directive directive)
    {
        return formatFileString(directive.getTemplateName(), directive.getLine(), directive.getColumn());
    }

    /**
     * Creates a string that formats the template filename with line number
     * and column of the given Node. We use this routine to provide a cosistent format for displaying
     * file errors.
     */
    public static final String formatFileString(Node node)
    {
        return formatFileString(node.getTemplateName(), node.getLine(), node.getColumn());
    }

    /**
     * Simply creates a string that formats the template filename with line number
     * and column. We use this routine to provide a cosistent format for displaying
     * file errors.
     */
    public static final String formatFileString(Info info)
    {
          return formatFileString(info.getTemplateName(), info.getLine(), info.getColumn());
    }

    /**
     * Simply creates a string that formats the template filename with line number
     * and column. We use this routine to provide a cosistent format for displaying
     * file errors.
     * @param template File name of template, can be null
     * @param linenum Line number within the file
     * @param colnum Column number withing the file at linenum
     */
    public static final String formatFileString(String template, int linenum, int colnum)
    {
        StringBuilder buffer = new StringBuilder();
        if (template == null || template.equals(""))
        {
            template = "<unknown template>";
        }
        buffer.append(template).append("[line ").append(linenum).append(", column ").append(colnum).append(']');
        return buffer.toString();
    }
}
