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

import org.apache.velocity.test.BaseEvalTestCase;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * This class tests VELOCITY-681.
 */
public class Velocity681TestCase extends BaseEvalTestCase
{
    public Velocity681TestCase(String name)
    {
        super(name);
        //DEBUG = true;
    }

    public void testIt()
    {
        String template = "#macro(myMacro $result)"+
                          "  #set($result = 'woogie')"+
                          "#end"+
                          "#myMacro($x)"+
                          "$x";
        assertEvalEquals("woogie", template);
    }

    public void testConstant()
    {
        String template = "#macro(myMacro $result)"+
                            "#set($result = 'woogie')"+
                            "$result"+
                          "#end"+
                          "#myMacro('foo')";
        assertEvalEquals("woogie", template);
    }

    public void testReadOnlyProperty()
    {
        context.put("foo", new Foo());
        String template = "#macro(myMacro $result)"+
                            "#set($result = 'woogie')"+
                            "$result"+
                          "#end"+
                          "#myMacro($foo.bar)"+
                          "$foo.bar";
        assertEvalEquals("barbar", template);
    }

    public void testReadWriteProperty()
    {
        context.put("foo", new FooRW());
        String template = "#macro(myMacro $result)"+
                            "#set($result = 'woogie')"+
                            "$result"+
                          "#end"+
                          "#myMacro($foo.bar)"+
                          "$foo.bar";
        assertEvalEquals("woogiewoogie", template);
    }

    public static class Foo
    {
        public String getBar()
        {
            return "bar";
        }
    }

    public static class FooRW
    {
        private String bar = "bar";

        public String getBar()
        {
            return bar;
        }

        public void setBar(String bar)
        {
            this.bar = bar;
        }
    }

}
