package org.apache.velocity.test.issues;

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

import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-615.
 */
public class Velocity615TestCase extends BaseTestCase
{
    public Velocity615TestCase(String name)
    {
       super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty("velocimacro.permissions.allow.inline", "true");
        engine.setProperty("velocimacro.permissions.allow.inline.to.replace.global", "false");
        engine.setProperty("velocimacro.permissions.allow.inline.local.scope", "true");
        engine.setProperty("velocimacro.arguments.strict", "true");
        engine.setProperty("space.gobbling", "bc");
    }

    public void testIt()
    {
        String template = "#set( $foo = 'old' )"+
                          "#macro( test $foo )"+
                            "#set( $foo = \"new $foo \" )"+
                            "$foo"+
                          "#end"+
                          "#test( 'foo' )"+
                          "$foo";
        assertEvalEquals("new foo new foo ", template);
    }

    public void testForIrrationallyFearedRelatedPossibleProblem()
    {
        context.put("i", new Inc());
        String template = "#macro( test $a )"+
                            "$a"+
                            "$a"+
                          "#end"+
                          "#test( \"$i\" )$i";
        assertEvalEquals("001", template);
    }

    public void testForIrrationallyFearedRelatedPossibleProblem2()
    {
        context.put("i", new Inc());
        String template = "#macro( test $a )"+
                            "#set( $a = 'a' )"+
                            "$a"+
                            "$a"+
                          "#end"+
                          "#test( \"$i\" )$i";
        assertEvalEquals("aa1", template);
    }

    public void testForIrrationallyFearedRelatedPossibleProblem3()
    {
        context.put("i", new Inc());
        String template = "#macro( test $a )"+
                            "$a"+
                            "$a"+
                          "#end"+
                          "#test( $i )$i";
        assertEvalEquals("012", template);
    }

    public void testForIrrationallyFearedRelatedPossibleProblem4()
    {
        context.put("i", new Inc());
        String template = "#macro( test $a )"+
                            "$a"+
                            "$a"+
                          "#end"+
                          "#test( $i.plus() )$i";
        assertEvalEquals("001", template);
    }

    public void testForIrrationallyFearedRelatedPossibleProblem5()
    {
        context.put("i", new Inc());
        String template = "#macro( test $a )"+
                            "#set( $a = $i )"+
                            "$a"+
                            "$a"+
                          "#end"+
                          "#test( 'a' )$i";
        assertEvalEquals("012", template);
    }

    public void testVelocity681()
    {
        String template = "#macro(myMacro $result)"+
                          "  #set($result = 'some value')"+
                          "#end"+
                          "#myMacro($x)"+
                          "$x";
        assertEvalEquals("$x", template);
    }

    public static class Inc
    {
        private int i=0;

        public int plus()
        {
            return i++;
        }

        public String toString()
        {
            return String.valueOf(i++);
        }
    }

}
