
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

/**
 * This class provides an interface to accessing Velocity functionality
 * in ways different from the usual template parse/merge model. 
 *
 * It is intended to mediate between applications and the Velocity core
 * services.  Use this.  Try to avoid hitting Runtime directly.
 *
 * NOTE THAT YOU DON'T NEED TO USE THE init() METHODS IN HERE TO 
 * USE THE OTHER FUNCTIONS.  THIS IS CURRENTLY A WORK IN PROGRESS.
 * SLOW PROGRESS :)
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Velocity.java,v 1.2 2001/02/02 11:57:56 geirm Exp $
 */

package org.apache.velocity.util;

import java.io.Writer;
import java.util.Hashtable;
import java.util.Properties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.directive.VelocimacroProxy;

public class Velocity
{
    boolean initialized = false;

    /**
     *  initialize the Velocity runtime engine, using default properties
     */
    public boolean init()
    {
        if (initialized)
            return true;

        try
        {
            Runtime.init("");
            initialized = true;
        }
        catch(Exception e ) 
        {
            System.out.println("Velocity.init() : Velocity init exception : " + e);
            initialized = false;
        }

        return initialized;

    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the properties file passed in
     */
    public boolean init( String propsFilename )
    {
        if (initialized)
            return true;

        try
        {
            Runtime.init( propsFilename );
            initialized = true;
        }
        catch(Exception e ) 
        {
            System.out.println("Velocity.init( filename ) : Velocity init exception : " + e);
            initialized = false;
        }

        return initialized;
    }

    /**
     *  initialize the Velocity runtime engine, using default properties
     *  plus the properties in the passed in j.u.Properties object
     */
    public boolean init( Properties p )
    {
        if (initialized)
            return true;

        try
        {
            Runtime.init( p );
            initialized = true;

        }
        catch(Exception e ) 
        {
            System.out.println("Velocity.init( props ) : Velocity init exception : " + e);
            initialized = false;
        }

        return initialized;
    }


    /**
     *  renders the input
     *
     */

    public static  boolean evaluate( Context context,  Writer out, String logTag, String instring )
    {
        ByteArrayInputStream inStream = new ByteArrayInputStream( instring.getBytes() );
        return evaluate( context, out, logTag, inStream );
    }

    public static boolean evaluate( Context context, Writer writer, String logTag, InputStream instream )
    {
        SimpleNode nodeTree = null;

        try
        {
            nodeTree = Runtime.parse( instream, logTag );        
 
            if (nodeTree != null)
            {
                InternalContextAdapterImpl ica = new InternalContextAdapterImpl( context );
                ica.setCurrentTemplateName( logTag );
                nodeTree.init( ica, null );
                nodeTree.render( ica, writer );
                return true;
            }
        }
        catch( Exception e )
        {
            Runtime.error("Velocity.evaluate() : tag = " + logTag + " : " + e );
        }

        return false;
    }

    /**
     *  Invokes a currently registered Velocimacro with the parms provided
     *  and places the rendered stream into the writer.
     *
     *  @param vmName name of Velocimacro to call
     *  @param params[] args used to invoke Velocimacro. In context format : eg  "foo","bar"
     *  @param context Context object containing data/objects used for rendering.
     *  @param writer  Writer for output stream
     *  @return true if Velocimacro exists and successfully invoked, false otherwise.
     */
    public  boolean invokeVelocimacro( String vmName, String namespace, String params[], Context context, Writer writer )
    {
        if (!initialized)
            return false;

        /*
         *  check parms
         */

        if ( vmName == null ||  params == null ||  context == null || writer == null || namespace == null)
        {
            Runtime.error( "Velocity.invokeVelocimacro() : invalid parameter");
            return false;
        }

        /*
         * does the VM exist?
         */
            
        if (!Runtime.isVelocimacro( vmName, namespace ))
        {
            Runtime.error( "Velocity.invokeVelocimacro() : VM '"+ vmName+"' not registered.");
            return false;
        }

        /*
         * apparently.  Ok, make one..
         */
           
        VelocimacroProxy vp = (VelocimacroProxy) Runtime.getVelocimacro( vmName, namespace );
        
        if ( vp == null )
        {
            Runtime.error( "Velocity.invokeVelocimacro() : VM '" + vmName + "' : severe error.  Unable to get VM from factory.");
            return false;
        }
 
        /*
         * if we get enough args?
         */
            
        if ( vp.getNumArgs() > params.length )
        {
            Runtime.error( "Velocity.invokeVelocimacro() : VM '" + vmName + "' : invalid # of args.  Needed " 
                           + vp.getNumArgs() + " but called with " + params.length);
            return false;
        }

        /*
         *  ok.  setup the vm
         */

        /*
         *  fix the parms : since we don't require the $ from the caller, we need to add it
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
            InternalContextAdapterImpl ica = new InternalContextAdapterImpl( context );
            
            ica.setCurrentTemplateName( namespace );

            vp.render( ica, writer, null);
        }
        catch (Exception e )
        {
            Runtime.error("Velocity.invokeVelocimacro() : " + e );
            return false;
        }

        return true;
    }

    /**
     *  Parses a template and returns the rendered stream into the writer
     */
    public static boolean parseTemplate( String templateName, Context context, Writer writer )
    {
        return false;
    }

} 


