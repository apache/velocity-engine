package org.apache.velocity.test;

import junit.framework.TestSuite;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.junit.Test;

/**
 * Testing mutable and immutable integer ranges. Note that this test assumes that the test executions is not done in parallel.
 */

public class IntegerRangeMutabilityTest  extends BaseTestCase {

    public static junit.framework.Test suite()
    {
        return new TestSuite(IntegerRangeMutabilityTest.class);
    }

    public IntegerRangeMutabilityTest(String name) {
        super(name);
    }

    @Test
    public void testImmutableRange()
    {
        VelocityEngine ve = createEngine();
        ve.init();

        Exception e = assertEvalException("#set($range = [1..2])#set($range[0] = 4)", MethodInvocationException.class, ve);
        Throwable cause = e.getCause();
        assertNotNull(cause);
        assertEquals(UnsupportedOperationException.class, cause.getClass());
    }

    @Test
    public void testMutableRange()
    {
        VelocityEngine ve = createEngine();
        ve.setProperty(RuntimeConstants.IMMUTABLE_RANGES, false);
        ve.init();

        assertEvalEquals("4", "#set($range = [1..2])#set($range[0] = 4)$range[0]", ve);
    }
}
