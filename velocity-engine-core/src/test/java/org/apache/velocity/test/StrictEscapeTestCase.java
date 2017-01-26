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
 * Test Strict escape mode
 * property: RuntimeConstants.RUNTIME_REFERENCES_STRICT_ESCAPE set to true
 */
public class StrictEscapeTestCase extends BaseTestCase
{
  public StrictEscapeTestCase(String name)
  {
      super(name);
  }

  public void setUp() throws Exception
  {
      super.setUp();
      engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT_ESCAPE, Boolean.TRUE);
      context.put("pow", "bang");
      context.put("NULL", null);
      context.put("animal", new Animal());
      // DEBUG = true;
  }

  public void testReferenceEscape()
  {
      engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);

      assertEvalException("\\\\$bogus");
      assertEvalException("\\\\\\\\$bogus");

      assertEvalEquals("$bogus", "\\$bogus");
      assertEvalEquals("$bogus.xyz", "\\$bogus.xyz");
      assertEvalEquals("${bogus}", "\\${bogus}");
      assertEvalEquals("${bogus.xyz}", "\\${bogus.xyz}");
      assertEvalEquals("\\$bogus", "\\\\\\$bogus");
      assertEvalEquals("\\xyz","#set($foo = \"xyz\")\\\\$foo");
      assertEvalEquals("\\$foo","#set($foo = \"xyz\")\\\\\\$foo");
      assertEvalEquals("$foo\\","#set($foo = \"xyz\")\\$foo\\");
      assertEvalEquals("$pow", "#set($foo = \"\\$pow\")$foo");
      assertEvalEquals("\\bang", "#set($foo = \"\\\\$pow\")$foo");
      assertEvalEquals("\\$pow", "#set($foo = \"\\\\\\$pow\")$foo");

      assertEvalEquals("\\$%", "\\$%");

      // This should work but does not... may be related to VELOCITY-679
      // This is broken from existing escape behavior
      // assertEvalEquals("\\$bang", "\\$$pow");

      assertEvalEquals("$!foo", "#set($foo = $NULL)\\$!foo");
      assertEvalEquals("$!animal.null", "\\$!animal.null");
      assertEvalEquals("$!animal", "\\$!animal");
  }

  public void testMacroEscape()
  {
      engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE);
      assertEvalException("\\\\#bogus()");

      // define the foo macro
      assertEvalEquals("", "#macro(foo)bar#end");

      assertEvalEquals("#foo()", "\\#foo()");
      assertEvalEquals("\\bar", "\\\\#foo()");
      assertEvalEquals("\\#foo()", "\\\\\\#foo()");

      assertEvalEquals("bar", "#set($abc = \"#foo()\")$abc");
      assertEvalEquals("#foo()", "#set($abc = \"\\#foo()\")$abc");
      assertEvalEquals("\\bar", "#set($abc = \"\\\\#foo()\")$abc");

      assertEvalEquals("#@foo()", "\\#@foo()");
      assertEvalEquals("#@foo", "\\#@foo");
      assertEvalEquals("#@bar", "\\#@bar");
      assertEvalEquals("\\bar", "\\\\#@foo()#end");
      assertEvalEquals("#@foo()#end", "\\#@foo()\\#end");
      assertEvalEquals("#@foo#end", "\\#@foo\\#end");
      assertEvalEquals("#@bar #end", "\\#@bar \\#end");

      assertEvalEquals("#end #foreach #define() #elseif", "\\#end \\#foreach \\#define() \\#elseif");
      assertEvalEquals("#{end} #{foreach} #{define}() #{elseif}", "\\#{end} \\#{foreach} \\#{define}() \\#{elseif}");
      assertEvalEquals("#macro(foo) #end", "\\#macro(foo) \\#end");

      assertEvalException("\\\\#end");
      assertEvalException("\\\\#if()");

      // This should work but does not, probably related to VELOCITY-678
      // this is broken from existing behavior
      //assertEvalEquals("\\$bar", "\\$#foo()");
  }

  /**
   * Tests for non strict-mode
   */
  public void testStrictMode()
  {
      assertEvalEquals("#bogus()", "\\#bogus()");
      assertEvalEquals("\\#bogus", "\\\\#bogus");

      assertEvalEquals("\\$bogus", "\\\\$bogus");
      assertEvalEquals("\\\\$bogus", "\\\\\\\\$bogus");
      assertEvalEquals("\\$bogus", "#set($foo = \"\\\\$bogus\")$foo");
  }

  /**
   * Test object for escaping
   */
  public static class Animal
  {
      public Object getNull()
      {
          return null;
      }

      public String toString()
      {
          return null;
      }
  }
}
