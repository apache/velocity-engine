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
 * @version $Id: SampleServlet2.java,v 1.2.2.1 2001/12/09 19:51:17 geirm Exp $
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




