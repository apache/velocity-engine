package org.apache.velocity.servlet;

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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.util.SimplePool;

import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;

import org.apache.velocity.app.Velocity;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

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
 * There are other methods you can override to access, alter or control
 * any part of the request processing chain.  Please see the javadocs for
 * more information on :
 * <ul>
 * <li> loadConfiguration() : for setting up the Velocity runtime
 * <li> createContext() : for creating and loading the Context
 * <li> setContentType() : for changing the content type on a request
 *                         by request basis
 * <li> handleRequest() : you <b>must</b> implement this 
 * <li> mergeTemplate()  : the template rendering process
 * <li> requestCleanup() : post rendering resource or other cleanup
 * <li> error() : error handling
 * </ul>
 * <br>
 * If you put a contentType object into the context within either your
 * serlvet or within your template, then that will be used to override
 * the default content type specified in the properties file.
 *
 * "contentType" - The value for the Content-Type: header
 *
 * @author Dave Bryson
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="kjohnson@transparent.com">Kent Johnson</a>
 * $Id: VelocityServlet.java,v 1.41 2001/09/11 18:05:47 geirm Exp $
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
    public static final String CONTENT_TYPE = "default.contentType";

    /**
     *  The default content type for the response
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";
    
  
    /**
     *  Encoding for the output stream
     */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";
 
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
    protected static final String INIT_PROPS_KEY = "properties";

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
                
        try
        {
            /*
             *  call the overridable method to allow the 
             *  derived classes a shot at altering the configuration
             *  before initializing Runtime
             */

            Properties props = loadConfiguration( config );
  
            Velocity.init( props );
            
            defaultContentType = RuntimeSingleton.getString( CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            
            encoding = RuntimeSingleton.getString( RuntimeSingleton.OUTPUT_ENCODING, DEFAULT_OUTPUT_ENCODING);
        }
        catch( Exception e )
        {
            throw new ServletException("Error configuring the loader: " + e);
        }
    }

    /**
     *  Loads the configuration information and returns that 
     *  information as a Properties, which will be used to 
     *  initialize the Velocity runtime.
     *  <br><br>
     *  Currently, this method gets the initialization parameter
     *  VelocityServlet.INIT_PROPS_KEY, which should be a file containing
     *  the configuration information.
     *  <br><br>
     *  To configure your Servlet Spec 2.2 compliant servlet runner to pass this
     *  to you, put the following in your WEB-INF/web.xml file
     *  <br>
     *  <pre>
     *    &lt;servlet &gt;
     *      &lt;servlet-name&gt; YourServlet &lt/servlet-name&gt;
     *      &lt;servlet-class&gt; your.package.YourServlet &lt;/servlet-class&gt;
     *      &lt;init-param&gt;
     *         &lt;param-name&gt; properties &lt;/param-name&gt;
     *         &lt;param-value&gt; velocity.properties &lt;/param-value&gt;
     *      &lt;/init-param&gt;
     *    &lt;/servlet&gt;
     *   </pre>
     * 
     *  Derived classes may do the same, or take advantage of this code to do the loading for them via :
     *   <pre>
     *      Properties p = super.loadConfiguration( config );
     *   </pre>
     *  and then add or modify the configuration values from the file.
     *  <br>
     *
     *  @param config ServletConfig passed to the servlets init() function
     *                Can be used to access the real path via ServletContext (hint)
     *  @return java.util.Properties loaded with configuration values to be used
     *          to initialize the Velocity runtime.
     *  @throws FileNotFoundException if a specified file is not found.
     *  @throws IOException I/O problem accessing the specified file, if specified.
     */
    protected Properties loadConfiguration(ServletConfig config )
        throws IOException, FileNotFoundException
    {
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

        return p;
    }
          
    /**
     * Handles GET - calls doRequest()
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     * Handle a POST request - calls doRequest()
     */
    public void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doRequest(request, response);
    }

    /**
     *   Handles all requests 
     *
     *  @param request  HttpServletRequest object containing client request
     *  @param response HttpServletResponse object for the response
     */
    protected void doRequest(HttpServletRequest request, HttpServletResponse response )
         throws ServletException, IOException
    {
        try
        {
            /*
             *  first, get a context
             */

            Context context = createContext( request, response );
            
            /*
             *   set the content type 
             */

            setContentType( request, response );

            /*
             *  let someone handle the request
             */

            Template template = handleRequest( request, response, context );        

            /*
             *  bail if we can't find the template
             */

            if ( template == null )
            {
                return;
            }

            /*
             *  now merge it
             */

            mergeTemplate( template, context, response );

            /*
             *  call cleanup routine to let a derived class do some cleanup
             */

            requestCleanup( request, response, context );
        }
        catch (Exception e)
        {
            /*
             *  call the error handler to let the derived class
             *  do something useful with this failure.
             */

            error( request, response, e);
        }
    }

    /**
     *  cleanup routine called at the end of the request processing sequence
     *  allows a derived class to do resource cleanup or other end of 
     *  process cycle tasks
     *
     *  @param request servlet request from client 
     *  @param response servlet reponse 
     *  @param context  context created by the createContext() method
     */
    protected void requestCleanup( HttpServletRequest request, HttpServletResponse response, Context context )
    {
        return;
    }

    /**
     *  merges the template with the context.  Only override this if you really, really
     *  really need to. (And don't call us with questions if it breaks :)
     *
     *  @param template template object returned by the handleRequest() method
     *  @param context  context created by the createContext() method
     *  @param response servlet reponse (use this to get the output stream or Writer
     */
    protected void mergeTemplate( Template template, Context context, HttpServletResponse response )
        throws ResourceNotFoundException, ParseErrorException, 
               MethodInvocationException, IOException, UnsupportedEncodingException, Exception
    {
        ServletOutputStream output = response.getOutputStream();
        VelocityWriter vw = null;
        
        try
        {
            vw = (VelocityWriter) writerPool.get();
            
            if (vw == null)
            {
                vw = new VelocityWriter( new OutputStreamWriter(output, encoding), 4*1024, true);
            }
            else
            {
                vw.recycle(new OutputStreamWriter(output, encoding));
            }
           
            template.merge( context, vw);
        }
        finally
        {
            try
            {
                if (vw != null)
                {
                    /*
                     *  flush and put back into the pool
                     *  don't close to allow us to play
                     *  nicely with others.
                     */

                    vw.flush();
                    writerPool.put(vw);
                }                
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
    }

    /**
     *  Sets the content type of the response.  This is available to be overriden
     *  by a derived class.
     *
     *  The default implementation is :
     *
     *     response.setContentType( defaultContentType );
     * 
     *  where defaultContentType is set to the value of the default.contentType
     *  property, or "text/html" if that is not set.
     *
     *  @param request servlet request from client
     *  @param response servlet reponse to client
     */
    protected void setContentType( HttpServletRequest request, HttpServletResponse response )
    {
        response.setContentType( defaultContentType );
    }

    /**
     *  Returns a context suitable to pass to the handleRequest() method
     *  <br><br>
     *  Default implementation will create a VelocityContext object,
     *   put the HttpServletRequest and HttpServletResponse
     *  into the context accessable via the keys VelocityServlet.REQUEST and
     *  VelocityServlet.RESPONSE, respectively.
     *
     *  @param request servlet request from client
     *  @param response servlet reponse to client
     *
     *  @return context
     */
    protected Context createContext(HttpServletRequest request,  HttpServletResponse response )
    {
        /*
         *   create a new context
         */

        VelocityContext context = new VelocityContext();
        
        /*
         *   put the request/response objects into the context
         *   wrap the HttpServletRequest to solve the introspection
         *   problems 
         */
           
        context.put( REQUEST,  request );
        context.put( RESPONSE, response );

        return context;
    }

    /**
     * Retrieves the requested template.
     *
     * @param name The file name of the template to retrieve relative to the 
     *             template root.
     * @return     The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate( String name )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate(name);
    }
    
    /**
     * Retrieves the requested template with the specified
     * character encoding.
     *
     * @param name The file name of the template to retrieve relative to the 
     *             template root.
     * @param encoding the character encoding of the template
     *
     * @return     The requested template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     *     
     *  @since Velocity v1.1
     */
    public Template getTemplate( String name, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate( name, encoding );
    }

    /**
     * Implement this method to add your application data to the context, 
     * calling the <code>getTemplate()</code> method to produce your return 
     * value.
     * <br><br>
     * In the event of a problem, you may handle the request directly
     * and return <code>null</code> or throw a more meaningful exception
     * for the error handler to catch.
     *
     *  @param request servlet request from client 
     *  @param response servlet reponse 
     *  @param ctx The context to add your data to.
     *  @return    The template to merge with your context or null, indicating
     *    that you handled the processing.
     *
     *  @since Velocity v1.1
     */
    protected Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context ctx ) 
        throws Exception
    {
        /*
         * invoke handleRequest
         */

        Template t =  handleRequest( ctx );

        /*
         *  if it returns null, this is the 'old' deprecated 
         *  way, and we want to mimic the behavior for a little 
         *  while anyway
         */

        if (t == null)
        {
            throw new Exception ("handleRequest(Context) returned null - no template selected!" );
        }

        return t;
    }

    /**
     * Implement this method to add your application data to the context, 
     * calling the <code>getTemplate()</code> method to produce your return 
     * value.
     * <br><br>
     * In the event of a problem, you may simple return <code>null</code>
     * or throw a more meaningful exception.
     *
     * @deprecated Use
     * {@link #handleRequest( HttpServletRequest request, 
     * HttpServletResponse response, Context ctx )}
     *
     * @param ctx The context to add your data to.
     * @return    The template to merge with your context.
     */
    protected Template handleRequest( Context ctx ) 
        throws Exception
    {
        throw new Exception ("You must override VelocityServlet.handleRequest( Context) "
                             + " or VelocityServlet.handleRequest( HttpServletRequest, "
                             + " HttpServletResponse, Context)" );
    }
 
    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     * 
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param cause  Exception that was thrown by some other part of process.
     */
    protected  void error( HttpServletRequest request, HttpServletResponse response, Exception cause )
        throws ServletException, IOException
    {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>VelocityServlet : Error processing the template</h2>");
        html.append( cause );
        html.append("<br>");

        StringWriter sw = new StringWriter();
        cause.printStackTrace( new PrintWriter( sw ) );

        html.append( sw.toString()  );
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print( html.toString() );
    }
}

