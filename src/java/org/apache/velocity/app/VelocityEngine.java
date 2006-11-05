package org.apache.velocity.app;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * <p>
 * This class provides a separate new-able instance of the
 * Velocity template engine.  The alternative model for use
 * is using the Velocity class which employs the singleton
 * model.
 * </p>
 *
 * <p>
 * Please ensure that you call one of the init() variants.
 * This is critical for proper behavior.
 * </p>
 *
 * <p> Coming soon : Velocity will call
 * the parameter-less init() at the first use of this class
 * if the init() wasn't explicitly called.  While this will
 * ensure that Velocity functions, it almost certainly won't
 * function in the way you intend, so please make sure to
 * call init().
 * </p>
 *
 * @version $Id$
 */
public class VelocityEngine implements RuntimeConstants
{
    private RuntimeInstance ri = new RuntimeInstance();


    /**
     *  Init-less CTOR
     */
    public VelocityEngine()
    {
        // do nothing
    }

    /**
     *  CTOR that invokes an init(String), initializing
     *  the engine using the properties file specified
     *
     * @param propsFilename name of properties file to init with
     * @throws Exception
     */
    public VelocityEngine(String propsFilename)
        throws Exception
    {
        ri.init(propsFilename);
    }

    /**
     *  CTOR that invokes an init(String), initializing
     *  the engine using the Properties specified
     *
     * @param p name of properties  to init with
     * @throws Exception
     */
    public VelocityEngine(Properties p)
        throws Exception
    {
        ri.init(p);
    }

    /**
     *  initialize the Velocity runtime engine, using the default
     *  properties of the Velocity distribution
     * @throws Exception
     */
    public void init()
        throws Exception
    {
        ri.init();
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the properties file passed in as the arg
     *
     *  @param propsFilename file containing properties to use to initialize
     *         the Velocity runtime
     * @throws Exception
     */
    public void init(String propsFilename)
        throws Exception
    {
        ri.init(propsFilename);
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the passed in java.util.Properties object
     *
     *  @param p  Proprties object containing initialization properties
     * @throws Exception
     *
     */
    public void init(Properties p)
        throws Exception
    {
        ri.init(p);
    }

    /**
     * Set a Velocity Runtime property.
     *
     * @param  key
     * @param  value
     */
    public void setProperty(String key, Object value)
    {
        ri.setProperty(key,value);
    }

    /**
     * Add a Velocity Runtime property.
     *
     * @param  key
     * @param  value
     */
    public void addProperty(String key, Object value)
    {
        ri.addProperty(key,value);
    }

    /**
     * Clear a Velocity Runtime property.
     *
     * @param key of property to clear
     */
    public void clearProperty(String key)
    {
        ri.clearProperty(key);
    }

    /**
     * Set an entire configuration at once. This is
     * useful in cases where the parent application uses
     * the ExtendedProperties class and the velocity configuration
     * is a subset of the parent application's configuration.
     *
     * @param  configuration
     *
     */
    public void setExtendedProperties( ExtendedProperties configuration)
    {
        ri.setConfiguration( configuration );
    }

    /**
     *  Get a Velocity Runtime property.
     *
     *  @param key property to retrieve
     *  @return property value or null if the property
     *        not currently set
     */
    public Object getProperty( String key )
    {
        return ri.getProperty( key );
    }

    /**
     *  renders the input string using the context into the output writer.
     *  To be used when a template is dynamically constructed, or want to use
     *  Velocity as a token replacer.
     *
     *  @param context context to use in rendering input string
     *  @param out  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log
     *                 messages in case of error
     *  @param instring input string containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see
     *             Velocity runtime log
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws ResourceNotFoundException
     * @throws IOException
     */
    public  boolean evaluate( Context context,  Writer out,
                                     String logTag, String instring )
        throws ParseErrorException, MethodInvocationException,
            ResourceNotFoundException, IOException
    {
        return evaluate( context, out, logTag, new BufferedReader( new StringReader( instring )) );
    }

    /**
     *  Renders the input stream using the context into the output writer.
     *  To be used when a template is dynamically constructed, or want to
     *  use Velocity as a token replacer.
     *
     *  @param context context to use in rendering input string
     *  @param writer  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log messages
     *                 in case of error
     *  @param instream input stream containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see
     *               Velocity runtime log
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws ResourceNotFoundException
     * @throws IOException
     *  @deprecated Use
     *  {@link #evaluate( Context context, Writer writer,
     *      String logTag, Reader reader ) }
     */
    public boolean evaluate( Context context, Writer writer,
                                    String logTag, InputStream instream )
        throws ParseErrorException, MethodInvocationException,
            ResourceNotFoundException, IOException
    {
        /*
         *  first, parse - convert ParseException if thrown
         */
        BufferedReader br  = null;
        String encoding = null;

        try
        {
            encoding = ri.getString(INPUT_ENCODING,ENCODING_DEFAULT);
            br = new BufferedReader(  new InputStreamReader( instream, encoding));
        }
        catch( UnsupportedEncodingException  uce )
        {
            String msg = "Unsupported input encoding : " + encoding
                + " for template " + logTag;
            throw new ParseErrorException( msg );
        }

        return evaluate( context, writer, logTag, br );
    }

    /**
     *  Renders the input reader using the context into the output writer.
     *  To be used when a template is dynamically constructed, or want to
     *  use Velocity as a token replacer.
     *
     *  @param context context to use in rendering input string
     *  @param writer  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log messages
     *                 in case of error
     *  @param reader Reader containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see
     *               Velocity runtime log
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws ResourceNotFoundException
     * @throws IOException
     *
     *  @since Velocity v1.1
     */
    public boolean evaluate(Context context, Writer writer,
                                    String logTag, Reader reader)
        throws ParseErrorException, MethodInvocationException,
            ResourceNotFoundException,IOException
    {
        SimpleNode nodeTree = null;

        try
        {
            nodeTree = ri.parse(reader, logTag);
        }
        catch (ParseException pex)
        {
            throw  new ParseErrorException( pex );
        }
        catch (TemplateInitException pex)
        {
            throw  new ParseErrorException( pex );
        }

        /*
         * now we want to init and render
         */

        if (nodeTree != null)
        {
            InternalContextAdapterImpl ica =
                new InternalContextAdapterImpl( context );

            ica.pushCurrentTemplateName( logTag );

            try
            {
                try
                {
                    nodeTree.init( ica, ri );
                }
                catch (TemplateInitException pex)
                {
                    throw  new ParseErrorException( pex );
                }
                /**
                 * pass through application level runtime exceptions
                 */
                catch( RuntimeException e )
                {
                    throw e;
                }
                catch(Exception e)
                {
                    getLog().error("Velocity.evaluate() : init exception for tag = "
                                   + logTag, e);
                }

                /*
                 *  now render, and let any exceptions fly
                 */

                nodeTree.render( ica, writer );
            }
            finally
            {
                ica.popCurrentTemplateName();
            }

            return true;
        }

        return false;
    }


    /**
     *  Invokes a currently registered Velocimacro with the parms provided
     *  and places the rendered stream into the writer.
     *
     *  Note : currently only accepts args to the VM if they are in the context.
     *
     *  @param vmName name of Velocimacro to call
     *  @param logTag string to be used for template name in case of error
     *  @param params args used to invoke Velocimacro. In context key format :
     *                  eg  "foo","bar" (rather than "$foo","$bar")
     *  @param context Context object containing data/objects used for rendering.
     *  @param writer  Writer for output stream
     *  @return true if Velocimacro exists and successfully invoked, false otherwise.
     * @throws Exception
     */
    public boolean invokeVelocimacro( String vmName, String logTag,
                                              String params[], Context context,
                                              Writer writer )
        throws Exception
    {
        /*
         *  check parms
         */

        if ( vmName == null ||  params == null ||  context == null
             || writer == null || logTag == null)
        {
            getLog().error("VelocityEngine.invokeVelocimacro() : invalid parameter");
            return false;
        }

        /*
         * does the VM exist?
         */

        if (!ri.isVelocimacro( vmName, logTag ))
        {
            getLog().error("VelocityEngine.invokeVelocimacro() : VM '" + vmName
                           + "' not registered.");
            return false;
        }

        /*
         *  now just create the VM call, and use evaluate
         */

        StringBuffer construct = new StringBuffer("#");

        construct.append( vmName );
        construct.append( "(" );

        for( int i = 0; i < params.length; i++)
        {
            construct.append( " $" );
            construct.append( params[i] );
        }

        construct.append(" )");

        try
        {
            boolean retval = evaluate(  context,  writer,
                                         logTag, construct.toString() );

            return retval;
        }
        /**
         * pass through application level runtime exceptions
         */
        catch( RuntimeException e )
        {
            throw e;
        }
        catch(Exception e)
        {
            getLog().error("VelocityEngine.invokeVelocimacro() : error ", e);
            throw e;
        }
    }

    /**
     *  Merges a template and puts the rendered stream into the writer.
     *  The default encoding that Velocity uses to read template files is defined in
     *  the property input.encoding and defaults to ISO-8859-1.
     *
     *  @param templateName name of template to be used in merge
     *  @param context  filled context to be used in merge
     *  @param  writer  writer to write template into
     *
     *  @return true if successful, false otherwise.  Errors
     *           logged to velocity log.
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     * *  @deprecated Use
     *  {@link #mergeTemplate( String templateName, String encoding,
     *                Context context, Writer writer )}
     */
    public boolean mergeTemplate( String templateName,
                                         Context context, Writer writer )
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception
    {
        return mergeTemplate( templateName, ri.getString(INPUT_ENCODING,ENCODING_DEFAULT),
                               context, writer );
    }

    /**
     *  merges a template and puts the rendered stream into the writer
     *
     *  @param templateName name of template to be used in merge
     *  @param encoding encoding used in template
     *  @param context  filled context to be used in merge
     *  @param  writer  writer to write template into
     *
     *  @return true if successful, false otherwise.  Errors
     *           logged to velocity log
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws Exception
     *
     *  @since Velocity v1.1
     */
    public boolean mergeTemplate( String templateName, String encoding,
                                      Context context, Writer writer )
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception
    {
        Template template = ri.getTemplate(templateName, encoding);

        if ( template == null )
        {
            getLog().error("Velocity.mergeTemplate() was unable to load template '"
                           + templateName + "'");
            return false;
        }
        else
        {
            template.merge(context, writer);
            return true;
         }
    }

    /**
     *  Returns a <code>Template</code> from the Velocity
     *  resource management system.
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return ri.getTemplate( name );
    }

    /**
     *  Returns a <code>Template</code> from the Velocity
     *  resource management system.
     *
     * @param name The file name of the desired template.
     * @param encoding The character encoding to use for the template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     *
     *  @since Velocity v1.1
     */
    public Template getTemplate(String name, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return ri.getTemplate( name, encoding );
    }

    /**
     *   Determines if a resource is accessable via the currently
     *   configured resource loaders.
     *   <br><br>
     *   Note that the current implementation will <b>not</b>
     *   change the state of the system in any real way - so this
     *   cannot be used to pre-load the resource cache, as the
     *   previous implementation did as a side-effect.
     *   <br><br>
     *   The previous implementation exhibited extreme lazyness and
     *   sloth, and the author has been flogged.
     *
     *   @param resourceName  name of the resource to search for
     *   @return true if found, false otherwise
     */
    public boolean resourceExists(String resourceName)
    {
        return (ri.getLoaderNameForResource(resourceName) != null);
    }

    /**
     * @param resourceName
     * @return True if the template exists.
     * @see #resourceExists(String)
     * @deprecated Use resourceExists(String) instead.
     */
    public boolean templateExists(String resourceName)
    {
        return resourceExists(resourceName);
    }


    /**
     * Returns a convenient Log instance that wraps the current LogChute.
     * Use this to log error messages. It has the usual methods you'd expect.
     * @return A log object.
     */
    public Log getLog()
    {
        return ri.getLog();
    }

    /**
     * @param message
     * @deprecated Use getLog() and call warn() on it.
     */
    public void warn(Object message)
    {
        getLog().warn(message);
    }

    /**
     * @param message
     * @deprecated Use getLog() and call warn() on it.
     */
    public void info(Object message)
    {
        getLog().info(message);
    }

    /**
     * @param message
     * @deprecated Use getLog() and call warn() on it.
     */
    public void error(Object message)
    {
        getLog().error(message);
    }

    /**
     * @param message
     * @deprecated Use getLog() and call warn() on it.
     */
    public void debug(Object message)
    {
        getLog().debug(message);
    }

    /**
     *  <p>
     *  Sets an application attribute (which can be any Object) that will be
     *  accessible from any component of the system that gets a
     *  RuntimeServices. This allows communication between the application
     *  environment and custom pluggable components of the Velocity engine,
     *  such as ResourceLoaders and LogChutes.
     *  </p>
     *
     *  <p>
     *  Note that there is no enforcement or rules for the key
     *  used - it is up to the application developer.  However, to
     *  help make the intermixing of components possible, using
     *  the target Class name (e.g. com.foo.bar ) as the key
     *  might help avoid collision.
     *  </p>
     *
     *  @param key object 'name' under which the object is stored
     *  @param value object to store under this key
     */
     public void setApplicationAttribute( Object key, Object value )
     {
        ri.setApplicationAttribute(key, value);
     }

     /**
      *  <p>
      *  Return an application attribute (which can be any Object)
      *  that was set by the application in order to be accessible from
      *  any component of the system that gets a RuntimeServices.
      *  This allows communication between the application
      *  environment and custom pluggable components of the
      *  Velocity engine, such as ResourceLoaders and LogChutes.
      *  </p>
      *
      *  @param key object 'name' under which the object is stored
      *  @return value object to store under this key
      */
     public Object getApplicationAttribute( Object key )
     {
        return ri.getApplicationAttribute(key);
     }

}
