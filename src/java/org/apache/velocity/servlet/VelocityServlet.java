package org.apache.velocity.servlet;

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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Context;
import org.apache.velocity.Template;

import org.apache.velocity.runtime.Runtime;

import org.apache.velocity.io.FastWriter;

//import org.apache.velocity.runtime.TemplateLoader;
//import org.apache.velocity.runtime.TemplateFactory;

/**
 * Base class to use Velocity with Servlets.
 * Simply extend this class, override the handleRequest method
 * and add your data to the context. Then call getTemplate("mytemplate.wm")
 * 
 * @author Dave Bryson
 * $Revision: 1.1 $
 */
public abstract class VelocityServlet extends HttpServlet
{
    private String encoding;
    private boolean asciiHack;
    private FastWriter writer;

    /** 
     * Init the loader
     * @param ServletConfig
     */
    public void init( ServletConfig config )
     throws ServletException
    {
        super.init( config );
        
        String file = config.getInitParameter("properties");
        try
        {
            Runtime.init(file);
            
            encoding = Runtime.getString(
                Runtime.TEMPLATE_ENCODING);
            
            asciiHack = Runtime.getBoolean(
                Runtime.TEMPLATE_ASCIIHACK);
        }
        catch( Exception e1 )
        {
            throw new ServletException( "Error configuring the loader: " + e1);
        }
    }
    
    /**
     * Handles GET
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     * Handle a POST
     */
    public void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }
    
    /**
     * Process the request.
     */
    private void doRequest(HttpServletRequest request, HttpServletResponse response )
         throws ServletException, IOException
    {
        Context context = new Context();
        Template template = handleRequest(context);
        
        if ( template == null )
        {
            error( response, "Cannot find template!");
            return;
        }

        response.setContentType("text/html");
        
        writer = new FastWriter(response.getOutputStream(), encoding);
        writer.setAsciiHack(asciiHack);
        template.merge( context, writer );
    }
    
    /**
     * @return the requested template
     */
    public Template getTemplate( String name )
     throws Exception
    {
        return Runtime.getTemplate(name);
    }
    
    /**
     * Override the method to add your information to the
     * context and call the getTemplate method.
     * @param Context 
     * @return Template
     */
    public abstract Template handleRequest( Context ctx );
 
    /**
     * Send an error message
     */
    private void error( HttpServletResponse response, String message )
        throws ServletException, IOException
    {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>Error processing the template</h2>");
        html.append(message);
        html.append("</body>");
        html.append("</html>");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println( html.toString() );
    }
}





