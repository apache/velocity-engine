package org.apache.velocity.runtime.directive;

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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;


/**
 * Base class for all directives used in Velocity.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class Directive implements DirectiveConstants, Cloneable
{
    private int line = 0;
    private int column = 0;
    private String templateName;

    /**
     *
     */
    protected RuntimeServices rsvc = null;

    /**
     * Return the name of this directive.
     * @return The name of this directive.
     */
    public abstract String getName();

    /**
     * Get the directive type BLOCK/LINE.
     * @return The directive type BLOCK/LINE.
     */
    public abstract int getType();

    /**
     * Allows the template location to be set.
     * @param line
     * @param column
     */
    public void setLocation( int line, int column )
    {
        this.line = line;
        this.column = column;
    }

    /**
     * Allows the template location to be set.
     * @param line
     * @param column
     */
    public void setLocation(int line, int column, String templateName)
    {
        setLocation(line, column);
        this.templateName = templateName;
    }

    /**
     * for log msg purposes
     * @return The current line for log msg purposes.
     */
    public int getLine()
    {
        return line;
    }

    /**
     * for log msg purposes
     * @return The current column for log msg purposes.
     */
    public int getColumn()
    {
        return column;
    }
    
    /**
     * @return The template file name this directive was defined in, or null if not 
     * defined in a file.
     */
    public String getTemplateName()
    {
      return templateName;
    }

    /**
     * How this directive is to be initialized.
     * @param rs
     * @param context
     * @param node
     * @throws TemplateInitException
     */
    public void init( RuntimeServices rs, InternalContextAdapter context,
                      Node node)
        throws TemplateInitException
    {
        rsvc = rs;

        //        int i, k = node.jjtGetNumChildren();

        //for (i = 0; i < k; i++)
        //    node.jjtGetChild(i).init(context, rs);
    }

    /**
     * The Parser calls this method during template parsing to check the arguments
     * types.  Be aware that this method is called pre init, so not all data
     * is available in this method.  The default implementation does not peform any
     * checking.  We do this so that Custom directives do not trigger any parse 
     * errors in IDEs.
     * @param argtypes type, Array of argument types of each argument to the directive
     * for example ParserTreeConstants.JJTWORD
     * @param t token of directive
     * @param templateName the name of the template this directive is referenced in.
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
        throws ParseException
    {      
    }
    
    /**
     * How this directive is to be rendered
     * @param context
     * @param writer
     * @param node
     * @return True if the directive rendered successfully.
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     */
    public abstract boolean render( InternalContextAdapter context,
                                    Writer writer, Node node )
           throws IOException, ResourceNotFoundException, ParseErrorException,
                MethodInvocationException;
}
