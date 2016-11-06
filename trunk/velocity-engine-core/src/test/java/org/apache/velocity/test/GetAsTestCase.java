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

import org.apache.velocity.util.TemplateBoolean;
import org.apache.velocity.util.TemplateNumber;
import org.apache.velocity.util.TemplateString;

/**
 * Test objects with getAs<Type>() methods.
 */
public class GetAsTestCase extends BaseTestCase
{
    public GetAsTestCase(final String name)
    {
        super(name);
    }

    public void testCustomString()
    {
        // render
        context.put("foo", new CustomString("getAsString"));
        assertEvalEquals("getAsString", "$foo");
        // aborted value
        context.put("bar", new CustomString(null));
        assertEvalEquals("", "$!bar");
        // concatenation
        context.put("half", new CustomString("half"));
        assertEvalEquals("1half", "#set( $out = 1 + $half )$out");
    }

    public void testCustomBoolean()
    {
        context.put("foo", new CustomBoolean(false));
        assertEvalEquals("right", "#if( !$foo )right#end");
    }

    public void testCustomNumber()
    {
        context.put("foo", new CustomNumber(7));
        assertEvalEquals("14", "#set( $bar = $foo * 2 )$bar");
    }


    public void testTemplateString()
    {
        context.put("foo", new CustomTemplateString("getAsString"));
        assertEvalEquals("getAsString", "$foo");
    }

    public void testTemplateBoolean()
    {
        context.put("foo", new CustomTemplateBoolean(false));
        assertEvalEquals("right", "#if( !$foo )right#end");
    }

    public void testTemplateNumber()
    {
        context.put("foo", new CustomTemplateNumber(5));
        assertEvalEquals("25", "#set( $foo = $foo * $foo )$foo");
    }



    public static class CustomString
    {
        String string;
        public CustomString(String string)
        {
            this.string = string;
        }
        public String getAsString()
        {
            return string;
        }
    }

    public static class CustomBoolean
    {
        boolean bool;
        public CustomBoolean(boolean bool)
        {
            this.bool = bool;
        }
        public boolean getAsBoolean()
        {
            return bool;
        }
    }

    public static class CustomNumber
    {
        Number num;
        public CustomNumber(Number num)
        {
            this.num = num;
        }
        public Number getAsNumber()
        {
            return num;
        }
    }

    public static class CustomTemplateString extends CustomString implements TemplateString
    {
        public CustomTemplateString(String string)
        {
            super(string);
        }
    }

    public static class CustomTemplateBoolean extends CustomBoolean implements TemplateBoolean
    {
        public CustomTemplateBoolean(Boolean bool)
        {
            super(bool);
        }
    }

    public static class CustomTemplateNumber extends CustomNumber implements TemplateNumber
    {
        public CustomTemplateNumber(Number num)
        {
            super(num);
        }
    }

}


