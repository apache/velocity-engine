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

import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

/**
 * Sample of how to use the VelocityServlet.
 * This example shows how to add objects to the context and
 * pass them to the template.
 * 
 * @author Dave Bryson
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: SampleServlet.java,v 1.5.8.1 2004/03/04 00:18:29 geirm Exp $
 */
public class SampleServlet extends VelocityServlet
{
    
    /**
     *   Called by the VelocityServlet
     *   init().  We want to set a set of properties
     *   so that templates will be found in the webapp
     *   root.  This makes this easier to work with as 
     *   an example, so a new user doesn't have to worry
     *   about config issues when first figuring things
     *   out
     */
    protected Properties loadConfiguration(ServletConfig config )
        throws IOException, FileNotFoundException
    {
        Properties p = new Properties();

        /*
         *  first, we set the template path for the
         *  FileResourceLoader to the root of the 
         *  webapp.  This probably won't work under
         *  in a WAR under WebLogic, but should 
         *  under tomcat :)
         */

        String path = config.getServletContext().getRealPath("/");

        if (path == null)
        {
            System.out.println(" SampleServlet.loadConfiguration() : unable to " 
                               + "get the current webapp root.  Using '/'. Please fix.");

            path = "/";
        }

        p.setProperty( Velocity.FILE_RESOURCE_LOADER_PATH,  path );

        /**
         *  and the same for the log file
         */

        p.setProperty( "runtime.log", path + "velocity.log" );

        return p;
    }


    /**
     *  <p>
     *  main routine to handle a request.  Called by
     *  VelocityServlet, your responsibility as programmer
     *  is to simply return a valid Template
     *  </p>
     *
     *  @param ctx a Velocity Context object to be filled with
     *             data.  Will be used for rendering this 
     *             template
     *  @return Template to be used for request
     */   
    public Template handleRequest( HttpServletRequest request,
	HttpServletResponse response, Context ctx )
    {        
        /*
         *  set up some data to put into the context
         */

        String p1 = "Bob";
        String p2 = "Harold";
        
        Vector personList = new Vector();
        personList.addElement( p1 );
        personList.addElement( p2 );

        /*
         *  Add the list to the context.
         *  This is how it's passed to the template.
         */

        ctx.put("theList", personList );
        
        /*
         *  get the template.  There are three possible
         *  exceptions.  Good to know what happened.
         */

        Template outty = null;
        
        try
        {
            outty =  getTemplate("sample.vm");
        }
        catch( ParseErrorException pee )
        {
            System.out.println("SampleServlet : parse error for template " + pee);
        }
        catch( ResourceNotFoundException rnfe )
        {
            System.out.println("SampleServlet : template not found " + rnfe);
        }
        catch( Exception e )
        {
            System.out.println("Error " + e);
        }
        return outty;
    }
}




