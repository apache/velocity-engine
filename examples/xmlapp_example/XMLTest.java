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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;


/**
 * Example to show basic XML handling in a template.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: XMLTest.java,v 1.2 2001/04/10 16:51:29 geirm Exp $
 */
public class XMLTest
{
    public XMLTest( String templateFile)
    {    
        Writer writer = null;

        try
        {
            /*
             *  and now call init
             */

            Velocity.init();

                       
            /*
             * build a Document from our xml
             */

            SAXBuilder builder;
            Document root = null;

            try 
            {
                builder = new SAXBuilder(  "org.apache.xerces.parsers.SAXParser" );
                root = builder.build("test.xml");
            }
            catch( Exception ee)
            { 
                System.out.println("Exception building Document : " + ee);
                return;
            }

            /*
             * now, make a Context object and populate it.
             */

            VelocityContext context = new VelocityContext();
            context.put("root", root);

            /*
             *  make a writer, and merge the template 'against' the context
             */
 
            Template template = Velocity.getTemplate(templateFile);

            writer = new BufferedWriter(new OutputStreamWriter(System.out));
            template.merge( context , writer);  
        }
        catch( Exception e )
        {
           System.out.println("Exception : " + e);
        }
        finally
        {
            if ( writer != null)
            {
                try
                {
                    writer.flush();
                    writer.close();
                }
                catch( Exception ee )
                {
                    System.out.println("Exception : " + ee );
                }
            }
        }
    }

    public static void main(String[] args)
    {
        XMLTest t;

        if( args.length < 1 )
        {
            System.out.println("Usage : java XMLTest <templatename>");
            return;
        }

        t = new XMLTest(args[0]);
    }
}

