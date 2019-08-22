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

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import java.io.Reader;

public interface Parser
{
    RuntimeServices getRuntimeServices();
    SimpleNode parse(Reader reader, Template template) throws ParseException;
    void resetCurrentTemplate();
    Template getCurrentTemplate();
    Token getToken(int index);
    boolean isDirective(String macro);
    Directive getDirective(String directive);
    void ReInit(CharStream stream);

    char dollar();
    char hash();
    char at();
    char asterisk();

    default String lineComment()
    {
        return String.valueOf(hash()) + hash();
    }

    default String blockComment()
    {
        return String.valueOf(hash()) + asterisk();
    }
}
