package org.apache.velocity.demo;

/* 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Jakarta-Regexp", and "Apache Software
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
 *
 */ 

// Java stuff
import java.util.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

// Servlet stuff
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Velocity stuff
import org.apache.velocity.context.Context;
import org.apache.velocity.Template;
import org.apache.velocity.servlet.VelocityServlet;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.MethodInvocationException;

// Demo stuff
import org.apache.velocity.demo.action.*;

/**
 * Main entry point into the forum application.
 * All requests are made to this servlet.
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.4 $
 * $Id: ControllerServlet.java,v 1.4 2001/05/19 17:26:35 geirm Exp $
 */
public class ControllerServlet extends VelocityServlet
{
    private static String ERR_MSG_TAG = "forumdemo_current_error_msg";

    
    /**
     *  lets override the loadConfiguration() so we can do some 
     *  fancier setup of the template path
     */
    protected Properties loadConfiguration(ServletConfig config )
        throws IOException, FileNotFoundException
    {
        String propsFile = config.getInitParameter(INIT_PROPS_KEY);
        
        /*
         *  now convert to an absolute path relative to the webapp root
         *  This will work in a decently implemented servlet 2.2 
         *  container like Tomcat.
         */

        if ( propsFile != null )
        {
            String realPath = getServletContext().getRealPath(propsFile);
        
            if ( realPath != null )
            {
                propsFile = realPath;
            }
        }
        
       Properties p = new Properties(); 
       p.load( new FileInputStream(propsFile) );
      

       /*
        *  now, lets get the two elements we care about, the 
        *  template path and the log, and fix those from relative
        *  to the webapp root, to absolute on the filesystem, which is
        *  what velocity needs
        */

       String path = p.getProperty("file.resource.loader.path");

       if (path != null)
       {
           path = getServletContext().getRealPath( path );
           p.setProperty( "file.resource.loader.path", path );
       }
        
       path = p.getProperty("runtime.log");

       if (path != null)
       {
           path = getServletContext().getRealPath( path );
           p.setProperty("runtime.log", path );
       }

       return p;
    }

    /**
     * VelocityServlet handles most of the Servlet issues.
     * By extending it, you need to just implement the handleRequest method.
     * @param the Context created in VelocityServlet.
     * @return the template
     */
    public Template handleRequest( Context ctx )
    {
        HttpServletRequest req = (HttpServletRequest)ctx.get(VelocityServlet.REQUEST);
        HttpServletResponse resp = (HttpServletResponse)ctx.get(VelocityServlet.RESPONSE);
        Template template = null;
        String templateName = null;
        
        HttpSession sess = req.getSession();
        sess.setAttribute(ERR_MSG_TAG, "all ok" );

        try
        {
            // Process the command
            templateName = processRequest( req, resp, ctx );
            // Get the template
            template  = getTemplate( templateName );
        }
        catch( ResourceNotFoundException rnfe )
        {
            String err = "ForumDemo -> ControllerServlet.handleRequest() : Cannot find template " + templateName ;
            sess.setAttribute( ERR_MSG_TAG, err );
            System.out.println(err );
        }
        catch( ParseErrorException pee )
        {
            String err = "ForumDemo -> ControllerServlet.handleRequest() : Syntax error in template " + templateName + ":" + pee ;
            sess.setAttribute( ERR_MSG_TAG, err );
            System.out.println(err );
        }
        catch( Exception e )
        {
            String err = "Error handling the request: " + e ;
            sess.setAttribute( ERR_MSG_TAG, err );
            System.out.println(err );
        }

        return template;
    }
    
    /**
     * Process the request and execute the command.
     * Uses a command pattern
     * @param the request
     * @param the response 
     * @param the context
     * @return the name of the template to use
     */
    private String processRequest( HttpServletRequest req, HttpServletResponse resp, Context context )
     throws Exception
    {
        Command c = null;
        String template = null;
        String name = req.getParameter("action");
        
        if ( name == null || name.length() == 0 )
        {
            throw new Exception("Unrecognized action request!");
        }
                
        if ( name.equalsIgnoreCase("list") )
        {
            c = new ListCommand( req, resp);
            template = c.exec( context );
        }
        else if ( name.equalsIgnoreCase("post") ) 
        {
            c = new PostCommand( req, resp );
            template = c.exec( context ); 
        }
        else if (  name.equalsIgnoreCase("reply") )
        {
            c = new ReplyCommand( req, resp );
            template = c.exec( context );
        }
        else if (  name.equalsIgnoreCase("postreply") )
        {
            c = new PostReplyCommand( req, resp );
            template = c.exec( context );
        }
        else if ( name.equalsIgnoreCase("view") )
        {
            c = new ViewCommand( req, resp );
            template = c.exec( context );
        }
        return template;
    }

    /**
     *  Override the method from VelocityServlet to produce an intelligent 
     *  message to the browser
     */
    protected  void error( HttpServletRequest request, HttpServletResponse response, Exception cause )
        throws ServletException, IOException
    {
        HttpSession sess = request.getSession();
        String err = (String) sess.getAttribute( ERR_MSG_TAG );

        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>ForumDemo : Error processing the request</h2>");
        html.append("<br><br>There was a problem in the request." );
        html.append("<br><br>The relevant error is :<br>");
        html.append( err );
        html.append("<br><br><br>");
        html.append("The error occurred at :<br><br>");

        StringWriter sw = new StringWriter();
        cause.printStackTrace( new PrintWriter( sw ) );

        html.append( sw.toString()  );
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print( html.toString() );
    }
}





