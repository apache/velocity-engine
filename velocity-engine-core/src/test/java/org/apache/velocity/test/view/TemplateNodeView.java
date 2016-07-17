package org.apache.velocity.test.view;

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
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.NodeViewMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Simple class for dumping the AST for a template.
 * Good for debugging and writing new directives.
 */
public class TemplateNodeView
{
    /**
     * Root of the AST node structure that results from
     * parsing a template.
     */
    private SimpleNode document;

    /**
     * Visitor used to traverse the AST node structure
     * and produce a visual representation of the
     * node structure. Very good for debugging and
     * writing new directives.
     */
    private NodeViewMode visitor;

    /**
     * Default constructor: sets up the Velocity
     * Runtime, creates the visitor for traversing
     * the node structure and then produces the
     * visual representation by the visitation.
     */
    public TemplateNodeView(String templateFile)
    {
        try
        {
            RuntimeSingleton.init("velocity.properties");

            InputStreamReader isr = new InputStreamReader(
                                       new FileInputStream(templateFile),
                                       RuntimeSingleton.getString(RuntimeSingleton.INPUT_ENCODING));

            BufferedReader br = new BufferedReader( isr );

            Template tmpl = new Template();
            tmpl.setName(templateFile);
            document = RuntimeSingleton.parse( br, tmpl);

            visitor = new NodeViewMode();
            visitor.setContext(null);
            visitor.setWriter(new PrintWriter(System.out));
            document.jjtAccept(visitor, null);
        }
        catch (Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
