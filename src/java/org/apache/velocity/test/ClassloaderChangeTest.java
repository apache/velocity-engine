package org.apache.velocity.test;

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

import java.lang.ClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.VelocityContext;

import org.apache.velocity.runtime.log.LogSystem;

import org.apache.velocity.util.introspection.Introspector;

import junit.framework.TestCase;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ClassloaderChangeTest.java,v 1.1 2001/09/16 23:55:32 geirm Exp $
 */
public class ClassloaderChangeTest extends TestCase implements LogSystem
{
    private VelocityEngine ve = null;
    private boolean sawCacheDump = false;
    
    private static String OUTPUT = "Hello From Foo";
    
    
    /**
     * Default constructor.
     */
    public ClassloaderChangeTest()
    {
        super("ClassloaderChangeTest");

        try
        {
            /*
             *  use an alternative logger.  Set it up here and pass it in.
             */
            
            ve = new VelocityEngine();
            ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this );
            ve.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup ClassloaderChnageTest : " + e);
            System.exit(1);
        }            
    }

    public void init( RuntimeServices rs )
    {
        // do nothing with it
    }

    public static junit.framework.Test suite ()
    {
        return new ClassloaderChangeTest();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        sawCacheDump = false;
                        
        try
        {
            VelocityContext vc = new VelocityContext();
            Object foo = null;

            /*
             *  first, we need a classloader to make our foo object
             */

            TestClassloader cl = new TestClassloader();
            Class fooclass = cl.loadClass("Foo");
            foo = fooclass.newInstance();

            /*
             *  put it into the context
             */
            vc.put("foo", foo);
        
            /*
             *  and render something that would use it
             *  that will get it into the introspector cache
             */
            StringWriter writer = new StringWriter();
            ve.evaluate( vc, writer, "test", "$foo.doIt()");

            /*
             *  Check to make sure ok.  note the obvious
             *  dependency on the Foo class...
             */
             
            if ( !writer.toString().equals( OUTPUT ))
            {
               fail("Output from doIt() incorrect");
            }
             
            /*
             * and do it again :)
             */
            cl = new TestClassloader();
            fooclass = cl.loadClass("Foo");
            foo = fooclass.newInstance();
            
            vc.put("foo", foo);
        
            writer = new StringWriter(); 
            ve.evaluate( vc, writer, "test", "$foo.doIt()");

            if ( !writer.toString().equals( OUTPUT ))
            {
               fail("Output from doIt() incorrect");
            }   
        }
        catch( Exception ee )
        {
            System.out.println("ClassloaderChangeTest : " + ee );
        }   
        
        if (!sawCacheDump)
        {
            fail("Didn't see introspector cache dump.");
        }
    }

    /**
     *  method to catch Velocity log messages.  When we
     *  see the introspector dump message, then set the flag
     */
    public void logVelocityMessage(int level, String message)
    {
        if (message.equals( Introspector.CACHEDUMP_MSG) )
        {
            sawCacheDump = true;
        }       
    }
}

/**
 *  Simple (real simple...) classloader that depends
 *  on a Foo.class being located in the classloader
 *  directory under test
 */
class TestClassloader extends ClassLoader
{
    private final static String testclass = 
        "../test/classloader/Foo.class";
        
    private Class fooClass = null;
    
    public TestClassloader()
    {
        try
        {
            File f = new File( testclass );
            
            byte[] barr = new byte[ (int) f.length() ];
                 
            FileInputStream fis = new FileInputStream( f );
            fis.read( barr );
            fis.close();
        
            fooClass = defineClass("Foo", barr, 0, barr.length);
        }
        catch( Exception e )
        {
            System.out.println("TestClassloader : exception : " + e );
        }        
    }
    
    
    public Class findClass(String name)     
    {
        return fooClass;
    }
}