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

import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Test macro default parameters.
 */
public class MacroDefaultArgTestCase extends BaseTestCase
{
    public MacroDefaultArgTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
        engine.setProperty(RuntimeConstants.VM_ARGUMENTS_STRICT, Boolean.TRUE);
        engine.setProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT_ESCAPE, Boolean.TRUE);
    }

    public void testCompare()
    {
      assertEvalEquals("121", "#macro(foo $a=1)$a#end#foo()#foo(2)#foo");
      assertEvalEquals("12", "#macro(foo $a = 1)$a#end#foo()#foo(2)");
      assertEvalEquals("12", "#macro(foo $a= 1 )$a#end#foo()#foo(2)");
      assertEvalEquals("1x2x", "#macro(foo $a= 1 $b = \"x\")$a$b#end#foo()#foo(2)");
      assertEvalEquals("1 2 5 2 5 [1, 2] ", "#macro(foo $a=1 $b=2)$a $b #end#foo()#foo(5)#foo(5 [1,2])");
      assertEvalEquals("1 2 5 2 5 [1, 2] ", "#macro(foo $a=1 , $b=2)$a $b #end#foo()#foo(5)#foo(5 [1,2])");
      assertEvalEquals("1 2 5 2 5 [1, 2] ", "#macro(foo, $a=1\n $b =2 )$a $b #end#foo()#foo(5)#foo(5 [1,2])");

      assertEvalEquals("3 2 5 2 5 [1, 2] ", "#macro(foo, $a=$x $b =2 )$a $b #end#set($x=3)#foo()#foo(5)#foo(5 [1,2])");
      assertEvalEquals("{a=3} 2 5 2 5 [1, 2] ", "#macro(foo, $a = {\"a\":$x} $b =2 )$a $b #end#set($x=3)#foo()#foo(5)#foo(5 [1,2])");

      assertEvalEquals("3 2 5 2 5 [1, 2] ", "#macro(foo, $a = \"$x\" $b =2 )$a $b #end#set($x=3)#foo()#foo(5)#foo(5 [1,2])");
      assertEvalEquals("3$y 2 5 2 5 [1, 2] ", "#macro(foo, $a = \"$x\\$y\" $b =2 )$a $b #end#set($x=3)#foo()#foo(5)#foo(5 [1,2])");
      assertEvalEquals("5 3 2 5 [1, 2] 2 ", "#macro(foo, $c $a = \"$x\" $b =2 )$c $a $b #end#set($x=3)#foo(5)#foo(5 [1,2])");

      assertEvalEquals("1xno2xyes", "#macro(foo $a= 1 $b = \"x\")$a$b$bodyContent#end#@foo()no#end#@foo(2)yes#end");

      assertEvalEquals("xy", "#macro(foo $a=\"$b$c\"##\n)$a#end#set($b=\"x\")#set($c=\"y\")#foo()");
    }

    public void testErrors()
    {
      assertEvalException("#macro(foo $a = 1 $b)#end");
      assertEvalException("#macro(foo $c $a = 3 $b)#end");
      assertEvalException("#macro(foo $a $b = 1)#end#foo()");  // Too few arguments
      assertEvalException("#macro(foo $a $b $c = 4)#end#foo(1)");  // Too few arguments
      assertEvalException("#macro(foo $a = 3)#end#foo(2 3)"); // Too many arguments
    }
}
