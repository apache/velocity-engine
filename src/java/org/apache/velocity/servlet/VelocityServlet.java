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
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.Stack;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Context;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.util.SimplePool;

/**
 * Base class which simplifies the use of Velocity with Servlets.
 * Extend this class, implement the <code>handleRequest()</code> method, 
 * and add your data to the context.  Then call 
 * <code>getTemplate("myTemplate.wm")</code>.
 * 
 * This class puts some things into the context object that you should
 * be aware of:
 * <pre>
 * "req" - The HttpServletRequest object
 * "res" - The HttpServletResponse object
 * </pre>
 *
 * If you put a contentType object into the context within either your
 * serlvet or within your template, then that will be used to override
 * the default content type specified in the properties file.
 *
 * "contentType" - The value for the Content-Type: header
 *
 * @author Dave Bryson
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * $Id: VelocityServlet.java,v 1.19 2000/12/20 07:06:04 jvanzyl Exp $
 */
public abstract class VelocityServlet extends HttpServlet
{
    /**
     * The HTTP request object context key.
     */
    public static final String REQUEST = "req";

    /**
     * The HTTP response object context key.
     */
    public static final String RESPONSE = "res";

    /**
     * The HTTP content type context key.
     */
    public static final String CONTENT_TYPE = "contentType";
    
    /**
     * The encoding to use when generating outputing.
     */
    private static String encoding = null;

    /**
     * The default content type.
     */
    private static String defaultContentType;

    /**
     * This is the string that is looked for when getInitParameter is
     * called.
     */
    private static final String INIT_PROPS_KEY = "properties";

    /**
     * Cache of writers
     */
   
    private static SimplePool writerPool = new SimplePool(40);
   
    /** 
     * Performs initialization of this servlet.  Called by the servlet 
     * container on loading.
     *
     * @param config The servlet configuration to apply.
     *
     * @exception ServletException
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );
        
        String propsFile = config.getInitParameter(INIT_PROPS_KEY);
        
        /*
         * This will attempt to find the location of the properties
         * file from the relative path to the WAR archive (ie:
         * docroot). Since JServ returns null for getRealPath()
         * because it was never implemented correctly, then we know we
         * will not have an issue with using it this way. I don't know
         * if this will break other servlet engines, but it probably
         * shouldn't since WAR files are the future anyways.
         */
        if ( propsFile != null )
        {
            String realPath = getServletContext().getRealPath(propsFile);
            if ( realPath != null )
                propsFile = realPath;
        }

        try
        {
            Runtime.init(propsFile);
            
            defaultContentType = 
                Runtime.getString(Runtime.DEFAULT_CONTENT_TYPE, "text/html");
            
            encoding = Runtime.getString(Runtime.TEMPLATE_ENCODING, "8859_1");
        }
        catch( Exception e )
        {
            throw new ServletException("Error configuring the loader: " + e);
        }
    }
    
    /**
     * Handles GET
     */
    public final void doGet( HttpServletRequest request, 
                             HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     * Handle a POST
     */
    public final void doPost( HttpServletRequest request, 
                              HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     * Process the request.
     */
    private void doRequest(HttpServletRequest request, 
                           HttpServletResponse response )
         throws ServletException, IOException
    {
        ServletOutputStream output = response.getOutputStream();
        String contentType = null;
        VelocityWriter vw = null;
        
        try
        {
            // create a new context
            Context context = new Context();
            // put the request/response objects into the context
            context.put (REQUEST, request);
            context.put (RESPONSE, response);

             // check for a content type in the context    
            if (context.containsKey(CONTENT_TYPE))
            {
                contentType = (String) context.get (CONTENT_TYPE);
            }
            else
            {
                contentType = defaultContentType;
            }
            // set the content type
            response.setContentType(contentType);

            // call whomever extends this class and implements this method
            Template template = handleRequest(context);        
            // could not find the template
            if ( template == null )
                throw new Exception ("Cannot find the template!" );
            
          
            vw = (VelocityWriter) writerPool.get();
          
            if (vw == null)
                vw = new VelocityWriter(
                    new OutputStreamWriter(output, encoding), 4*1024, true);
            else
                vw.recycle(new OutputStreamWriter(output, encoding));
           
            template.merge( context, vw);
        }
        catch (Exception e)
        {
            // display error messages
            error (output, e.getMessage());
        }
        finally
        {
            try
            {
                if (vw != null)
                {
                    vw.flush();
                    writerPool.put(vw);
                    output.close();
                }                
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
    }
    
    /**
     * Retrieves the requested template.
     *
     * @param name The file name of the template to retrieve relative to the 
     *             <code>template.path</code> property.
     * @return     The requested template.
     */
    public Template getTemplate( String name )
        throws Exception
    {
        return Runtime.getTemplate(name);
    }
    
    /**
     * Implement this method to add your application data to the context, 
     * calling the <code>getTemplate()</code> method to produce your return 
     * value.
     *
     * @param ctx The context to add your data to.
     * @return    The template to merge with your context.
     */
    protected abstract Template handleRequest( Context ctx );
 
    /**
     * Send an error message to the client.
     */
    private final void error( ServletOutputStream out, String message )
        throws ServletException, IOException
    {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>Error processing the template</h2>");
        html.append(message);
        html.append("</body>");
        html.append("</html>");
        out.print( html.toString() );
    }
}

