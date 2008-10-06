package org.apache.velocity.test;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Test strict reference mode turned on by the velocity property
 * runtime.references.strict
 */
public class StrictReferenceTestCase extends BaseEvalTestCase
{
    public StrictReferenceTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
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
        evaluate("$bar");
        assertEvalEquals("true", "#if($bar == $NULL)true#end");
        assertEvalEquals("false", "#set($foobar = $NULL)#if(!$foobar)false#end");
        assertEvalEquals("13", "#set($list = [1, $NULL, 3])#foreach($item in $list)#if($item)$item#end#end");
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
        assertEvalEquals("$fargo.nullVal", "$fargo.nullVal");
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

        assertMethodEx("$NULL.bogus");
        assertMethodEx("$fargo.nullVal.bogus");
        assertMethodEx("$fargo.next.nullVal.bogus");
        assertMethodEx("#if (\"junk\" == $fargo.nullVal.bogus)#end");
        assertMethodEx("#if ($fargo.nullVal.bogus > 2)#end");
        assertMethodEx("#set($fargo.next.nullVal.bogus = \"junk\")");
        assertMethodEx("#set($foo = $NULL.bogus)");
        assertMethodEx("#foreach($item in $fargo.next.nullVal.bogus)#end");

        evaluate("$fargo.prop.toString()");
        assertMethodEx("#set($fargo.prop = $NULL)$fargo.prop.next");

        // make sure no exceptions are thrown here
        evaluate("$fargo.next.next");
        evaluate("$fargo.next.nullVal");
        evaluate("#foreach($item in $fargo.nullVal)#end");
    }
        
    /**
     * Assert that we get a MethodInvocationException when calling evaluate
     */
    public void assertMethodEx(String template)
    {
        assertEvalException(template, MethodInvocationException.class);
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
            this.prop = prop;
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
