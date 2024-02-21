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

import junit.framework.TestSuite;
import org.apache.velocity.util.ExtProperties;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.Vector;


/**
 * Tests for the ExtProperties class. This is an identical
 *  copy of the ConfigurationTestCase, which will disappear when
 *  the Configuration class does
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class CommonsExtPropTestCase extends BaseTestCase
{
    /**
     * Comparison directory.
     */
    private static final String COMPARE_DIR =
        TEST_COMPARE_DIR + "/configuration/compare";

    /**
     * Results directory.
     */
    private static final String RESULTS_DIR =
        TEST_RESULT_DIR + "/configuration";

    /**
     * Test configuration
     */
    private static final String TEST_CONFIG =
        TEST_COMPARE_DIR + "/configuration/test-config.properties";

    /**
     * Creates a new instance.
     *
     */
    public CommonsExtPropTestCase(String name)
    {
        super(name);
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(CommonsExtPropTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testExtendedProperties ()
            throws Exception
    {
            assureResultsDirectoryExists(RESULTS_DIR);

            ExtProperties c = new ExtProperties(TEST_CONFIG);

            FileWriter result = new FileWriter(
                getFileName(RESULTS_DIR, "output", "res"));

            message(result, "Testing order of keys ...");
            showIterator(result, c.getKeys());

            message(result, "Testing retrieval of CSV values ...");
            showVector(result, c.getVector("resource.loaders"));

            message(result, "Testing subset(prefix).getKeys() ...");
            ExtProperties subset = c.subset("resource.loader.file");
            showIterator(result, subset.getKeys());

            message(result, "Testing getVector(prefix) ...");
            showVector(result, subset.getVector("path"));

            message(result, "Testing getString(key) ...");
            result.write(c.getString("config.string.value"));
            result.write("\n\n");

            message(result, "Testing getBoolean(key) ...");
            result.write(Boolean.toString(c.getBoolean("config.boolean.value")));
            result.write("\n\n");

            message(result, "Testing getByte(key) ...");
            result.write(Byte.toString(c.getByte("config.byte.value")));
            result.write("\n\n");

            message(result, "Testing getShort(key) ...");
            result.write(Short.toString(c.getShort("config.short.value")));
            result.write("\n\n");

            message(result, "Testing getInt(key) ...");
            result.write(Integer.toString(c.getInt("config.int.value")));
            result.write("\n\n");

            message(result, "Testing getLong(key) ...");
            result.write(Long.toString(c.getLong("config.long.value")));
            result.write("\n\n");

            message(result, "Testing getFloat(key) ...");
            result.write(Float.toString(c.getFloat("config.float.value")));
            result.write("\n\n");

            message(result, "Testing getDouble(key) ...");
            result.write(Double.toString(c.getDouble("config.double.value")));
            result.write("\n\n");

            message(result, "Testing escaped-comma scalar...");
            result.write( c.getString("escape.comma1"));
            result.write("\n\n");

            message(result, "Testing escaped-comma vector...");
            showVector(result,  c.getVector("escape.comma2"));
            result.write("\n\n");

            result.flush();
            result.close();

            if (!isMatch(RESULTS_DIR, COMPARE_DIR, "output","res","cmp"))
            {
                fail("Output incorrect.");
            }
    }

    private void showIterator(FileWriter result, Iterator i)
        throws Exception
    {
        while(i.hasNext())
        {
            result.write((String) i.next());
            result.write("\n");
        }
        result.write("\n");
    }

    private void showVector(FileWriter result, Vector v)
        throws Exception
    {
        for (Object aV : v)
        {
            result.write((String) aV);
            result.write("\n");
        }
        result.write("\n");
    }

    private void message(FileWriter result, String message)
        throws Exception
    {
        result.write("--------------------------------------------------\n");
        result.write(message + "\n");
        result.write("--------------------------------------------------\n");
        result.write("\n");
    }
}
