
package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.Writer;
import java.io.IOException;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Pluggable directive that handles the #include() statement in VTL. 
 * This #include() can take multiple arguments of either 
 * StringLiteral or Reference.
 *
 * Notes:
 * -----
 *  1) The included source material can only come from somewhere in 
 *    the TemplateRoot tree for security reasons. There is no way 
 *    around this.  If you want to include content from elsewhere on
 *    your disk, use a link from somwhere under Template Root to that 
 *    content.
 *
 *  2) By default, there is no output to the render stream in the event of
 *    a problem.  You can override this behavior with two property values :
 *       include.output.errormsg.start
 *       include.output.errormsg.end
 *     If both are defined in velocity.properties, they will be used to
 *     in the render output to bracket the arg string that caused the 
 *     problem.
 *     Ex. : if you are working in html then
 *       include.output.errormsg.start=<!-- #include error :
 *       include.output.errormsg.end= -->
 *     might be an excellent way to start...
 *
 *  3) As noted above, #include() can take multiple arguments.
 *    Ex : #include( "foo.vm" "bar.vm" $foo )
 *    will simply include all three if valid to output w/o any
 *    special separator.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:kav@kav.dk">Kasper Nielsen</a>
 * @version $Id: Include.java,v 1.22 2001/09/07 18:11:22 geirm Exp $
 */
public class Include extends Directive
{
    private String outputMsgStart = "";
    private String outputMsgEnd = "";

    /**
     * Return name of this directive.
     */
    public String getName()
    {
        return "include";
    }        
    
    /**
     * Return type of this directive.
     */
    public int getType()
    {
        return LINE;
    }        

    /**
     *  simple init - init the tree and get the elementKey from
     *  the AST
     */
    public void init( RuntimeServices rs, InternalContextAdapter context, Node node) 
        throws Exception
    {
        super.init( rs, context, node );

        /*
         *  get the msg, and add the space so we don't have to
         *  do it each time
         */
        outputMsgStart = rsvc.getString(RuntimeConstants.ERRORMSG_START);
        outputMsgStart = outputMsgStart + " ";
        
        outputMsgEnd = rsvc.getString(RuntimeConstants.ERRORMSG_END );
        outputMsgEnd = " " + outputMsgEnd;   
    }

    /**
     *  iterates through the argument list and renders every
     *  argument that is appropriate.  Any non appropriate
     *  arguments are logged, but render() continues.
     */
    public boolean render(InternalContextAdapter context, 
                           Writer writer, Node node)
        throws IOException, MethodInvocationException, ResourceNotFoundException
    {
        /*
         *  get our arguments and check them
         */

        int argCount = node.jjtGetNumChildren();

        for( int i = 0; i < argCount; i++)
        {
            /*
             *  we only handle StringLiterals and References right now
             */

            Node n = node.jjtGetChild(i);

            if ( n.getType() ==  ParserTreeConstants.JJTSTRINGLITERAL || 
                 n.getType() ==  ParserTreeConstants.JJTREFERENCE )
            {
                if (!renderOutput( n, context, writer ))
                    outputErrorToStream( writer, "error with arg " + i 
                        + " please see log.");
            }
            else
            {
                rsvc.error("#include() error : invalid argument type : " 
                    + n.toString());
                outputErrorToStream( writer, "error with arg " + i 
                    + " please see log.");
            }
        }
        
        return true;
    }

    /**
     *  does the actual rendering of the included file
     *
     *  @param node AST argument of type StringLiteral or Reference
     *  @param context valid context so we can render References
     *  @param writer output Writer
     *  @return boolean success or failure.  failures are logged
     */
    private boolean renderOutput( Node node, InternalContextAdapter context, 
                                  Writer writer )
        throws IOException, MethodInvocationException, ResourceNotFoundException
    {
        String arg = "";
        
        if ( node == null )
        {
            rsvc.error("#include() error :  null argument");
            return false;
        }
            
        /*
         *  does it have a value?  If you have a null reference, then no.
         */        
        Object value = node.value( context );
        if ( value == null)
        {
            rsvc.error("#include() error :  null argument");
            return false;
        }

        /*
         *  get the path
         */
        arg = value.toString();

        Resource resource = null;

        Resource current = context.getCurrentResource();

        try
        {
            /*
             *  get the resource, and assume that we use the encoding of the current template
             *  the 'current resource' can be null if we are processing a stream....
             */

            String encoding = null;

            if ( current != null)
            {
                encoding = current.getEncoding();
            }
            else
            {
                encoding = (String) rsvc.getProperty( RuntimeConstants.INPUT_ENCODING);
            }

            resource = rsvc.getContent(arg, encoding);
        }
        catch ( ResourceNotFoundException rnfe )
        {
       		/*
       		 * the arg wasn't found.  Note it and throw
       		 */
       		 
        	rsvc.error("#include(): cannot find resource '" + arg + "', called from template " 
        		+ context.getCurrentTemplateName() + " at (" + getLine() + ", " + getColumn() + ")" );       	
        	throw rnfe;
        }

        catch (Exception e)
        {
        	rsvc.error("#include(): arg = '" + arg + "',  called from template " 
        		+ context.getCurrentTemplateName() + " at (" + getLine() + ", " + getColumn() 
        		+ ") : " + e);       	
        }            
        
        if ( resource == null )
            return false;
       
        writer.write((String)resource.getData());       
        return true;
    }

    /**
     *  Puts a message to the render output stream if ERRORMSG_START / END
     *  are valid property strings.  Mainly used for end-user template
     *  debugging.
     */
    private void outputErrorToStream( Writer writer, String msg )
        throws IOException
    {        
        if ( outputMsgStart != null  && outputMsgEnd != null)
        {
            writer.write(outputMsgStart);
            writer.write(msg);
            writer.write(outputMsgEnd);
        }
        return;
    }
}
