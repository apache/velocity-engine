package org.apache.velocity.test;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.File;
import java.io.FileWriter;

import java.util.Iterator;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.configuration.Configuration;
import org.apache.velocity.util.StringUtils;

import junit.framework.TestCase;

/**
 * Tests for the Configuration class.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: ConfigurationTestCase.java,v 1.4 2001/05/20 20:29:48 geirm Exp $
 *
 * @deprecated Will be removed when Configuration class is removed
 */
public class ConfigurationTestCase extends BaseTestCase
{
    /**
     * Comparison directory.
     */
    private static final String COMPARE_DIR = 
        "../test/configuration/compare";
    
    /**
     * Results directory.
     */
    private static final String RESULTS_DIR = 
        "../test/configuration/results";

    /**
     * Test configuration
     */
    private static final String TEST_CONFIG = 
        "../test/configuration/test.config";

    /**
     * Creates a new instance.
     *
     */
    public ConfigurationTestCase()
    {
        super("ConfigurationTestCase");
    }

    public static junit.framework.Test suite()
    {
        return new ConfigurationTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            assureResultsDirectoryExists(RESULTS_DIR);
            
            Configuration c = new Configuration(TEST_CONFIG);
            
            FileWriter result = new FileWriter(
                getFileName(RESULTS_DIR, "output", "res"));
            
            message(result, "Testing order of keys ...");
            showIterator(result, c.getKeys());
            
            message(result, "Testing retrieval of CSV values ...");
            showVector(result, c.getVector("resource.loader"));            

            message(result, "Testing subset(prefix).getKeys() ...");
            Configuration subset = c.subset("file.resource.loader");
            showIterator(result, subset.getKeys());

            message(result, "Testing getVector(prefix) ...");
            showVector(result, subset.getVector("path"));            

            message(result, "Testing getString(key) ...");
            result.write(c.getString("config.string.value"));
            result.write("\n\n");

            message(result, "Testing getBoolean(key) ...");
            result.write(new Boolean(c.getBoolean("config.boolean.value")).toString());
            result.write("\n\n");

            message(result, "Testing getByte(key) ...");
            result.write(new Byte(c.getByte("config.byte.value")).toString());
            result.write("\n\n");

            message(result, "Testing getShort(key) ...");
            result.write(new Short(c.getShort("config.short.value")).toString());
            result.write("\n\n");

            message(result, "Testing getInt(key) ...");
            result.write(new Integer(c.getInt("config.int.value")).toString());
            result.write("\n\n");

            message(result, "Testing getLong(key) ...");
            result.write(new Long(c.getLong("config.long.value")).toString());
            result.write("\n\n");

            message(result, "Testing getFloat(key) ...");
            result.write(new Float(c.getFloat("config.float.value")).toString());
            result.write("\n\n");

            message(result, "Testing getDouble(key) ...");
            result.write(new Double(c.getDouble("config.double.value")).toString());
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
        catch (Exception e)
        {
            System.err.println("Cannot setup ConfigurationTestCase!");
            e.printStackTrace();
            System.exit(1);
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
        for (int j = 0; j < v.size(); j++)
        {
            result.write((String) v.get(j));
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
