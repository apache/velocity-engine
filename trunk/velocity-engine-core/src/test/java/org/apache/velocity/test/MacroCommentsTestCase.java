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

import org.apache.velocity.exception.ParseErrorException;

/**
 * Test Macro comment functionality
 */
public class MacroCommentsTestCase extends BaseTestCase
{
    public MacroCommentsTestCase(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void testComments()
    {
      assertEvalEquals("ab","#macro(foo ##\n $bar \n ## blaa\n $bar2##\n)$bar$bar2#end#foo(\"a\" \"b\")");
      assertEvalEquals("","#macro(foo1##\n)#end#foo1()");
      assertEvalEquals("ab","#macro(foo2##\n\t ####\r $bar \n ##\n## Testing  blaa\n $bar2##\n)$bar$bar2#end#foo2(\"a\" \"b\")");
      assertEvalEquals("","#macro(foo4 ## test\n  ## test2  ## test3 \n)#end#foo4()");
      assertEvalEquals("","#macro(foo4 ## test\n  $x = 5 ## test2  ## test3 \n)#end#foo4()");
    }

    public void testErrors()
    {
      // We only allow comment lines in macro definitions
      assertEvalException("#foo1(## test)", ParseErrorException.class);
      assertEvalException("#foo1($test ## test)", ParseErrorException.class);
      assertEvalException("#break(## test)", ParseErrorException.class);
    }
}
