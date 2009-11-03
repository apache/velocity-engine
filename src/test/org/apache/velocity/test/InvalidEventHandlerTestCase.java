package org.apache.velocity.test;

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

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;

/**
 * Tests event handling for all event handlers except IncludeEventHandler.  This is tested
 * separately due to its complexity.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class InvalidEventHandlerTestCase
extends TestCase
{
    /**
     * Default constructor.
     */
    public InvalidEventHandlerTestCase(String name)
    {
        super(name);
    }
    
    public static Test suite ()
    {
        return new TestSuite(InvalidEventHandlerTestCase.class);
    }
    
    public void testManualEventHandlers()
    throws Exception
    {
        TestEventCartridge te = new TestEventCartridge();
        
        /**
         * Test attaching the event cartridge to the context
         */
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        
        /*
         *  lets make a Context and add the event cartridge
         */
        
        VelocityContext inner = new VelocityContext();
        
        /*
         *  Now make an event cartridge, register all the
         *  event handlers (at once) and attach it to the
         *  Context
         */
        
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(te);
        ec.attachToContext( inner );
        
        doTestInvalidReferenceEventHandler1(ve, inner);
        doTestInvalidReferenceEventHandler2(ve, inner);
        doTestInvalidReferenceEventHandler3(ve, inner);
        doTestInvalidReferenceEventHandler4(ve, inner);
    }

    /**
     * Test assigning the event handlers via properties
     */

    public void testConfigurationEventHandlers()
            throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.EVENTHANDLER_INVALIDREFERENCES, TestEventCartridge.class.getName());

        ve.init();
        doTestInvalidReferenceEventHandler1(ve, null);
        doTestInvalidReferenceEventHandler2(ve, null);
        doTestInvalidReferenceEventHandler3(ve, null);
        doTestInvalidReferenceEventHandler4(ve, null);
    }

    /**
     * Test deeper structures
     * @param ve
     * @param vc
     * @throws Exception
     */
    private void doTestInvalidReferenceEventHandler4(VelocityEngine ve, VelocityContext vc)
    throws Exception
    {
        VelocityContext context = new VelocityContext(vc);

        Tree test = new Tree();
        test.setField("10");
        Tree test2 = new Tree();
        test2.setField("12");
        test.setChild(test2);
        
        context.put("tree",test);
        String s;
        Writer w;
        
        // show work fine
        s = "$tree.Field $tree.field $tree.child.Field";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        
        s = "$tree.x $tree.field.x $tree.child.y $tree.child.Field.y";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        
    }
    
    /**
     * Test invalid #set
     * @param ve
     * @param vc
     * @throws Exception
     */
    private void doTestInvalidReferenceEventHandler3(VelocityEngine ve, VelocityContext vc)
    throws Exception
    {
        VelocityContext context = new VelocityContext(vc);
        context.put("a1",new Integer(5));
        context.put("a4",new Integer(5));
        context.put("b1","abc");
        
        String s;
        Writer w;
        
        // good object, bad right hand side
        s = "#set($xx = $a1.afternoon())";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // good object, bad right hand reference
        s = "#set($yy = $q1)";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
    }

    /**
     * Test invalid method calls
     * @param ve
     * @param vc
     * @throws Exception
     */
    private void doTestInvalidReferenceEventHandler2(VelocityEngine ve, VelocityContext vc)
    throws Exception
    {
        VelocityContext context = new VelocityContext(vc);
        context.put("a1",new Integer(5));
        context.put("b1",new Integer(5));
        context.put("a4",new Integer(5));
        context.put("b4",new Integer(5));
        context.put("z1","abc");
        
        String s;
        Writer w;
        
        // good object, bad method
        s = "$a1.afternoon()";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // good object, bad method
        s = "$!b1.afternoon()";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // bad object, bad method -- fails on get
        s = "$zz.daylight()";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // change result
        s = "$z1.baby()";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("www",w.toString());        
    }
    
    /**
     * Test invalid gets/references
     * @param ve
     * @param vc
     * @throws Exception
     */
    private void doTestInvalidReferenceEventHandler1(VelocityEngine ve, VelocityContext vc)
    throws Exception
    {
        String result;
        
        VelocityContext context = new VelocityContext(vc);
        context.put("a1",new Integer(5));
        context.put("b1",new Integer(5));
        context.put("a4",new Integer(5));
        context.put("b4",new Integer(5));
        context.put("z1","abc");
        
        // normal - should be no calls to handler
        String s = "$a1 $a1.intValue() $z1 $z1.length() #set($c1 = '5')";
        Writer w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        
        // good object, bad property
        s = "$a1.foobar";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // good object, bad property / silent
        s = "$!b1.foobar";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // bad object, bad property            
        s = "$a2.foobar";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // bad object, bad property / silent            
        s = "$!b2.foobar";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // bad object, no property            
        s = "$a3";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // bad object, no property / silent            
        s = "$!b3";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}
        
        // good object, bad property; change the value
        s = "$a4.foobar";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        result = w.toString();
        assertEquals("zzz", result);

    }
    
    

    /**
     * Test assigning the event handlers via properties
     */
    
    public static class TestEventCartridge
    implements InvalidReferenceEventHandler,
    RuntimeServicesAware
    {
        private RuntimeServices rs;
        
        public TestEventCartridge()
        {
        }
        
        /**
         * Required by EventHandler
         */
        public void setRuntimeServices( RuntimeServices rs )
        {
            // make sure this is only called once
            if (this.rs == null)
                this.rs = rs;
            
            else
                fail("initialize called more than once.");
        }
        
        
        public Object invalidGetMethod(Context context, String reference, Object object, String property, Info info)
        {
            // as a test, make sure this EventHandler is initialized
            if (rs == null)
                fail ("Event handler not initialized!");
            
            // good object, bad property
            if (reference.equals("$a1.foobar"))
            {
                assertEquals(new Integer(5),object);
                assertEquals("foobar",property);
                throw new RuntimeException("expected exception");
            }
            
            // good object, bad property
            else if (reference.equals("$!b1.foobar"))
            {
                assertEquals(new Integer(5),object);
                assertEquals("foobar",property);
                throw new RuntimeException("expected exception");
            }
            
            // good object, bad property
            else if (reference.equals("$a1.foobar"))
            {
                assertEquals(new Integer(5),object);
                assertEquals("foobar",property);
                throw new RuntimeException("expected exception");
            }
            
            // good object, bad property
            else if (reference.equals("$!b1.foobar"))
            {
                assertEquals(new Integer(5),object);
                assertEquals("foobar",property);
                throw new RuntimeException("expected exception");
            }
            
            // bad object, bad property            
            else if (reference.equals("$a2"))
            {
                assertNull(object);
                assertNull(property);
                throw new RuntimeException("expected exception");
            }

            // bad object, bad property            
            else if (reference.equals("$!b2"))
            {
                assertNull(object);
                assertNull(property);
                throw new RuntimeException("expected exception");
            }

            // bad object, no property            
            else if (reference.equals("$a3"))
            {
                assertNull(object);
                assertNull(property);
                throw new RuntimeException("expected exception");
            }
            
            // bad object, no property            
            else if (reference.equals("$!b3"))
            {
                assertNull(object);
                assertNull(property);
                throw new RuntimeException("expected exception");
            }
            
            // good object, bad property; change the value
            else if (reference.equals("$a4.foobar"))
            {
                assertEquals(new Integer(5),object);
                assertEquals("foobar",property);
                return "zzz";
            }

            // bad object, bad method -- fail on the object
            else if (reference.equals("$zz"))
            {
                assertNull(object);
                assertNull(property);
                throw new RuntimeException("expected exception");
            }

            // pass q1 through
            else if (reference.equals("$q1"))
            {

            }

            
            else if (reference.equals("$tree.x"))
            {
                assertEquals("x",property);
            }

            else if (reference.equals("$tree.field.x"))
            {
                assertEquals("x",property);
            }

            else if (reference.equals("$tree.child.y"))
            {
                assertEquals("y",property);
            }
            
            else if (reference.equals("$tree.child.Field.y"))
            {
                assertEquals("y",property);
            }
            
            else
            {
                fail("invalidGetMethod: unexpected reference: " + reference);
            }
            return null;
        }
        
        public Object invalidMethod(Context context, String reference, Object object, String method, Info info)
        {
            // as a test, make sure this EventHandler is initialized
            if (rs == null)
                fail ("Event handler not initialized!");

            // good reference, bad method
            if (object.getClass().equals(Integer.class))
            {
                if (reference.equals("$a1.afternoon()"))
                {
                    assertEquals("afternoon",method);
                    throw new RuntimeException("expected exception");                    
                }
                else if (reference.equals("$!b1.afternoon()"))
                {
                    assertEquals("afternoon",method);
                    throw new RuntimeException("expected exception");                    
                }
                else
                {
                    fail("Unexpected invalid method.  " + method);
                    
                }
            }


            else if (object.getClass().equals(String.class) && "baby".equals(method))
            {
                return "www";
            }

            else
            { 
                fail("Unexpected invalid method.  " + method);
            }

            return null;
        }        
    

        public boolean invalidSetMethod(Context context, String leftreference, String rightreference, Info info)
        {

            // as a test, make sure this EventHandler is initialized
            if (rs == null)
                fail ("Event handler not initialized!");

            // good object, bad method
            if (leftreference.equals("xx"))
            {
                assertEquals("q1.afternoon()",rightreference);
                throw new RuntimeException("expected exception");
            }
            if (leftreference.equals("yy"))
            {
                assertEquals("$q1",rightreference);
                throw new RuntimeException("expected exception");
            }
            else
            { 
                fail("Unexpected left hand side.  " + leftreference);
            }
            
            return false;
        }

    }

    public static class Tree
    {
        String field;
        Tree child;
        
        public Tree()
        {
            
        }

        public String getField()
        {
            return field;
        }

        public void setField(String field)
        {
            this.field = field;
        }

        public Tree getChild()
        {
            return child;
        }

        public void setChild(Tree child)
        {
            this.child = child;
        }

        public String testMethod() 
        {
            return "123";
        }
    }
    
}
