package org.apache.velocity;

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

import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.InternalContextAdapterImpl;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

/**
 * This class is used for controlling all template
 * operations. This class uses a parser created
 * by JavaCC to create an AST that is subsequently
 * traversed by a Visitor. 
 *
 * <pre>
 * Template template = Velocity.getTemplate("test.wm");
 * Context context = new VelocityContext();
 *
 * context.put("foo", "bar");
 * context.put("customer", new Customer());
 *
 * template.merge(context, writer);
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Template.java,v 1.34 2001/08/07 22:20:03 geirm Exp $
 */
public class Template extends Resource
{
    /**
     *   To keep track of whether this template has been
     *   initialized. We use the document.init(context)
     *   to perform this.
     */
    private boolean initialized = false;

    private Exception errorCondition = null;

    /** Default constructor */
    public Template()
    {
    }

    /**
     *  gets the named resource as a stream, parses and inits
     *
     * @return true if successful
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception some other problem, should only be from 
     *          initialization of the template AST.
     */
    public boolean process()
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        data = null;
        InputStream is = null;
        errorCondition = null;

        /*
         *  first, try to get the stream from the loader
         */
        try 
        {
            is = resourceLoader.getResourceStream(name);
        }
        catch( ResourceNotFoundException rnfe )
        {
            /*
             *  remember and re-throw
             */

            errorCondition = rnfe;
            throw rnfe;
        }

        /*
         *  if that worked, lets protect in case a loader impl
         *  forgets to throw a proper exception
         */

        if (is != null)
        {
            /*
             *  now parse the template
             */

            try
            {
                BufferedReader br = new BufferedReader( new InputStreamReader( is, encoding ) );
 
                data = rsvc.parse( br, name);
                initDocument();
                return true;
            }
            catch( UnsupportedEncodingException  uce )
            {   
                String msg = "Template.process : Unsupported input encoding : " + encoding 
                + " for template " + name;

                errorCondition  = new ParseErrorException( msg );
                throw errorCondition;
            }
            catch ( ParseException pex )
            {
                /*
                 *  remember the error and convert
                 */

               errorCondition =  new ParseErrorException( pex.getMessage() );
               throw errorCondition;
            }
            catch( Exception e )
            {
                /*
                 *  who knows?  Something from initDocument()
                 */

                errorCondition = e;
                throw e;
            }
            finally 
            {
                /*
                 *  Make sure to close the inputstream when we are done.
                 */
                is.close();
            }
        }    
        else
        {
            /* 
             *  is == null, therefore we have some kind of file issue
             */

            errorCondition = new ResourceNotFoundException("Unknown resource error for resource " + name );
            throw errorCondition;
        }
    }

    /**
     *  initializes the document.  init() is not longer 
     *  dependant upon context, but we need to let the 
     *  init() carry the template name down throught for VM
     *  namespace features
     */
    public void initDocument()
        throws Exception
    {
        /*
         *  send an empty InternalContextAdapter down into the AST to initialize it
         */
        
        InternalContextAdapterImpl ica = new InternalContextAdapterImpl(  new VelocityContext() );

        try
        {
            /*
             *  put the current template name on the stack
             */

            ica.pushCurrentTemplateName( name );
            
            /*
             *  init the AST
             */

            ((SimpleNode)data).init( ica, rsvc);
        }
        finally
        {
            /*  
             *  in case something blows up...
             *  pull it off for completeness
             */

            ica.popCurrentTemplateName();
        }

    }

    /**
     * The AST node structure is merged with the
     * context to produce the final output. 
     *
     * Throws IOException if failure is due to a file related
     * issue, and Exception otherwise
     *
     *  @param context Conext with data elements accessed by template
     *  @param writer output writer for rendered template
     *  @throws ResourceNotFoundException if template not found
     *          from any available source.
     *  @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     *  @throws  Exception  anything else. 
     */
    public void merge( Context context, Writer writer)
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception
    {
        /*
         *  we shouldn't have to do this, as if there is an error condition, 
         *  the application code should never get a reference to the 
         *  Template
         */

        if (errorCondition != null)
        {
            throw errorCondition;
        }

        if( data != null)
        {
            /*
             *  create an InternalContextAdapter to carry the user Context down
             *  into the rendering engine.  Set the template name and render()
             */

            InternalContextAdapterImpl ica = new InternalContextAdapterImpl( context );

            try
            {
                ica.pushCurrentTemplateName( name );
                ica.setCurrentResource( this );

                ( (SimpleNode) data ).render( ica, writer);
            }
            finally
            {
                /*
                 *  lets make sure that we always clean up the context 
                 */
                ica.popCurrentTemplateName();
                ica.setCurrentResource( null );
            }
        }
        else
        {
            /*
             * this shouldn't happen either, but just in case.
             */

            String msg = "Template.merge() failure. The document is null, " + 
                "most likely due to parsing error.";

            rsvc.error(msg);
            throw new Exception(msg);
        }
    }
}


