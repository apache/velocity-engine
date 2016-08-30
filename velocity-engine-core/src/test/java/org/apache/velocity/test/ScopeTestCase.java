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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.util.HashMap;

/**
 * This class tests the directive scope controls
 */
public class ScopeTestCase extends BaseTestCase
{
    public ScopeTestCase(String name)
    {
       super(name);
    }

    protected void setUpEngine(VelocityEngine engine)
    {
        engine.setProperty("a.provide.scope.control", "true");
        engine.setProperty("define.provide.scope.control", "true");
        engine.setProperty("evaluate.provide.scope.control", "true");
        engine.setProperty("foo.provide.scope.control", "true");
        engine.setProperty("macro.provide.scope.control", "true");
        engine.setProperty("template.provide.scope.control", "true");
        engine.setProperty("vm.provide.scope.control", "true");
        engine.setProperty("space.gobbling", "bc");
    }

    public void testScopeGetLeakIntoInner()
    {
        addTemplate("foo", "#foreach($i in [1..1])#set($foreach.a=$i)"+
                           "#foreach($j in [2..2])$foreach.a#set($foreach.a=$j)"+
                           "#foreach($k in [3..3])$foreach.a#end#end$foreach.a#end");
        assertTmplEquals("121", "foo");
    }

    public void testScopeGetLeakDoesntHideNullset()
    {
        addTemplate("a", "#macro(a)#set($macro.a='a')#b()$macro.a#end"+
                         "#macro(b)$macro.a#set($macro.a=$null)$!macro.a#end"+
                         "#a()");
        assertTmplEquals("aa", "a");
    }

    public void testRootTemplateMergeScope()
    {
        addTemplate("foo", "foo#break($template)bar");
        assertTmplEquals("foo", "foo");
        assertNull(context.get("template"));
    }

    public void testParseScope()
    {
        addTemplate("test", "$template.info.depth"+
                            "$!parse.parent.info.depth"+
                            "#set( $template.foo = 'bar' )"+
                            "$template.foo"+
                            "#break($template)"+
                            "woogie");
        assertEvalEquals("1bar", "#parse( 'test' )");
        assertNull(context.get("template"));
    }

    public void testNestedParseScope()
    {
        HashMap grab = new HashMap();
        context.put("grab", grab);

        addTemplate("inner", "Inner depth: $template.info.depth"+
                             "#set( $template.foo = '?' )"+
                             "$!grab.put('inner',$template)"+
                             "#break($template)$template.foo");
        addTemplate("outer", "#set( $template.foo = '!' )"+
                             "Outer depth: $template.info.depth "+
                             "#parse('inner')"+
                             "$!grab.put('outer', $template)"+
                             "$template.foo");
        assertEvalEquals("Outer depth: 1 Inner depth: 2!", "#parse('outer')");
        // make extra sure that the outer control was restored after the stop
        assertFalse(grab.get("inner") == grab.get("outer"));
        // make sure the outer control was cleaned up
        assertNull(context.get("template"));

        addTemplate("3", "$template.topmost.foo#set( $template.topmost.foo = 'bar' )");
        addTemplate("2", "#parse( '3' )$!parse.foo");
        addTemplate("1", "#set( $template.foo = 'foo' )#parse('2')$template.foo");
        assertEvalEquals("foobar", "#parse('1')$!parse");
        // make sure the top control was cleaned up
        assertNull(context.get("template"));
    }

    public void testForeachScope()
    {
        String template = "#foreach( $i in [0..2] )"+
                          "#if( $i > 1 )#break($foreach)#end"+
                          "$foreach.index:$foreach.count:$foreach.hasNext,"+
                          "#end";
        assertEvalEquals("0:1:true,1:2:true,", template);
        assertNull(context.get("foreach"));
    }

    public void testNestedForeachScope()
    {
        String template = "#foreach( $i in [1..5] )"+
                            "#foreach( $j in [1..2] )"+
                              "#if ( $i > $foreach.count + $foreach.index + $foreach.info.depth )#break($foreach.topmost)#end"+
                            "#end"+
                            "$i"+
                          "#end";
        assertEvalEquals("123", template);
        assertNull(context.get("foreach"));
    }

    public void testMacroScope()
    {
        String template = "#macro( foo $i )"+
                          "#if($i > 2 )#break($macro)#end"+
                          "$i#end"+
                          "#foo( 0 )#foo( 1 )#foo( 2 )";
        assertEvalEquals("012", template);
        assertNull(context.get("macro"));
    }

    public void testRecursiveMacroScope()
    {
        String template = "#macro( foo )$macro.info.depth"+
                          "#if($macro.info.depth > 2 )#break($macro.topmost)#end"+
                          "#foo()#end#foo()";
        assertEvalEquals("123", template);
        assertNull(context.get("macro"));
    }

    public void testNestedMacroScope()
    {
        String template = "#macro( a )$macro.info.depth#set($macro.c = 'a')$macro.c#end"+
                          "#macro( b )#set($macro.c = 'b' )#a()$macro.c#end"+
                          "#b()";
        assertEvalEquals("2ab", template);
        assertNull(context.get("macro"));
    }

    public void testBodyMacroScope()
    {
        String template = "#macro( foo $bar )$bodyContent$macro.bar#end"+
                          "#@foo( 'bar' )#set( $macro.bar = 'foo'+$bar )"+
                          "#set( $foo.d = $foo.info.depth )$foo.d #end";
        assertEvalEquals("1 foobar", template);
        assertNull(context.get("foo"));
        assertNull(context.get("macro"));
    }

    public void testRecursiveBodyMacroScope()
    {
        engine.setProperty(RuntimeConstants.VM_MAX_DEPTH, "5");
        String template = "#macro( foo )$bodyContent$macro.i#end"+
                          "#@foo()#set( $macro.i = \"$!macro.i$foo.info.depth,\" )"+
                          "$!bodyContent#end";
        assertEvalEquals("1,2,3,4,5,", template);
        assertNull(context.get("foo"));
        assertNull(context.get("macro"));
    }

    public void testDefineScope()
    {
        String template = "#define( $foo )#set( $define.bar = 'bar'+$define.info.depth )$define.bar#end$foo";
        assertEvalEquals("bar1", template);
        assertNull(context.get("define"));
    }

    public void testNestedDefineScope()
    {
        String template = "#define($a)$b c#end"+
                          "#define($b)$define.info.depth#break($define.topmost)#end"+
                          "$a";
        assertEvalEquals("2", template);
        assertNull(context.get("define"));
    }

    public void testRecursiveDefineScope()
    {
        engine.setProperty(RuntimeConstants.DEFINE_DIRECTIVE_MAXDEPTH, "10");
        String template = "#define($a)$define.info.depth"+
                          "#if($define.info.depth == 5)#break($define)#end,$a#end$a";
        assertEvalEquals("1,2,3,4,5", template);
        assertNull(context.get("define"));
    }

    public void testRootEvaluateScope()
    {
        assertEvalEquals("1", "$evaluate.info.depth");
        assertEvalEquals("foo", "foo#break($evaluate)bar");
        assertNull(context.get("evaluate"));
    }

    public void testEvaluateScope()
    {
        context.put("h", "#");
        context.put("d", "$");
        String template = "${h}set( ${d}evaluate.foo = 'bar' )"+
                          "${d}evaluate.foo ${d}evaluate.info.depth";
        addTemplate("eval", "#evaluate(\""+template+"\")");
        assertTmplEquals("bar 1", "eval");
        assertNull(context.get("evaluate"));
        assertNull(context.get("template"));
    }

    public void testNestedEvaluateScope()
    {
        context.put("h", "#");
        context.put("d", "$");
        addTemplate("e", "#evaluate(\"${h}evaluate( '${d}evaluate.info.depth${h}stop(${d}evaluate) blah' )\")");
        assertTmplEquals("2", "e");
        assertNull(context.get("evaluate"));
        assertNull(context.get("template"));
    }

    public void testTurningOffTemplateScope()
    {
        engine.setProperty("template."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, "false");
        // root
        addTemplate("test", "$template.info.depth");
        assertTmplEquals("$template.info.depth", "test");
        // #parse
        assertEvalEquals("$template.info.depth", "#parse('test')");
    }

    public void testTurningOffEvaluateScope()
    {
        engine.setProperty("evaluate."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, "false");
        // root
        assertSchmoo("$evaluate.info.depth");
        // #evaluate
        assertEvalEquals("$evaluate.info.depth", "#evaluate( '$evaluate.info.depth' )");
    }

    public void testTurningOffMacroScope()
    {
        engine.setProperty("macro."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, "false");
        engine.setProperty("foo."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, "false");
        // macro definition
        assertEvalEquals("$macro", "#macro(a)$macro#end#a()");
        // macro body
        assertEvalEquals("$macro $foo", "#macro(foo)$bodyContent#end#@foo()$macro $foo#end");
    }

    public void testTurningOffDefineScope()
    {
        engine.setProperty("define."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, "false");
        assertEvalEquals("$define", "#define($a)$define#end$a");
    }

    public void testTurningOffForeachScope()
    {
        engine.setProperty("foreach."+RuntimeConstants.PROVIDE_SCOPE_CONTROL, "false");
        assertEvalEquals("$foreach$foreach", "#foreach($i in [0..1])$foreach#end");
    }

    public void testTemplateReplaced()
    {
        context.put("template", "foo");
        addTemplate("test", "$template.replaced");
        assertTmplEquals("foo", "test");
        assertEvalEquals("foo", "#parse('test')");
        assertContextValue("template", "foo");
    }

    public void testEvaluateReplaced()
    {
        context.put("evaluate","foo");
        assertEvalEquals("foo", "$evaluate.replaced");
        assertEvalEquals("foo", "#evaluate('$evaluate.replaced')");
        assertContextValue("evaluate", "foo");
    }

    public void testMacroReplaced()
    {
        context.put("macro", "foo");
        assertEvalEquals("foo foo foo", "$macro #macro(a)$macro.replaced#end#a() $macro");
        assertContextValue("macro", "foo");
    }

    public void testForeachReplaced()
    {
        context.put("foreach", "foo");
        assertEvalEquals("foofoofoo", "$foreach#foreach($i in [1..1])$foreach.replaced#end$foreach");
        assertEquals("foo", context.get("foreach"));
        context.put("foreach", "a");
        assertEvalEquals("a", "#foreach($i in [1..1])#foreach($j in [1..1])$foreach.replaced#end#end");
        assertContextValue("foreach", "a");
    }

    public void testDefineReplaced()
    {
        context.put("define", "a");
        assertEvalEquals("a", "#define($a)$define.replaced#end$a");
        assertContextValue("define", "a");
    }

    public void testBodyContentReplaced()
    {
        context.put("vm", "a");
        assertEvalEquals("a", "#macro(vm)$bodyContent#end#@vm()$vm.replaced#end");
        assertContextValue("vm", "a");
    }

    public void testInfoDepth()
    {
        String template = "#foreach($i in [1..1])"+
                            "#foreach($j in [0..0])"+
                                "$foreach.info.depth"+
                            "#end"+
                          "#end";
        assertEvalEquals("2", template);
    }

    public void testInfoName()
    {
        String template = "#foreach($i in [1..1])"+
                            "$foreach.info.name #evaluate('$evaluate.info.name')"+
                          "#end";
        assertEvalEquals("foreach evaluate", template);
    }

    public void testInfoType()
    {
        addTemplate("info", "#foreach($i in [1..1])"+
                                "$foreach.info.type"+
                            "#end "+
                            "#evaluate('$evaluate.info.type') "+
                            "$template.info.type");
        assertTmplEquals("block line utf-8", "info");
    }

    public void testInfoLineAndColumn()
    {
        String template = " #evaluate('$evaluate.info.line, $evaluate.info.column')";
        assertEvalEquals(" 1, 2", template);
        assertEvalEquals("\n\n   3, 4", "\n\n  "+template);
    }

    public void testInfoTemplate()
    {
        addTemplate("test", "#evaluate('$evaluate.info.template')");
        assertTmplEquals("test", "test");
        assertEvalEquals("test", "#parse('test')");
    }

}
