package org.apache.velocity.test.misc;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import org.apache.velocity.app.FieldMethodizer;
import org.apache.velocity.app.Velocity;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.provider.TestProvider;

import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;

import org.apache.velocity.context.Context;

/**
 * This class the testbed for Velocity. It is used to
 * test all the directives support by Velocity.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Test.java,v 1.31 2001/08/07 22:20:29 geirm Exp $
 */
public class Test implements ReferenceInsertionEventHandler, 
                             NullSetEventHandler,
                             MethodExceptionEventHandler
{
    /**
     * Cache of writers
     */
    private static Stack writerStack = new Stack();

    public Test(String templateFile, String encoding)
    {
        Writer writer = null;
        TestProvider provider = new TestProvider();
        ArrayList al = provider.getCustomers();
        Hashtable h = new Hashtable();
  
        /*
         *  put this in to test introspection $h.Bar or $h.get("Bar") etc
         */
        
        h.put("Bar", "this is from a hashtable!");
        h.put("Foo", "this is from a hashtable too!");
       
        /*
         *  adding simple vector with strings for testing late introspection stuff
         */

        Vector v = new Vector();

        String str = "mystr";

        v.addElement( new String("hello") );
        v.addElement( new String("hello2") );
        v.addElement( str );

        try
        {
            /*
             *  this is another way to do properties when initializing Runtime.
             *  make a Properties 
             */

            Properties p = new Properties();

            /*
             *  now, if you want to, load it from a file (or whatever)
             */
            
            try
            {
                FileInputStream fis =  new FileInputStream( 
                    new File("velocity.properties" ));
            
                if( fis != null)
                    p.load( fis );
            }
            catch (Exception ex)
            {
                /* no worries. no file... */
            }

            /*
             *  iterate out the properties
             */

            for( Enumeration e = p.propertyNames(); e.hasMoreElements(); )
            {
                String el = (String) e.nextElement();

                Velocity.setProperty( el, p.getProperty( el ) );
            }

            /*
             *  add some individual properties if you wish
             */


            Velocity.setProperty(Velocity.RUNTIME_LOG_ERROR_STACKTRACE, "true");
            Velocity.setProperty(Velocity.RUNTIME_LOG_WARN_STACKTRACE, "true");
            Velocity.setProperty(Velocity.RUNTIME_LOG_INFO_STACKTRACE, "true");

            /*
             *  use an alternative logger.  Set it up here and pass it in.
             */
            
            //            SimpleLogSystem sls = new SimpleLogSystem("velocity_simple.log");
            
            // Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, sls );
          
            /*
             *  and now call init
             */

            Velocity.init();

            /*
             *  now, do what we want to do.  First, get the Template
             */

            if (templateFile == null)
            {
                templateFile = "examples/example.vm";
            }                
         

            Template template = null;

            try 
            {
                template = RuntimeSingleton.getTemplate(templateFile, encoding);
            }
            catch( ResourceNotFoundException rnfe )
            {
                System.out.println("Test : RNFE : Cannot find template " + templateFile );
            }
            catch( ParseErrorException pee )
            {
                System.out.println("Test : Syntax error in template " + templateFile + ":" + pee );
            }

            /*
             * now, make a Context object and populate it.
             */

            VelocityContext context = new VelocityContext();

            context.put("provider", provider);
            context.put("name", "jason");
            context.put("providers", provider.getCustomers2());
            context.put("list", al);
            context.put("hashtable", h);
            context.put("search", provider.getSearch());
            context.put("relatedSearches", provider.getRelSearches());
            context.put("searchResults", provider.getRelSearches());
            context.put("menu", provider.getMenu());
            context.put("stringarray", provider.getArray());
            context.put("vector", v);
            context.put("mystring", new String());
            context.put("hashmap", new HashMap() );
            context.put("runtime", new FieldMethodizer( "org.apache.velocity.runtime.RuntimeSingleton" ));
            context.put("fmprov", new FieldMethodizer( provider ));
            context.put("Floog", "floogie woogie");
            context.put("geirstring", str );
            context.put("mylong", new Long(5) );


            /*
             *  we want to make sure we test all types of iterative objects
             *  in #foreach()
             */

            Object[] oarr = { "a","b","c","d" } ;
            
            context.put( "collection", v );
            context.put("iterator", v.iterator());
            context.put("map", h );
            context.put("obarr", oarr );

            String stest = " My name is $name -> $Floog";
            StringWriter w = new StringWriter();
            //            Velocity.evaluate( context, w, "evaltest",stest );
            //            System.out.println("Eval = " + w );

            w = new StringWriter();
            //Velocity.mergeTemplate( "mergethis.vm",  context, w );
            //System.out.println("Merge = " + w );

            w = new StringWriter();
            //Velocity.invokeVelocimacro( "floog", "test", new String[2],  context,  w );
            //System.out.println("Invoke = " + w );


            /*
             *  event cartridge stuff
             */

            EventCartridge ec = new EventCartridge();
            ec.addEventHandler(this);
            ec.attachToContext( context );

            /*
             *  make a writer, and merge the template 'against' the context
             */

            VelocityContext vc = new VelocityContext( context );

            if( template != null)
            {
                writer = new BufferedWriter(new OutputStreamWriter(System.out, encoding));
                template.merge( vc , writer);
                writer.flush();
                writer.close();
            }
 
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("MIE : " + mie );
        }
        catch( Exception e )
        {
            RuntimeSingleton.error( "Test- exception : " + e);
            e.printStackTrace();

        }
    }

    public Object referenceInsert( String reference, Object value  )
    {
        if (value != null)
            ; // System.out.println("Woo! referenceInsert : " + reference + " = " + value.toString() );
        return value;
    }

    public boolean shouldLogOnNullSet( String lhs, String rhs )
    {
        //        System.out.println("Woo2! nullSetLogMessage : " + lhs + " :  RHS = " + rhs);

        if (lhs.equals("$woogie"))
            return false;
        
        return true;
    }

   public Object methodException( Class claz, String method, Exception e )
         throws Exception
    {
        if (method.equals("getThrow"))
            return "I should have thrown";

        throw e;
    }


    public static void main(String[] args)
    {
        Test t;

        String encoding = "ISO-8859-1";

        if( args.length > 1 )
            encoding = args[1];

        t = new Test(args[0], encoding);
    }
}







