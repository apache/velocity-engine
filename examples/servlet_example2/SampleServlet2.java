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
import java.io.FileInputStream;

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
 *  Sample of how to use the VelocityServlet.  This example
 *  is intended to show how to use an external properties
 *  file.  Note that :
 *  <ul>
 *    <li> It assumes that the path to the velocity log
 *      is relative to the webapp root
 *    <li> If specified, it assumes that the path for the 
 *       file resource loader is single, and relative to
 *       the webapp root
 *  </ul>
 * 
 * @author Dave Bryson
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: SampleServlet2.java,v 1.3.8.1 2004/03/04 00:18:29 geirm Exp $
 */
public class SampleServlet2 extends VelocityServlet
{

    /**  
     *  A fancier version of loadConfiguration(), this will
     *  set the log file to be off of the webapp root, and
     *  will do the same with the file loader paths
     */
   protected Properties loadConfiguration(ServletConfig config )
        throws IOException, FileNotFoundException
    {
        /*
         *  get our properties file and load it
         */

        String propsFile = config.getInitParameter(INIT_PROPS_KEY);
        
        Properties p = new Properties();
        
        if ( propsFile != null )
        {
            String realPath = getServletContext().getRealPath(propsFile);
        
            if ( realPath != null )
            {
                propsFile = realPath;
            }

            p.load( new FileInputStream(propsFile) );
        }

        /*
         *  first, normalize our velocity log file to be in the 
         *  webapp
         */

        String log = p.getProperty( Velocity.RUNTIME_LOG);

        if (log != null )
        {
            log = getServletContext().getRealPath( log );
            
            if (log != null)
            {
                p.setProperty( Velocity.RUNTIME_LOG, log );
            }
        }

       
        /*
         *  now, if there is a file loader resource path, treat it the
         *  same way.
         */

        String path = p.getProperty( Velocity.FILE_RESOURCE_LOADER_PATH );

        if ( path != null)
        {
            path = getServletContext().getRealPath(  path );

            if ( path != null)
            {
                p.setProperty( Velocity.FILE_RESOURCE_LOADER_PATH, path );
            }
        }

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




