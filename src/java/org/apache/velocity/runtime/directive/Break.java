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
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Break directive used for interrupting foreach loops.
 *
 * @author <a href="mailto:wyla@removethis.sci.fi">Jarkko Viinamaki</a>
 * @version $Id$
 */
public class Break extends Directive
{
    private static final RuntimeException BREAK = new BreakException();
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
     * Break directive does not actually do any rendering. 
     * 
     * This directive throws a BreakException (RuntimeException) which
     * signals foreach directive to break out of the loop. Note that this
     * directive does not verify that it is being called inside a foreach
     * loop.
     * @return true if the directive rendered successfully.
     */
    public boolean render(InternalContextAdapter context,
                           Writer writer, Node node)
        throws IOException,  MethodInvocationException, ResourceNotFoundException,
        	ParseErrorException
    {
        throw BREAK;
    }
    
    public static class BreakException extends RuntimeException 
    {
        public BreakException()
        {
          // If a break is thrown during a macro or parse call, then this exception
          // will be logged because this method calls catch
          // RuntimeException, so provide the user with some info.
          super("Break");
        }
    }
    
    /**
     * Called by the parser to validate the argument types
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
      throws ParseException
    {
        if (argtypes.size() != 0)
        {
            throw new MacroParseException("The #break directive does not take any arguments",
               templateName, t);
        }
    }    
    
}
