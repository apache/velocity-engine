package org.apache.velocity.runtime.directive.contrib;

import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * This class implements the #return directive, used for returning from within
 * a #macro.  Specifying the #return directive inside a macro will end rendering
 * within the macro and resume rendering at the point of the macro call.
 */
public class Return extends Directive
{
  @Override
  public String getName()
  {
    return "return";
  }

  @Override
  public int getType()
  {
    return LINE;
  }
  
  public void init(RuntimeServices rs, InternalContextAdapter context, Node thisnode)
      throws TemplateInitException
  {
     super.init(rs, context, thisnode);
     
     // Make sure the #return directive is within a macro block.
     Node node = thisnode;
     while (! (node instanceof ASTDirective) 
         || ! ((ASTDirective)node).getDirectiveName().equals("macro"))
     {
         node = node.jjtGetParent();
         if (node == null)
         {
             // We are not in a macro definition, so throw an exception.
             throw new VelocityException("#return must be within a #macro block at " 
                 + Log.formatFileString(this));
         }
     }
  }  
  
  public boolean render(InternalContextAdapter context, Writer writer, Node node)
  {
      // Throw the ReturnThrowable to be caught at the macro call level.
      throw new ReturnThrowable();
  }

  /**
   * Implements a Throwable we can pass up the stack without disturbing
   * other catches of other throwables.
   * @see org.apache.velocity.runtime.directive.Stop
   */
  public static class ReturnThrowable extends Error
  {
  }
}
