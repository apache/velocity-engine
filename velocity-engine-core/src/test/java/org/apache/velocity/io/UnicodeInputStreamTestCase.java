package org.apache.velocity.io;

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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Test the UnicodeInputStream.
 *
 * @author  $author$
 * @version  $Revision$, $Date$
 */
public class UnicodeInputStreamTestCase
    extends TestCase
{

    public UnicodeInputStreamTestCase(final String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(UnicodeInputStreamTestCase.class);
    }

    public void testSimpleStream()
        throws Exception
    {
        testRun(null, "Ich bin zwei Oeltanks", "US-ASCII", true);
        testRun(null, "Ich bin zwei Oeltanks", "US-ASCII", false);
    }

    public void testSimpleUTF8()
        throws Exception
    {
        testRun(null, "Ich bin zwei Oeltanks", "UTF-8", true);
        testRun(null, "Ich bin zwei Oeltanks", "UTF-8", false);
    }

    public void testRealUTF8()
        throws Exception
    {
        testRun(null, "Ich bin zwei \u00d6ltanks", "UTF-8", true);
        testRun(null, "Ich bin zwei \u00d6ltanks", "UTF-8", false);
    }

    public void testRealUTF8WithBOM()
        throws Exception
    {
        testRun(UnicodeInputStream.UTF8_BOM, "Ich bin ein Test",
                "UTF-8", true);
        testRun(UnicodeInputStream.UTF8_BOM, "Ich bin ein Test",
                "UTF-8", false);
    }

    public void testRealUTF16BEWithBOM()
        throws Exception
    {
        testRun(UnicodeInputStream.UTF16BE_BOM, "Ich bin ein Test",
                "UTF-16BE", true);
        testRun(UnicodeInputStream.UTF16BE_BOM, "Ich bin ein Test",
                "UTF-16BE", false);
    }

    public void testRealUTF16LEWithBOM()
        throws Exception
    {
        testRun(UnicodeInputStream.UTF16LE_BOM, "Ich bin ein Test",
                "UTF-16LE", true);
        testRun(UnicodeInputStream.UTF16LE_BOM, "Ich bin ein Test",
                "UTF-16LE", false);
    }

    public void testRealUTF32BEWithBOM()
        throws Exception
    {
        testRun(UnicodeInputStream.UTF32BE_BOM, null,
                "UTF-32BE", true);
        testRun(UnicodeInputStream.UTF32BE_BOM, null,
                "UTF-32BE", false);
    }

    public void testRealUTF32LEWithBOM()
        throws Exception
    {
        testRun(UnicodeInputStream.UTF32LE_BOM, null,
                "UTF-32LE", true);
        testRun(UnicodeInputStream.UTF32LE_BOM, null,
                "UTF-32LE", false);
    }


    protected void testRun(final UnicodeInputStream.UnicodeBOM bom, final String str, final String testEncoding, final boolean skipBOM)
        throws Exception
    {

        byte [] testString = buildTestString(bom, str, testEncoding, skipBOM);

        InputStream is = null;
        UnicodeInputStream uis = null;

        try
        {
            is = createInputStream(bom, str, testEncoding);
            uis = new UnicodeInputStream(is, skipBOM);

            assertEquals("BOM Skipping problem", skipBOM, uis.isSkipBOM());

            if (bom != null)
            {
                assertEquals("Wrong Encoding detected", testEncoding, uis.getEncodingFromStream());
            }

            byte [] result = readAllBytes(uis, testEncoding);

            assertNotNull(testString);
            assertNotNull(result);
            assertEquals("Wrong result length", testString.length, result.length);

            for (int i = 0; i < result.length; i++)
            {
                assertEquals("Wrong Byte at " + i, testString[i], result[i]);
            }
        }
        finally
        {

            if (uis != null)
            {
                uis.close();
            }

            if (is != null)
            {
                is.close();
            }
        }
    }

    protected InputStream createInputStream(final UnicodeInputStream.UnicodeBOM bom, final String str, final String enc)
        throws Exception
    {

        if (bom == null)
        {
            if (str != null)
            {
                return new ByteArrayInputStream(str.getBytes(enc));
            }
            else
            {
                return new ByteArrayInputStream(new byte[0]);
            }
        }
        else
        {
            if (str != null)
            {
                return new ByteArrayInputStream(ArrayUtils.addAll(bom.getBytes(), str.getBytes(enc)));
            }
            else
            {
                return new ByteArrayInputStream(ArrayUtils.addAll(bom.getBytes(), new byte[0]));
            }
        }
    }

    protected byte [] buildTestString(final UnicodeInputStream.UnicodeBOM bom, final String str, final String enc, final boolean skipBOM)
        throws Exception
    {

        byte [] strBytes = (str != null) ? str.getBytes(enc) : new byte[0];

        if ((bom == null) || skipBOM)
        {
                return strBytes;
        }
        else
        {
            return ArrayUtils.addAll(bom.getBytes(), strBytes);
        }
    }

    protected byte [] readAllBytes(final InputStream inputStream, final String enc)
        throws Exception
    {
        InputStreamReader isr = null;

        byte [] res = new byte[0];

        try
        {
            byte[] buf = new byte[1024];
            int read = 0;

            while ((read = inputStream.read(buf)) >= 0)
            {
                res = ArrayUtils.addAll(res, ArrayUtils.subarray(buf, 0, read));
            }
        }
        finally
        {

            if (isr != null)
            {
                isr.close();
            }
        }

        return res;
    }

}
