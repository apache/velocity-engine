package org.apache.velocity.test;

/*
 * Copyright 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;
import junit.framework.Test;

import org.apache.velocity.util.StringUtils;

/**
 * Test case for any miscellaneous stuff.  If it isn't big, and doesn't fit
 * anywhere else, it goes here
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id: MiscTestCase.java,v 1.1.8.1 2004/03/03 23:23:04 geirm Exp $
 */
public class MiscTestCase extends BaseTestCase
{
    public MiscTestCase()
    {
        super("MiscTestCase");
    }

    public MiscTestCase (String name)
    {
        super(name);
    }

    public static Test suite ()
    {
        return new MiscTestCase();
    }

    public void runTest()
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

    }

}