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

import org.apache.velocity.VelocityContext;

/**
 * Used to check that nulls are properly handled in #if statements
 */
public class IfNullTestCase extends BaseTestCase
{
    public IfNullTestCase(final String name)
    {
        super(name);
    }

    protected void setUpContext(VelocityContext context)
    {
        context.put("nullToString", new NullToString());
        context.put("notnull", new Object());
    }

    public void testIfEquals()
    {
        // both null
        assertEvalEquals("foo", "#if( $null == $otherNull )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( $null == $nullToString )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( $nullToString == $null )foo#{else}bar#end");
        // left null, right not
        assertEvalEquals("bar", "#if( $nullToString == $notnull )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $null == $notnull )foo#{else}bar#end");
        // right null, left not
        assertEvalEquals("bar", "#if( $notnull == $nullToString )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $notnull == $null )foo#{else}bar#end");
    }

    public void testIfNotEquals()
    {
        // both null
        assertEvalEquals("bar", "#if( $null != $otherNull )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $null != $nullToString )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $nullToString != $null )foo#{else}bar#end");
        // left null, right not
        assertEvalEquals("foo", "#if( $nullToString != $notnull )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( $null != $notnull )foo#{else}bar#end");
        // right null, left not
        assertEvalEquals("foo", "#if( $notnull != $nullToString )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( $notnull != $null )foo#{else}bar#end");
    }

    public void testIfValue()
    {
        assertEvalEquals("bar", "#if( $null )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $nullToString )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( !$null )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( !$nullToString )foo#{else}bar#end");
    }

    public void testIfAnd()
    {
        assertEvalEquals("bar", "#if( $null && $nullToString )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $nullToString && $null )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $null && $notnull )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $notnull && $nullToString )foo#{else}bar#end");
    }

    public void testIfOr()
    {
        assertEvalEquals("bar", "#if( $null || $nullToString )foo#{else}bar#end");
        assertEvalEquals("bar", "#if( $nullToString || $null )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( $null || $notnull )foo#{else}bar#end");
        assertEvalEquals("foo", "#if( $notnull || $nullToString )foo#{else}bar#end");
    }

    public static class NullToString
    {
        public String getAsString()
        {
            return null;
        }
    }

}
