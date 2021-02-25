package org.apache.velocity.test.issues;

import org.apache.velocity.test.BaseTestCase;

public class Velocity919TestCase extends BaseTestCase
{
    public Velocity919TestCase(String name)
    {
        super(name);
    }

    public void testUnbreakableSpace() throws Exception
    {
        assertEvalEquals("before\u200Bafter", "before\u200Bafter");
    }

    public void testUserFileSeparator() throws Exception
    {
        assertEvalEquals("before\u001Cafter", "before\u001Cafter");
    }

}

