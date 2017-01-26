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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Test strict reference mode turned on by the velocity property
 * runtime.references.strict
 */
public class StrictReferenceTestCase extends BaseTestCase
{
    public StrictReferenceTestCase(String name)
    {
        super(name);
    }

    // second engine to test WITH conversions
    VelocityEngine engine2;

    public void setUp() throws Exception
    {
        super.setUp();

        /* first engine without conversions */
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
        engine.setProperty(RuntimeConstants.CONVERSION_HANDLER_CLASS, "none");

        /* second engine with conversions */
        engine2 = createEngine();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);

        context.put("NULL", null);
        context.put("bar", null);
        context.put("TRUE", Boolean.TRUE);
    }

    /**
     * Test the modified behavior of #if in strict mode.  Mainly, that
     * single variables references in #if statements use non strict rules
     */
    public void testIfStatement()
    {
        Fargo fargo = new Fargo();
        fargo.next = new Fargo();
        context.put("fargo", fargo);
        assertEvalEquals("", "#if($bogus)xxx#end");
        assertEvalEquals("xxx", "#if($fargo)xxx#end");
        assertEvalEquals("", "#if( ! $fargo)xxx#end");
        assertEvalEquals("xxx", "#if($bogus || $fargo)xxx#end");
        assertEvalEquals("", "#if($bogus && $fargo)xxx#end");
        assertEvalEquals("", "#if($fargo != $NULL && $bogus)xxx#end");
        assertEvalEquals("xxx", "#if($fargo == $NULL || ! $bogus)xxx#end");
        assertEvalEquals("xxx", "#if(! $bogus1 && ! $bogus2)xxx#end");
        assertEvalEquals("xxx", "#if($fargo.prop == \"propiness\" && ! $bogus && $bar == $NULL)xxx#end");
        assertEvalEquals("", "#if($bogus && $bogus.foo)xxx#end");

        assertMethodEx("#if($bogus.foo)#end");
        assertMethodEx("#if(!$bogus.foo)#end");
    }


    /**
     * We make sure that variables can actuall hold null
     * values.
     */
    public void testAllowNullValues()
        throws Exception
    {
        evaluate("$!bar");
        assertEvalEquals("true", "#if($bar == $NULL)true#end");
        assertEvalEquals("true", "#set($foobar = $NULL)#if($foobar == $NULL)true#end");
        assertEvalEquals("13", "#set($list = [1, $NULL, 3])#foreach($item in $list)#if($item != $NULL)$item#end#end");
    }

    /**
     * Test that variables references that have not been defined throw exceptions
     */
    public void testStrictVariableRef()
        throws Exception
    {
        // We expect a Method exception on the following
        assertMethodEx("$bogus");
        assertMethodEx("#macro(test)$bogus#end #test()");

        assertMethodEx("#set($bar = $bogus)");

        assertMethodEx("#if($bogus == \"bar\") #end");
        assertMethodEx("#if($bogus != \"bar\") #end");
        assertMethodEx("#if(\"bar\" == $bogus) #end");
        assertMethodEx("#if($bogus > 1) #end");
        assertMethodEx("#foreach($item in $bogus)#end");

        // make sure no exceptions are thrown here
        evaluate("#set($foo = \"bar\") $foo");
        evaluate("#macro(test1 $foo1) $foo1 #end #test1(\"junk\")");
        evaluate("#macro(test2) #set($foo2 = \"bar\") $foo2 #end #test2()");
    }

    /**
     * Test that exceptions are thrown when methods are called on
     * references that contains objects that do not contains those
     * methods.
     */
    public void testStrictMethodRef()
    {
        Fargo fargo = new Fargo();
        fargo.next = new Fargo();
        context.put("fargo", fargo);

        // Mainly want to make sure no exceptions are thrown here
        assertEvalEquals("propiness", "$fargo.prop");
        assertEvalEquals("", "$!fargo.nullVal");
        assertEvalEquals("propiness", "$fargo.next.prop");

        assertMethodEx("$fargo.foobar");
        assertMethodEx("$fargo.next.foobar");
        assertMethodEx("$fargo.foobar()");
        assertMethodEx("#set($fargo.next.prop = $TRUE)");
        assertMethodEx("$fargo.next.setProp($TRUE)");
    }

    /**
     * Make sure exceptions are thrown when when we attempt to call
     * methods on null values.
     */
    public void testStrictMethodOnNull()
    {
        Fargo fargo = new Fargo();
        fargo.next = new Fargo();
        context.put("fargo", fargo);

        assertVelocityEx("$NULL.bogus");
        assertVelocityEx("$fargo.nullVal.bogus");
        assertVelocityEx("$fargo.next.nullVal.bogus");
        assertVelocityEx("#if (\"junk\" == $fargo.nullVal.bogus)#end");
        assertVelocityEx("#if ($fargo.nullVal.bogus > 2)#end");
        assertVelocityEx("#set($fargo.next.nullVal.bogus = \"junk\")");
        assertVelocityEx("#set($foo = $NULL.bogus)");
        assertVelocityEx("#foreach($item in $fargo.next.nullVal.bogus)#end");

        evaluate("$fargo.prop.toString()");
        assertVelocityEx("#set($fargo.prop = $NULL)$fargo.prop.next");

        // make sure no exceptions are thrown here
        evaluate("$!fargo.next.next");
        evaluate("$!fargo.next.nullVal");
        evaluate("#foreach($item in $fargo.nullVal)#end");
    }

    /**
     * Make sure undefined macros throw exceptions
     */
    public void testMacros()
    {
        assertVelocityEx("#bogus()");
        assertVelocityEx("#bogus (  )");
        assertVelocityEx("#bogus( $a )");
        assertVelocityEx("abc#bogus ( $a )a ");

        assertEvalEquals(" true ", "#macro(test1) true #end#test1()");
        assertEvalEquals(" true ", "#macro(test2 $a) $a #end#test2 ( \"true\")");
        assertEvalEquals("#CCFFEE", "#CCFFEE");
        assertEvalEquals("#F - ()", "#F - ()");
        assertEvalEquals("#F{}", "#F{}");
    }


    public void testRenderingNull()
    {
        Fargo fargo = new Fargo();
        fargo.next = new Fargo();
        context.put("fargo", fargo);

        assertVelocityEx("#set($foo = $NULL)$foo");
        assertEvalEquals("", "#set($foo = $NULL)$!foo");
        assertVelocityEx("$fargo.nullVal");
        assertEvalEquals("", "$!fargo.nullVal");
        assertVelocityEx("$fargo.next.next");
        assertEvalEquals("", "$!fargo.next.next");
        assertVelocityEx("$fargo.next.nullVal");
        assertEvalEquals("", "$!fargo.next.nullVal");
    }

    /**
     * Assert that we get a MethodInvocationException when calling evaluate
     */
    public void assertMethodEx(String template)
    {
        assertEvalException(template, MethodInvocationException.class);
    }

    /**
     * Assert that we get a VelocityException when calling evaluate
     */
    public void assertVelocityEx(String template)
    {
        assertEvalException(template, VelocityException.class);
    }

    /**
     * Assert that we get a MethodInvocationException when calling evaluate
     */
    public void assertParseEx(String template)
    {
        assertEvalException(template, ParseErrorException.class);
    }


    public static class Fargo
    {
        String prop = "propiness";
        Fargo next = null;

        public String getProp()
        {
            return prop;
        }

        public void setProp(String val)
        {
            this.prop = val;
        }

        public String getNullVal()
        {
            return null;
        }

        public Fargo getNext()
        {
            return next;
        }
    }
}
