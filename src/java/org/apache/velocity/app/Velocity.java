package org.apache.velocity.app;

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

import java.io.Writer;
import java.util.Hashtable;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.velocity.context.Context;
import org.apache.velocity.Template;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.runtime.configuration.Configuration;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.runtime.parser.ParseException;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This class provides  services to the application 
 * developer, such as :
 * <ul>
 * <li> Simple Velocity Runtime engine initialization methods.
 * <li> Functions to apply the template engine to streams and strings
 *      to allow embedding and dynamic template generation.
 * <li> Methods to access Velocimacros directly.
 * </ul>
 *
 * <br><br>
 * While the most common way to use Velocity is via templates, as 
 * Velocity is a general-purpose template engine, there are other
 * uses that Velocity is well suited for, such as processing dynamically
 * created templates, or processing content streams.
 *
 * <br><br>
 * The methods herein were developed to allow easy access to the Velocity
 * facilities without direct spelunking of the internals.  If there is
 * something you feel is necessary to add here, please, send a patch.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="Christoph.Reck@dlr.de">Christoph Reck</a>
 * @author <a href="jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: Velocity.java,v 1.23.2.1 2001/12/09 19:28:24 geirm Exp $
 */

public class Velocity implements RuntimeConstants
{
    /**
     *  initialize the Velocity runtime engine, using the default 
     *  properties of the Velocity distribution
     */
    public static void init() 
        throws Exception
    {
        RuntimeSingleton.init();
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the properties file passed in as the arg
     *
     *  @param propsFilename file containing properties to use to initialize 
     *         the Velocity runtime
     */
    public static void init( String propsFilename ) 
        throws Exception
    {
        RuntimeSingleton.init(propsFilename);
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the passed in java.util.Properties object
     *
     *  @param p  Proprties object containing initialization properties
     *
     */
    public static void init( Properties p )
        throws Exception
    {      
        RuntimeSingleton.init( p ); 
    }
    
    /**
     * Set a Velocity Runtime property.
     *
     * @param String key
     * @param Object value
     */
    public static void setProperty(String key, Object value)
    {
        RuntimeSingleton.setProperty(key,value);
    }

    /**
     * Add a Velocity Runtime property.
     *
     * @param String key
     * @param Object value
     */
    public static void addProperty(String key, Object value)
    {
        RuntimeSingleton.addProperty(key,value);
    }

    /**
     * Clear a Velocity Runtime property.
     *
     * @param key of property to clear
     */
    public static void clearProperty(String key)
    {
        RuntimeSingleton.clearProperty(key);
    }        

    /**
     * Set an entire configuration at once. This is
     * useful in cases where the parent application uses
     * the Configuration class and the velocity configuration
     * is a subset of the parent application's configuration.
     *
     * @param Configuration configuration
     *
     * @deprecated Use
     *  {@link #setExtendedProperties( ExtendedProperties  ) }
     */
    public static void setConfiguration(Configuration configuration)
    {
        /*
         *  Yuk. We added a little helper to Configuration to 
         *  help with deprecation.  The Configuration class
         *  contains a 'shadow' ExtendedProperties
         */

        ExtendedProperties ep = configuration.getExtendedProperties();

        RuntimeSingleton.setConfiguration( ep );
    }

    /**
     * Set an entire configuration at once. This is
     * useful in cases where the parent application uses
     * the ExtendedProperties class and the velocity configuration
     * is a subset of the parent application's configuration.
     *
     * @param ExtendedProperties configuration
     *
     */
    public static void setExtendedProperties( ExtendedProperties configuration)
    {
        RuntimeSingleton.setConfiguration( configuration );
    }

    /**
     *  Get a Velocity Runtime property.
     *
     *  @param key property to retrieve
     *  @return property value or null if the property
     *        not currently set
     */
    public static Object getProperty( String key )
    {
        return RuntimeSingleton.getProperty( key );
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
     */
    public static  boolean evaluate( Context context,  Writer out,  
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
     *  @param out  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log messages
     *                 in case of error
     *  @param instream input stream containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see 
     *               Velocity runtime log
     *  @deprecated Use
     *  {@link #evaluate( Context context, Writer writer, 
     *      String logTag, Reader reader ) }
     */
    public static boolean evaluate( Context context, Writer writer, 
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
            encoding = RuntimeSingleton.getString(INPUT_ENCODING,ENCODING_DEFAULT);
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
     *  @param out  Writer in which to render the output
     *  @param logTag  string to be used as the template name for log messages
     *                 in case of error
     *  @param reader Reader containing the VTL to be rendered
     *
     *  @return true if successful, false otherwise.  If false, see 
     *               Velocity runtime log
     *
     *  @since Velocity v1.1
     */
    public static boolean evaluate( Context context, Writer writer, 
                                    String logTag, Reader reader )
        throws ParseErrorException, MethodInvocationException, 	
        	ResourceNotFoundException,IOException
    {
        SimpleNode nodeTree = null;
        
        try
        {
            nodeTree = RuntimeSingleton.parse( reader, logTag );        
        }
        catch ( ParseException pex )
        {
            throw  new ParseErrorException( pex.getMessage() );
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
                    nodeTree.init( ica, RuntimeSingleton.getRuntimeServices() );
                }
                catch( Exception e )
                {
                    RuntimeSingleton.error("Velocity.evaluate() : init exception for tag = " 
                                  + logTag + " : " + e );
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
     *  @param logTag string to identify as 'template' in logging
     *  @param params[] args used to invoke Velocimacro. In context key format : 
     *                  eg  "foo","bar" (rather than "$foo","$bar")
     *  @param context Context object containing data/objects used for rendering.
     *  @param writer  Writer for output stream
     *  @return true if Velocimacro exists and successfully invoked, false otherwise.
     */
    public static  boolean invokeVelocimacro( String vmName, String logTag, 
                                              String params[], Context context, 
                                              Writer writer )
    {
        /*
         *  check parms
         */

        if ( vmName == null ||  params == null ||  context == null 
             || writer == null || logTag == null)
        {
            RuntimeSingleton.error( "Velocity.invokeVelocimacro() : invalid parameter");
            return false;
        }

        /*
         * does the VM exist?
         */
          
        if (!RuntimeSingleton.isVelocimacro( vmName, logTag ))
        {
            RuntimeSingleton.error( "Velocity.invokeVelocimacro() : VM '"+ vmName 
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
        catch( Exception  e )
        {
            RuntimeSingleton.error( "Velocity.invokeVelocimacro() : error " + e );
        }
        
        return false;
    }

    /**
     *  merges a template and puts the rendered stream into the writer
     *
     *  @param templateName name of template to be used in merge
     *  @param context  filled context to be used in merge
     *  @param  writer  writer to write template into
     *
     *  @return true if successful, false otherwise.  Errors 
     *           logged to velocity log.
     *  @deprecated Use
     *  {@link #mergeTemplate( String templateName, String encoding,
     *                Context context, Writer writer )}
     */
    public static boolean mergeTemplate( String templateName, 
                                         Context context, Writer writer )
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception
    {
        return mergeTemplate( templateName, RuntimeSingleton.getString(INPUT_ENCODING,ENCODING_DEFAULT),
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
     *
     *  @since Velocity v1.1
     */
    public static boolean mergeTemplate( String templateName, String encoding,
                                      Context context, Writer writer )
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception
    {
        Template template = RuntimeSingleton.getTemplate(templateName, encoding);
        
        if ( template == null )
        {
            RuntimeSingleton.error("Velocity.parseTemplate() failed loading template '" 
                          + templateName + "'" );
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
    public static Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate( name );
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
    public static Template getTemplate(String name, String encoding)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return RuntimeSingleton.getTemplate( name, encoding );
    }

    /**
     *   Determines if a template is accessable via the currently 
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
     *   @param templateName  name of the temlpate to search for
     *   @return true if found, false otherwise
     */
    public static boolean templateExists( String templateName )
    {
        return (RuntimeSingleton.getLoaderNameForResource(templateName) != null);
    }
    
    /**
     * Log a warning message.
     *
     * @param Object message to log
     */
    public static void warn(Object message)
    {
        RuntimeSingleton.warn( message );
    }
    
    /** 
     * Log an info message.
     *
     * @param Object message to log
     */
    public static void info(Object message)
    {
        RuntimeSingleton.info( message );
    }
    
    /**
     * Log an error message.
     *
     * @param Object message to log
     */
    public static void error(Object message)
    {
        RuntimeSingleton.error( message );
    }
    
    /**
     * Log a debug message.
     *
     * @param Object message to log
     */
    public static void debug(Object message)
    {
        RuntimeSingleton.debug( message );
    }

} 



