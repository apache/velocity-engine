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

import java.io.Writer;
import org.apache.velocity.Template;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * This class implements the #stop directive which allows
 * a user to stop rendering the current execution context. The #stop directive
 * with no arguments will immediately stop rendering the current Template merge
 * or evaluate(...) call.
 * If the stop directive is called with a Scope argument, e.g.; #stop($foreach),
 * then rendering will end within that particular directive, but resume at 
 * the parent level.
 */
public class Stop extends Directive
{  
    private static final StopCommand STOP_ALL = new StopAllCommand();

    private boolean scopedStop = false;

    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "stop";
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

    public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
    {
        super.init(rs, context, node);

        int kids = node.jjtGetNumChildren();
        if (kids > 1)
        {  
            throw new VelocityException("The #stop directive only accepts a single scope object at "
                 + Log.formatFileString(this));
        }
        else
        {
            this.scopedStop = (kids == 1);
        }
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node)
    {
        if (!scopedStop)
        {
            // Only the top level calls that render an AST node tree catch and keep
            // this, thereby terminating at Template.merge or RuntimeInstance.evaluate.
            throw STOP_ALL;
        }

        Object argument = node.jjtGetChild(0).value(context);
        if (argument instanceof Scope)
        {
            ((Scope)argument).stop();
        }
        else
        {
            throw new VelocityException(node.jjtGetChild(0).literal()+" is not a valid Scope instance at "
                + Log.formatFileString(this));
        }
        return false;
    }

    /**
     * Specialized StopCommand that stops all merge or evaluate activity.
     */
    public static class StopAllCommand extends StopCommand
    {
        public StopAllCommand()
        {
            super("Template.merge or RuntimeInstance.evaluate");
        }

        public boolean isFor(Object that)
        {
            // only stop for the top :)
            return (that instanceof Template ||
                    that instanceof RuntimeInstance);
        }
    }
}

