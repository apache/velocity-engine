/**
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
package org.apache.velocity.runtime.directive.contrib;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Foreach;
import org.apache.velocity.runtime.directive.MacroParseException;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.ParserTreeConstants;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * The #for directive provides the behavior of the #foreach directive but also
 * provides an 'index' keyword that allows the user to define an optional index variable
 * that tracks the loop iterations. e.g.; #for($user in $users index $i).
 * As $user iterates through $users the index reference $i will be equal to
 * 0, 1, 2, etc..
 * @see org.apache.velocity.runtime.directive.Foreach
 */
public class For extends Foreach
{
  protected String counterName;
  protected int counterInitialValue;

  public String getName()
  {
    return "for";
  }

  public int getType()
  {
    return BLOCK;
  }

  public void init(RuntimeServices rs, InternalContextAdapter context, Node node)
      throws TemplateInitException
  {
    super.init(rs, context, node);
    // If we have more then 3 argument then the user has specified an
    // index value, i.e.; #foreach($a in $b index $c)
    if (node.jjtGetNumChildren() > 4)
    {
        // The index variable name is at position 4
        counterName = ((ASTReference) node.jjtGetChild(4)).getRootString();
        // The count value always starts at 0 when using an index.
        counterInitialValue = 0;
    }
  }

  @Override
  public boolean render(InternalContextAdapter context, Writer writer, Node node)
    throws IOException
  {
    Object c = context.get(counterName);
    context.put(counterName, counterInitialValue);
    try
    {
      return super.render(context, writer, node);
    }
    finally
    {
      if (c != null)
      {
        context.put(counterName, c);
      }
      else
      {
        context.remove(counterName);
      }
    }
  }

  @Override
  protected void renderBlock(InternalContextAdapter context, Writer writer, Node node)
    throws IOException
  {
    Object count = context.get(counterName);
    if (count instanceof Number)
    {
      context.put(counterName, ((Number)count).intValue() + 1);
    }
    super.renderBlock(context, writer, node);
  }

  /**
   * We do not allow a word token in any other arg position except for the 2nd
   * since we are looking for the pattern #foreach($foo in $bar).
   */
  public void checkArgs(ArrayList<Integer> argtypes, Token t,
      String templateName) throws ParseException
  {
    super.checkArgs(argtypes, t, templateName);

    // If #foreach is defining an index variable make sure it has the 'index
    // $var' combo.
    if (argtypes.size() > 3)
    {
      if (argtypes.get(3) != ParserTreeConstants.JJTWORD)
      {
        throw new MacroParseException(
            "Expected word 'index' at argument position 4 in #foreach",
            templateName, t);
      }
      else if (argtypes.size() == 4
          || argtypes.get(4) != ParserTreeConstants.JJTREFERENCE)
      {
        throw new MacroParseException(
            "Expected a reference after 'index' in #foreach", templateName, t);
      }
    }
  }
}
