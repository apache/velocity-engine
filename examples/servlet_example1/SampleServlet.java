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
 * @version $Id: SampleServlet.java,v 1.4.4.1 2001/12/09 19:53:07 geirm Exp $
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




