/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.*;
import java.util.ArrayList;

/**
 * This class is a simple demonstration of how the Velocity Template Engine
 * can be used in a standalone application.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Example.java,v 1.3.8.1 2004/03/04 00:18:30 geirm Exp $
 */

public class Example
{
    VelocityContext context = null;
    
    private final static String JAR_RESOURCE_LOADER_PATH1 = "jar:file:test.jar!/";

    private final static String JAR_RESOURCE_LOADER_PATH2 = "jar:file:template.jar!/";

    
    public Example()
    {
        try
        {
            /*
             * setup
             */

            Runtime.init("velocity.properties");
            //Runtime.setDefaultProperties();
            
            //Runtime.setSourceProperty(Runtime.JAR_RESOURCE_LOADER_PATH, JAR_RESOURCE_LOADER_PATH1);
            
            //Runtime.setSourceProperty(Runtime.JAR_RESOURCE_LOADER_PATH, JAR_RESOURCE_LOADER_PATH2);
            
            //Runtime.init();

            /*
             *  Make a context object and populate with the data.  This 
             *  is where the Velocity engine gets the data to resolve the
             *  references (ex. $list) in the template
             */
            
            context = new VelocityContext();
            context.put("list", getNames());
            
        }
        catch( Exception e )
        {
            Runtime.error("ERROR starting runtime: " + e );
        }
    }
    
    public void getTemplate( String templateFile)
    throws Exception{
        try
        {
            
            Template template =  null;
            
            try 
            {
                template = Runtime.getTemplate(templateFile);
            }
            catch( ResourceNotFoundException rnfe )
            {
                System.out.println("Example : error : cannot find template " + templateFile );
            }
            catch( ParseErrorException pee )
            {
                System.out.println("Example : Syntax error in template " + templateFile + ":" + pee );
            }

            /*
             *  Now have the template engine process your template using the
             *  data placed into the context.  Think of it as a  'merge' 
             *  of the template and the data to produce the output stream.
             */

            BufferedWriter writer = writer = new BufferedWriter(
                new OutputStreamWriter(System.out));

            if ( template != null)
                template.merge(context, writer);

            /*
             *  flush and cleanup
             */

            writer.flush();
            writer.close();
        }
        catch( Exception e )
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public ArrayList getNames()
    {
        ArrayList list = new ArrayList();

        list.add("ArrayList element 1");
        list.add("ArrayList element 2");
        list.add("ArrayList element 3");
        list.add("ArrayList element 4");

        return list;
    }

    public static void main(String[] args)
    {
        Example ex = new Example();
        
        try
        {
            say( "Started..." );
            
            say("get template from template.jar");
            ex.getTemplate( "/template/test1.vm" );
            
            say("get template from test.jar");
            ex.getTemplate( "/example/test1.vm" );
            
            say("try template from template.jar again");
            ex.getTemplate( "/template/test2.vm" );
            
            say("Try something that doesn't exist");
            ex.getTemplate( "/example/yomama.vm" );
        }
        catch(Exception e )
        {
            say("ERROR");
        }
    }

    private static void say( String m )
    {
        System.out.println(m);
    }
}





