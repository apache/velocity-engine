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

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-614.
 */
public class Velocity614TestCase extends BaseTestCase
{
    public Velocity614TestCase(String name)
    {
       super(name);
    }

    public void testSchmoo()
    {
        String template = "#something(Stuff)";
        assertEvalEquals(template, template);
    }

    public void testEscapeSchmooButNotReallySinceSchmooHasNoEscaping()
    {
        String template = "\\#something(Stuff)";
        assertEvalEquals(template, template);
    }

    public void testEscapeMacroWithBadArg()
    {
        String template = "#macro( evil $arg )$arg#end \\#evil(bar)";
        assertEvalEquals(" #evil(bar)", template);
    }

    public void testEarlyDefinedMacroWithBadArg()
    {
        // make sure this still bombs, but don't spam sysout
        log.off();
        assertEvalException("#macro( evil $arg )$arg#end #evil(bar)");
        log.on();
    }

    // just make sure this doesn't get broken
    public void testLateDefinedMacroWithGoodArg()
    {
        String good = "#good('bar') #macro( good $arg )$arg#end";
        assertEvalEquals("bar ", good);
    }

    public void testDirectivesWithBadArg()
    {
        // make sure these all still bomb, but don't spam sysout
        log.off();
        assertEvalException("#foreach(Stuff in That)foo#end");
        assertEvalException("#include(Stuff)");
        assertEvalException("#parse(Stuff)");
        assertEvalException("#define(Stuff)foo#end");
        assertEvalException("#macro( name Stuff)foo#end");
        assertEvalException("#foreach($i in [1..3])#break(Stuff)#end");
        assertEvalException("#literal(Stuff)foo#end");
        assertEvalException("#evaluate(Stuff)", ParseErrorException.class);
        log.on();
    }

    public void testLateDefinedMacroWithBadArg()
    {
        String evil = "#evil(bar) #macro( evil $arg )$arg#end";
        assertEvalException(evil, TemplateInitException.class);
    }

}
