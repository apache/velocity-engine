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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case for any miscellaneous stuff.  If it isn't big, and doesn't fit
 * anywhere else, it goes here
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class MiscTestCase extends BaseTestCase
{
    public MiscTestCase (String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new TestSuite(MiscTestCase.class);
    }

    public void testRuntimeInstanceProperties()
    {
        // check that runtime instance properties can be set and retrieved
        RuntimeInstance ri = new RuntimeInstance();
        ri.setProperty("baabaa.test","the answer");
        assertEquals("the answer",ri.getProperty("baabaa.test"));
    }
    
    public void testStringUtils()
    {
        /*
         *  some StringUtils tests
         */

        String eol = "XY";

        String arg = "XY";
        String res = StringUtils.chop(arg, 1, eol );
        assertTrue( "Test 1", res.equals("") );

        arg = "X";
        res = StringUtils.chop( arg, 1, eol );
        assertTrue( "Test 2", res.equals("") );

        arg = "ZXY";
        res = StringUtils.chop( arg, 1, eol );
        assertTrue( "Test 3", res.equals("Z") );


        arg = "Hello!";
        res = StringUtils.chop( arg, 2, eol );
        assertTrue( "Test 4", res.equals("Hell"));

        arg = null;
        res = StringUtils.nullTrim(arg);
        assertNull(arg);

        arg = " test ";
        res = StringUtils.nullTrim(arg);
        assertEquals("test",res);

        arg = "test";
        res = StringUtils.nullTrim(arg);
        assertEquals("test",res);

        List list = null;
        assertNull(StringUtils.trimStrings(list));

        list = new ArrayList();
        assertEquals(new ArrayList(),StringUtils.trimStrings(list));

        list.add("test");
        list.add(" abc");
        StringUtils.trimStrings(list);
        assertEquals("test",list.get(0));
        assertEquals("abc",list.get(1));

    }

}
