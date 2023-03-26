package org.apache.velocity.test.issues;

import org.apache.velocity.test.BaseTestCase;
import org.junit.Test;

public class Velocity940TestCase extends BaseTestCase
{
    public Velocity940TestCase(String name)
    {
        super(name);
    }

    @Test
    public void testNonBlockInsideBlock()
    {
        assertEvalEquals("Something First Something Second ", "#macro(test $label)Something $!label $!bodyContent#{end}#@test('First')#test('Second')#end");
    }

    public void testBlockInsideBlock()
    {
        assertEvalEquals("Something First Something Second Somewhere", "#macro(test $label)Something $!label $!bodyContent#{end}#@test('First')#@test('Second')Somewhere#end#end");
    }

}
