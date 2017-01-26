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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.util.StringUtils;

import java.io.Writer;
import java.util.ArrayList;

/**
 * Break directive used for interrupting scopes.
 *
 * @author <a href="mailto:wyla@removethis.sci.fi">Jarkko Viinamaki</a>
 * @author Nathan Bubna
 * @version $Id$
 */
public class Break extends Directive
{
    private boolean scoped = false;

    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "break";
    }

    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    public int getType()
    {
        return LINE;
    }

    /**
     * Since there is no processing of content,
     * there is never a need for an internal scope.
     */
    public boolean isScopeProvided()
    {
        return false;
    }

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
    {
        super.init(rs, context, node);

        this.scoped = (node.jjtGetNumChildren() == 1);
    }

    /**
     * This directive throws a StopCommand which signals either
     * the nearest Scope or the specified scope to stop rendering
     * its content.
     * @return never, always throws a StopCommand or Exception
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
    {
        if (!scoped)
        {
            throw new StopCommand();
        }

        Object argument = node.jjtGetChild(0).value(context);
        if (argument instanceof Scope)
        {
            ((Scope)argument).stop();
            throw new IllegalStateException("Scope.stop() failed to throw a StopCommand");
        }
        else
        {
            throw new VelocityException(node.jjtGetChild(0).literal()+
                " is not a valid " + Scope.class.getName() + " instance at "
                + StringUtils.formatFileString(this));
        }
    }

    /**
     * Called by the parser to validate the argument types
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
        throws ParseException
    {
        if (argtypes.size() > 1)
        {
            throw new MacroParseException("The #break directive takes only a single, optional Scope argument",
               templateName, t);
        }
    }

}
