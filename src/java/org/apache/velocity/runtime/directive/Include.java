package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import java.io.*;

import org.apache.velocity.runtime.parser.*;
import org.apache.velocity.Context;
import org.apache.velocity.runtime.configuration.*;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Pluggable directive that handles the #include() statement in VTL. 
 * This #include() can take multiple arguments of either StringLiteral or Reference.
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
 * @version $Id: Include.java,v 1.9 2000/12/04 02:08:19 geirm Exp $
 */
public class Include extends Directive
{
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
     *  iterates through the argument list and renders every
     *  argument that is appropriate.  Any non appropriate
     *  arguments are logged, but render() continues.
     */
    public boolean render(Context context, Writer writer, Node node)
        throws IOException
    {
        /*
         *  get our arguments and check them
         */

        int iArgCount = node.jjtGetNumChildren();

        for( int i = 0; i < iArgCount; i++)
        {
            /*
             *  we only handle StringLiterals and References right now
             */

            Node n = node.jjtGetChild(i);

            if ( n.getType() ==  ParserTreeConstants.JJTSTRINGLITERAL || n.getType() ==  ParserTreeConstants.JJTREFERENCE )
            {
                if (!renderOutput( n, context, writer ))
                    outputErrorToStream( writer, "error with arg " + i + " please see log.");
            }
            else
            {
                Runtime.error( new String("#include() error : invalid argument type : " + n.toString() ) );
                outputErrorToStream( writer, "error with arg " + i + " please see log.");
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
    private boolean renderOutput( Node argNode, Context context, Writer writer )
       throws IOException
    {
         String strArg = "";
        
        if (argNode == null)
        {
            Runtime.error( new String("#include() error :  null argument") );
            return false;
        }
            
        /*
         *  does it have a value?  If you have a null reference, then no.
         */
        
        Object value = argNode.value( context );
        
        if ( value == null)
        {
            Runtime.error( new String("#include() error :  null argument") );
            return false;
        }

        /*
         *  get the path
         */
        
        strArg = value.toString();
            
        /*
         *  everything must be under the template root TEMPLATE_PATH
         */
        
        //String strTemplatePath = Runtime.getString(Runtime.TEMPLATE_PATH);
        String[] includePaths = Runtime.getIncludePaths();
        
        /*
         *  for security, we will not accept anything with .. in the path
         */
        
        if ( strArg.indexOf("..") != -1)
        {
            Runtime.error( new String("#include() error : argument " + strArg + " contains .. and may be trying to access content outside of template root.  Rejected.") );
            return false;
        }

        /*
         *  if a / leads off, then just nip that :)
         */
        
        if ( strArg.startsWith( "/") )
            strArg = strArg.substring(1);
        
        /*
         *  we will put caching here in the future...
         */

        // Try to locate the file in all of the possible
        // locations. If we can't even find a trace of existence
        // report the problem and leave.
        File file = null;
        for (int i = 0; i < includePaths.length; i++)
        {
            file = new File(includePaths[i], strArg);
            
            if (file.exists())
                break;
            else
                file = null;
        }
    
        if (file == null)
        {
            Runtime.error("#include() : " + strArg + " doesn't exist!");
            return false;
        }            
        
        try
        {
            BufferedReader reader = new BufferedReader( new FileReader(file.getAbsolutePath()));
            
            char buf[] = new char[1024];
            int iLen = 0;
            
            while ( ( iLen = reader.read( buf, 0, 1024 )) != -1)
                writer.write( buf,0,iLen );
        }
        catch ( Exception e ) 
        {
            Runtime.error( new String("#include() : " + e ));
            return false;
        }
    
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
        String strOutputMsgStart = Runtime.getString(Runtime.ERRORMSG_START);
        String strOutputMsgEnd = Runtime.getString(Runtime.ERRORMSG_END );
        
        if ( strOutputMsgStart != null  && strOutputMsgEnd != null)
        {
            writer.write( strOutputMsgStart + " " + msg + " " + strOutputMsgEnd );
        }

        return;
    }
}
