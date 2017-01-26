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
 * This class tests the BlockMacro functionality.
 */
public class BlockMacroTestCase extends BaseTestCase
{
    public BlockMacroTestCase(String name)
    {
        super(name);
    }

    public void testMultipleBodyContentIncludes() throws Exception
    {
        String template = "#macro(foo $txt) Yeah, $txt! $bodyContent $bodyContent#end #@foo(\"woohoo\")jee#end";
        String result = "  Yeah, woohoo! jee jee";

        assertEvalEquals(result, template);
    }

    public void testNestedVelocityLogic() throws Exception
    {
        String template = "#macro(foo $txt) Yeah, $txt! $bodyContent#end #@foo(\"woohoo\")#foreach($i in [1..3])$i:#{end}#end";
        String result = "  Yeah, woohoo! 1:2:3:";

        assertEvalEquals(result, template);
    }

    public void testEmptyBody() throws Exception
    {
        String template = "#macro(foo $txt) Yeah, $txt! $bodyContent#end #@foo(\"woohoo\")#end";
        String result = "  Yeah, woohoo! ";

        assertEvalEquals(result, template);
    }

    public void testNoArgumentsEmptyBodyCall() throws Exception
    {
        String template = "#macro(foo) Yeah! $bodyContent#end #@foo()#end";
        String result = "  Yeah! ";

        assertEvalEquals(result, template);
    }

    public void testCustomBodyReference() throws Exception
    {
        engine.setProperty(RuntimeConstants.VM_BODY_REFERENCE, "myBody");
        String template = "#macro(foo) Yeah! $myBody#end #@foo()#end";
        String result = "  Yeah! ";

        assertEvalEquals(result, template);
    }

    public void testVelocity671() throws Exception
    {
        engine.setProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        String template = "#macro(echo)$bodyContent#end #@echo()Yeah!#end";
        String result = " Yeah!";
        assertEvalEquals(result, template);
    }

    public void testStrict()
    {
        engine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
        assertEvalException("#@foo#end");
        assertEvalException("#@foo()#end");
    }

    public void testVelocity690()
    {
        assertEvalEquals(" output ", "#macro(foo) output #end#@foo #end");
        assertEvalEquals("#[ output )", "#macro(foo2)#[$bodyContent)#end#@foo2 output #end");
        assertEvalEquals("#[output)", "#macro(foo2)#[$bodyContent)#end#{@foo2}output#end");
        assertEvalException("#macro(foo) output #end#@foo");
    }

    public void testVelocity675() throws Exception
    {
      assertEvalEquals("#@foo#end", "#@foo#end");
    }

    public void testVelocity685() throws Exception
    {
        engine.setProperty(RuntimeConstants.VM_ARGUMENTS_STRICT, Boolean.TRUE);
        assertEvalEquals(" ", "#macro(foo)#end #@foo() junk #end");
    }

    public void testVelocity686() throws Exception
    {
        String template = "#macro(foo)#set( $x = $bodyContent )#end"+
                          "#@foo()b#end a $x ";
        assertEvalEquals(" a b ", template);
    }

    public void testNestedBlockMacro()
    {
        String template = "#macro(foo)foo:$bodyContent#end"+
                          "#macro(bar)bar:$bodyContent#end"+
                          "#@foo()foo,#@bar()bar#end#end";
        assertEvalEquals("foo:foo,bar:bar", template);
    }

    public void testRecursiveBlockMacro()
    {
        engine.setProperty(RuntimeConstants.VM_MAX_DEPTH, 3);
        String template = "#macro(foo)start:$bodyContent#end"+
                          "#@foo()call:$bodyContent#end";
        assertEvalEquals("start:call:call:call:$bodyContent", template);
    }

    public void testBlueJoesProblem()
    {
        engine.setProperty("macro."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, Boolean.TRUE);
        addTemplate("a", "#macro(wrap $layout)$!macro.put($layout,$bodyContent)#parse($layout)#end"+
                         "#@wrap('b')a#end");
        addTemplate("b", "#@wrap('c')b$!macro.get('b')b#end");
        addTemplate("c", "c$!macro.get('c')c");
        assertTmplEquals("cbabc", "a");
    }
}
