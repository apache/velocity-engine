package org.apache.velocity.test;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 * @version $Id: ClassloaderChangeTest.java,v 1.1.10.1 2004/03/03 23:23:04 geirm Exp $
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