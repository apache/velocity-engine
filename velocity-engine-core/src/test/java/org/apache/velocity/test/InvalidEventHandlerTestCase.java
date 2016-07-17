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

import java.io.StringWriter;
import java.io.Writer;

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
    // @@ VELOCITY-553
    public class TestObject {
        private String nullValueAttribute = null;
        
        public String getNullValueAttribute() {
            return nullValueAttribute;
        }	

        public String getRealString() {
            return new String("helloFooRealStr");
        }	
        
        public String getString() {
            return new String("helloFoo");
        }

        public String getNullString() {
            return null;
        }	
        
        public java.util.Date getNullDate() {
            return null;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TestObject [nullValueAttribute=");
            builder.append(nullValueAttribute);
            builder.append("]");
            return builder.toString();
        }
    }
    // @@ VELOCITY-553

    
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
        
        doTestInvalidReferenceEventHandler0(ve, inner);
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
        doTestInvalidReferenceEventHandler0(ve, null);
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
        ve.evaluate(context, w, "mystring", s);
        
        s = "$tree.x $tree.field.x $tree.child.y $tree.child.Field.y";
        w = new StringWriter();
        ve.evaluate(context, w, "mystring", s);
        
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
        context.put("a1", new Integer(5));
        context.put("a4", new Integer(5));
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
        context.put("a4",new Integer(5));
        context.put("b1","abc");
        
        String s;
        Writer w;
        
        // good object, bad method
        s = "$a1.afternoon()";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // good object, bad method, quiet reference
        s = "$!a1.afternoon()";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("", w.toString());

        // bad object, bad method -- fails on get
        s = "$zz.daylight()";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // bad object, bad method, quiet reference
        s = "$!zz.daylight()";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("", w.toString());

        // change result
        s = "$b1.baby()";
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
        context.put("a4",new Integer(5));
        context.put("b1","abc");
        
        // normal - should be no calls to handler
        String s = "$a1 $a1.intValue() $b1 $b1.length() #set($c1 = '5')";
        Writer w = new StringWriter();
        ve.evaluate(context, w, "mystring", s);
        
        // good object, bad property
        s = "$a1.foobar";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // same one as a quiet reference should not fail
        s = "$!a1.foobar";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("",w.toString());

        // same one inside an #if statement should not fail
        s = "#if($a1.foobar)yes#{else}no#end";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("no",w.toString());


        // bad object, bad property            
        s = "$a2.foobar";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // same one as a quiet reference should not fail
        s = "$!a2.foobar";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("",w.toString());

        // same one inside an #if statement should still fail
        s = "#if($a2.foobar)yes#{else}no#end";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // except if object is tested first
        s = "#if($a2 and $a2.foobar)yes#{else}no#end";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        assertEquals("no", w.toString());

        // bad object, no property            
        s = "$a3";
        w = new StringWriter();
        try {
            ve.evaluate( context, w, "mystring", s );
            fail("Expected exception.");
        } catch (RuntimeException e) {}

        // bad object, no property as quiet reference should not fail
        s = "$!a3";
        w = new StringWriter();
        ve.evaluate(context, w, "mystring", s);
        result = w.toString();
        assertEquals("", result);

        // bad object, no property as #if condition should not fail
        s = "#if($a3)yes#{else}no#end";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        result = w.toString();
        assertEquals("no", result);

        // good object, bad property; change the value
        s = "$a4.foobar";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );
        result = w.toString();
        assertEquals("zzz", result);
        
    }

    /**
     * Test invalidGetMethod
     *
     * Test behaviour (which should be the same) of
     * $objRef.myAttribute and $objRef.getMyAttribute()
     *
     * @param ve
     * @param vc
     * @throws Exception
     */
    private void doTestInvalidReferenceEventHandler0(VelocityEngine ve, VelocityContext vc)
            throws Exception
    {
        String result;
        Writer w;
        String s;
        boolean rc;

        VelocityContext context = new VelocityContext(vc);
        context.put("propertyAccess", new String("lorem ipsum"));
        context.put("objRef", new TestObject());
        java.util.ArrayList arrayList = new java.util.ArrayList();
        arrayList.add("firstOne");
        arrayList.add(null);
        java.util.HashMap hashMap = new java.util.HashMap();
        hashMap.put(41, "41 is not 42");

        context.put("objRefArrayList", arrayList);
        context.put("objRefHashMap", hashMap);

        // good object, good property (returns non null value)
        s = "#set($resultVar = $propertyAccess.bytes)"; // -> getBytes()
        w = new StringWriter();
        rc = ve.evaluate( context, w, "mystring", s );

        // good object, good property accessor method (returns non null value)
        s = "#set($resultVar = $propertyAccess.getBytes())"; // -> getBytes()
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        // good object, good property (returns non null value)
        s = "$objRef.getRealString()";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        // good object, good property accessor method (returns null value)
        // No exception shall be thrown, as returning null should be valid
        s = "$objRef.getNullValueAttribute()";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        // good object, good property (returns null value)
        // No exception shall be thrown, as returning null should be valid
        s = "$objRef.nullValueAttribute";   // -> getNullValueAttribute()
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        // good object, good accessor method which returns a non-null object reference
        // Test removing a hashmap element which exists
        s = "$objRefHashMap.remove(41)";
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );


        // good object, good accessor method which returns null
        // Test removing a hashmap element which DOES NOT exist
        // Expected behaviour: Returning null as a value should be
        // OK and not result in an exception
        s = "$objRefHashMap.remove(42)";   // will return null, as the key does not exist
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

        // good object, good method invocation (returns non-null object reference)
        s = "$objRefArrayList.get(0)";   // element 0 is NOT NULL
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );


        // good object, good method invocation (returns null value)
        // Expected behaviour: Returning null as a value should be
        // OK and not result in an exception
        s = "$objRefArrayList.get(1)";   // element 1 is null
        w = new StringWriter();
        ve.evaluate( context, w, "mystring", s );

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
            
            // bad object, bad property            
            else if (reference.equals("$a2"))
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
                assertEquals("$a1.afternoon()",reference);
                assertEquals("afternoon",method);
                throw new RuntimeException("expected exception");
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
