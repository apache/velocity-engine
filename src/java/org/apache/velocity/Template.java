package org.apache.velocity;

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

import java.io.InputStream;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
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
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Template.java,v 1.36.4.1 2004/03/03 22:28:24 geirm Exp $
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


