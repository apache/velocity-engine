package org.apache.velocity.example;
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
 * @version $Id$
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
                builder = new SAXBuilder();
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

