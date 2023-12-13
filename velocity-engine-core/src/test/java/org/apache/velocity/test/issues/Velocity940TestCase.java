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

    @Test
    public void testBlockInsideBlock()
    {
        assertEvalEquals("Something First Something Second Somewhere", "#macro(test $label)Something $!label $!bodyContent#{end}#@test('First')#@test('Second')Somewhere#end#end");
    }

    @Test
    public void testBlockInsideBlockBodyRestored()
    {
        assertEvalEquals("Something First Something Second Somewhere After Somewhere After Something Second Somewhere After Somewhere", "#macro(test $label)Something $!label $!bodyContent After $!bodyContent#{end}#@test('First')#@test('Second')Somewhere#end#end");
    }

    @Test
    public void testBlockInsideBody()
    {
        assertEvalEquals("Something First Somehow Inside With Outside Someone Objectively Hidden", "#macro(test2 $label)Somehow $!label With $!bodyContent Someone#end#macro(test $label)Something $!label #@test2('Inside')Outside#end Objectively $!bodyContent#end#@test('First')Hidden#end");
    }
}
