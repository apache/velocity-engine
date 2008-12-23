package org.apache.velocity.test;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Make sure exceptions are thrown for strict comparisons that
 * cannot be compared.
 */
public class StrictCompareTestCase extends BaseEvalTestCase
{
    public StrictCompareTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
        context.put("NULL", null);
        context.put("a", "abc");
        context.put("b", new Integer(3));
        context.put("TRUE", Boolean.TRUE);
    }

    public void testCompare()
    {
        assertVelocityEx("#if($a > $NULL)#end");
        assertVelocityEx("#if($a < $NULL)#end");
        assertVelocityEx("#if($a >= $NULL)#end");
        assertVelocityEx("#if($a <= $NULL)#end");

        assertVelocityEx("#if($NULL > $a)#end");
        assertVelocityEx("#if($NULL < $a)#end");
        assertVelocityEx("#if($NULL >= $a)#end");        
        assertVelocityEx("#if($NULL <= $a)#end");

        assertVelocityEx("#if($NULL >= $NULL)#end");
        assertVelocityEx("#if($a >= $b)#end");
        assertVelocityEx("#if($a <= $b)#end");
        assertVelocityEx("#if($a > $b)#end");
        assertVelocityEx("#if($a < $b)#end");

        assertVelocityEx("#if($a < 5)#end");
        assertVelocityEx("#if($a > 5)#end");        
    }
    
    /**
     * Assert that we get a VelocityException when calling evaluate
     */
    public void assertVelocityEx(String template)
    {
      System.out.println(template);
        assertEvalException(template, VelocityException.class);
    }
}
