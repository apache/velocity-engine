package org.apache.velocity.app.event.implement;

import org.apache.commons.lang3.StringEscapeUtils;

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

/**
 * <p>Escapes the characters in a String to be suitable for use in JavaScript.</p>
 * <p>Warning: escaping references this way, without knowing if they land inside or outside Javascript simple-quoted or double-quoted strings, is not usable in production.</p>
 *
 * @see <a href="http://commons.apache.org/proper/commons-lang/javadocs/api-release/org/apache/commons/lang3/StringEscapeUtils.html#escapeEcmaScript%28java.lang.String%29">StringEscapeUtils</a>
 * @author wglass
 * @since 1.5
 * @deprecated impractical use
 */
@Deprecated
public class EscapeJavaScriptReference extends EscapeReference
{

    /**
     * Escapes the characters in a String to be suitable for use in JavaScript.
     *
     * @param text
     * @return An escaped String.
     * @see <a href="http://commons.apache.org/proper/commons-lang/javadocs/api-release/org/apache/commons/lang3/StringEscapeUtils.html#escapeEcmaScript%28java.lang.String%29">StringEscapeUtils</a>
     */
    @Override
    protected String escape(Object text)
    {
        return StringEscapeUtils.escapeEcmaScript(text.toString());
    }

    /**
     * @return attribute "eventhandler.escape.javascript.match"
     */
    @Override
    protected String getMatchAttribute()
    {
        return "eventhandler.escape.javascript.match";
    }

}
