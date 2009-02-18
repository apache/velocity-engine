package org.apache.velocity.runtime.directive.contrib;

import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Foreach;
import org.apache.velocity.runtime.directive.MacroParseException;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;


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
