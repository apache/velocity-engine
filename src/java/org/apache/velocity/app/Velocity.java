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

import org.apache.velocity.context.Context;
import org.apache.velocity.Template;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.VelocimacroProxy;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

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
 * @version $Id: Velocity.java,v 1.8 2001/03/19 00:57:56 geirm Exp $
 */

public class Velocity implements RuntimeConstants
{
    /**
     *  initialize the Velocity runtime engine, using the default 
     *  properties of the Velocity distribution
     *
     *  @return true if successful, false otherwise
     */
    public static void init() 
        throws Exception
    {
        Runtime.init();
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the properties file passed in as the arg
     *
     *  @param propsFilename file containing properties to use to initialize 
     *         the Velocity runtime
     *
     *  @return true if successful, false otherwise.  Check runtime log if 
     *         false
     */
    public static void init( String propsFilename ) 
        throws Exception
    {
        Runtime.init(propsFilename);
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the passed in java.util.Properties object
     *
     *  @param p  Proprties object containing initialization properties
     *
     *  @return true if successful, false otherwise
     */
    public static void init( Properties p )
        throws Exception
    {      
        Runtime.init( p ); 
    }
    
    /**
     * Set a Velocity Runtime property.
     *
     * @param String key
     * @param Object value
     */
    public static void setProperty(String key, Object value)
    {
        Runtime.setProperty(key,value);
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
        return Runtime.getProperty( key );
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
    {
        ByteArrayInputStream inStream = 
            new ByteArrayInputStream( instring.getBytes() );

        return evaluate( context, out, logTag, inStream );
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
     */
    public static boolean evaluate( Context context, Writer writer, 
                                    String logTag, InputStream instream )
    {
        SimpleNode nodeTree = null;

        try
        {
            nodeTree = Runtime.parse( instream, logTag );        
 
            if (nodeTree != null)
            {
                InternalContextAdapterImpl ica = 
                    new InternalContextAdapterImpl( context );
                
                ica.pushCurrentTemplateName( logTag );
                nodeTree.init( ica, null );
                
                try
                {
                    nodeTree.render( ica, writer );
                }
                finally
                {
                    ica.popCurrentTemplateName();
                }
                       
                return true;
            }
        }
        catch( Exception e )
        {
            Runtime.error("Velocity.evaluate() : tag = " 
                          + logTag + " : " + e );
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
     *  @param params[] args used to invoke Velocimacro. In context key format : 
     *                  eg  "foo","bar" (rather than "$foo","$bar")
     *  @param context Context object containing data/objects used for rendering.
     *  @param writer  Writer for output stream
     *  @return true if Velocimacro exists and successfully invoked, false otherwise.
     */
    public static  boolean invokeVelocimacro( String vmName, String namespace, 
                                              String params[], Context context, 
                                              Writer writer )
    {
        /*
         *  check parms
         */

        if ( vmName == null ||  params == null ||  context == null 
             || writer == null || namespace == null)
        {
            Runtime.error( "Velocity.invokeVelocimacro() : invalid parameter");
            return false;
        }

        /*
         * does the VM exist?
         */
          
        if (!Runtime.isVelocimacro( vmName, namespace ))
        {
            Runtime.error( "Velocity.invokeVelocimacro() : VM '"+ vmName 
                           + "' not registered.");
            return false;
        }

        /*
         * apparently.  Ok, make one..
         */
           
        VelocimacroProxy vp = 
            (VelocimacroProxy) Runtime.getVelocimacro( vmName, namespace );
        
        if ( vp == null )
        {
            Runtime.error( "Velocity.invokeVelocimacro() : VM '" 
                           + vmName 
                           + "' : severe error.  Unable to get VM from factory.");
            return false;
        }
  
        /*
         * if we get enough args?
         */
            
        if ( vp.getNumArgs() > params.length )
        {
            Runtime.error( "Velocity.invokeVelocimacro() : VM '" 
                           + vmName + "' : invalid # of args.  Needed " 
                           + vp.getNumArgs() 
                           + " but called with " + params.length);
            return false;
        }

        /*
         *  ok.  setup the vm
         */

        /*
         *  fix the parms : since we don't require the $ from the caller, 
         *  we need to add it
         */

        int [] types = new int[vp.getNumArgs()];
        String[] p = new String[vp.getNumArgs()];
 
        for( int i = 0; i < types.length; i++)
        {
            types[i] = ParserTreeConstants.JJTREFERENCE;
            p[i] = "$" + params[i]; 
        }

        vp.setupMacro( p, types  );
      
        try
        {
            InternalContextAdapterImpl ica 
                = new InternalContextAdapterImpl( context );
            
            try
            {
                ica.pushCurrentTemplateName( namespace );
                vp.render( ica, writer, null);
            }
            finally
            {
                ica.popCurrentTemplateName();
            }
        }
        catch (Exception e )
        {
            Runtime.error("Velocity.invokeVelocimacro() : " + e );
            return false;
        }
        
        return true;
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
     */
    public static boolean mergeTemplate( String templateName, 
                                         Context context, Writer writer )
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        try
        {
            Template template = Runtime.getTemplate(templateName);

            if ( template == null )
            {
                Runtime.error("Velocity.parseTemplate() failed loading template '" 
                              + templateName + "'" );
            }
            else
            {
                template.merge(context, writer);
            }

            return true;
        }
        catch( ResourceNotFoundException rnfe )
        {
        }
        catch( Exception e )
        {
            Runtime.error("Velocity.parseTemplate() with " 
                          + templateName + " : " + e );
        }

        return false;
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
        return Runtime.getTemplate( name );
    }


    /**
     *   Determines if a template is accessable via the currently 
     *   configured resource loaders.
     *   <br><br>
     *   The assumption is that the caller will use it at some
     *   point so that the current implementation, while 
     *   displaying lazyness and sloth, isn't entirely 
     *   unreasonable.
     *
     *   @param templateName  name of the temlpate to search for
     *   @return true if found, false otherwise
     */
    public static boolean templateExists( String templateName )
    {
        try
        {
            Template t = Runtime.getTemplate( templateName );
            return true;
        }
        catch( Exception e )
        {
            return false;
        }
    }
} 
