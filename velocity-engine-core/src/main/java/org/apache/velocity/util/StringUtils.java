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
     * Creates a string that formats the template filename with line number
     * and column of the given Directive. We use this routine to provide a consistent format for displaying
     * file errors.
     */
    public static String formatFileString(Directive directive)
    {
        return formatFileString(directive.getTemplateName(), directive.getLine(), directive.getColumn());
    }

    /**
     * Creates a string that formats the template filename with line number
     * and column of the given Node. We use this routine to provide a consistent format for displaying
     * file errors.
     */
    public static String formatFileString(Node node)
    {
        return formatFileString(node.getTemplateName(), node.getLine(), node.getColumn());
    }

    /**
     * Simply creates a string that formats the template filename with line number
     * and column. We use this routine to provide a consistent format for displaying
     * file errors.
     */
    public static String formatFileString(Info info)
    {
          return formatFileString(info.getTemplateName(), info.getLine(), info.getColumn());
    }

    /**
     * Simply creates a string that formats the template filename with line number
     * and column. We use this routine to provide a consistent format for displaying
     * file errors.
     * @param template File name of template, can be null
     * @param linenum Line number within the file
     * @param colnum Column number withing the file at linenum
     */
    public static String formatFileString(String template, int linenum, int colnum)
    {
        StringBuilder buffer = new StringBuilder();
        if (org.apache.commons.lang3.StringUtils.isEmpty(template))
        {
            template = "<unknown template>";
        }
        buffer.append(template).append("[line ").append(linenum).append(", column ").append(colnum).append(']');
        return buffer.toString();
    }
}
