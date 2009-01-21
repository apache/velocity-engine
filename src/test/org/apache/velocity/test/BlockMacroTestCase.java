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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogChute;
import org.apache.velocity.test.provider.ForeachMethodCallHelper;

/**
 * This class tests the BlockMacro functionality.
 */
public class BlockMacroTestCase extends BaseEvalTestCase
{
    public BlockMacroTestCase(String name)
    {
        super(name);
        // DEBUG = true;
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

}
