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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.FieldMethodizer;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.test.provider.BoolObj;
import org.apache.velocity.test.provider.NullToStringObject;
import org.apache.velocity.test.provider.TestNumber;
import org.apache.velocity.test.provider.TestProvider;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Easily add test cases which evaluate templates and check their output.
 *
 * NOTE:
 * This class DOES NOT extend RuntimeTestCase because the TemplateTestSuite
 * already initializes the Velocity runtime and adds the template
 * test cases. Having this class extend RuntimeTestCase causes the
 * Runtime to be initialized twice which is not good. I only discovered
 * this after a couple hours of wondering why all the properties
 * being setup were ending up as Vectors. At first I thought it
 * was a problem with the Configuration class, but the Runtime
 * was being initialized twice: so the first time the property
 * is seen it's stored as a String, the second time it's seen
 * the Configuration class makes a Vector with both Strings.
 * As a result all the getBoolean(property) calls were failing because
 * the Configurations class was trying to create a Boolean from
 * a Vector which doesn't really work that well. I have learned
 * my lesson and now have to add some code to make sure the
 * Runtime isn't initialized more then once :-)
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id$
 */
public class TemplateTestCase extends BaseTestCase implements TemplateTestBase
{
    /**
     * The base file name of the template and comparison file (i.e. array for
     * array.vm and array.cmp).
     */
    protected String baseFileName;

    private TestProvider provider;
    private ArrayList al;
    private Hashtable h;
    private VelocityContext context;
    private VelocityContext context1;
    private VelocityContext context2;
    private Vector vec;

    /**
     * Creates a new instance.
     *
     * @param baseFileName The base name of the template and comparison file to
     *                     use (i.e. array for array.vm and array.cmp).
     */
    public TemplateTestCase (String baseFileName)
    {
        super(getTestCaseName(baseFileName));
        this.baseFileName = baseFileName;
    }

    public static junit.framework.Test suite()
    {
        return new TemplateTestSuite();
    }

    /**
     * Sets up the test.
     */
    protected void setUp ()
       throws Exception
    {
        super.setUp();
        Velocity.reset();
        Velocity.setProperty(
            Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);

        Velocity.setProperty(
                Velocity.RUNTIME_LOG_INSTANCE, new TestLogger());

        Velocity.setProperty("space.gobbling", "bc");

        Velocity.init();

        provider = new TestProvider();
        al = provider.getCustomers();
        h = new Hashtable();

        h.put("Bar", "this is from a hashtable!");
        h.put("Foo", "this is from a hashtable too!");

        /*
         *  lets set up a vector of objects to test late introspection. See ASTMethod.java
         */

        vec = new Vector();

        vec.addElement(new String("string1"));
        vec.addElement(new String("string2"));

        /*
         *  set up 3 chained contexts, and add our data
         *  throught the 3 of them.
         */

        context2 = new VelocityContext();
        context1 = new VelocityContext( context2 );
        context = new VelocityContext( context1 );

        context.put("provider", provider);
        context1.put("name", "jason");
        context1.put("name2", new StringBuffer("jason"));
        context1.put("name3", new StringBuffer("geoge"));
        context2.put("providers", provider.getCustomers2());
        context.put("list", al);
        context1.put("hashtable", h);
        context2.put("hashmap", new HashMap());
        context2.put("search", provider.getSearch());
        context.put("relatedSearches", provider.getRelSearches());
        context1.put("searchResults", provider.getRelSearches());
        context2.put("stringarray", provider.getArray());
        context.put("vector", vec );
        context.put("mystring", new String());
        context.put("runtime", new FieldMethodizer( "org.apache.velocity.runtime.RuntimeSingleton" ));
        context.put("fmprov", new FieldMethodizer( provider ));
        context.put("Floog", "floogie woogie");
        context.put("boolobj", new BoolObj() );

        /*
         *  we want to make sure we test all types of iterative objects
         *  in #foreach()
         */

        Object[] oarr = { "a","b","c","d" } ;
        int intarr[] = { 10, 20, 30, 40, 50 };

        context.put( "collection", vec );
        context2.put("iterator", vec.iterator());
        context1.put("map", h );
        context.put("obarr", oarr );
        context.put("enumerator", vec.elements());
        context.put("intarr", intarr );

        // Add some Numbers
        context.put ("int1", new Integer (1000));
        context.put ("long1", new Long (10000000000l));
        context.put ("float1", new Float (1000.1234));
        context.put ("double1", new Double (10000000000d));

        // Add a TemplateNumber
        context.put ("templatenumber1", new TestNumber (999.125));

        /**
         * Test #foreach() with a list containing nulls
         */
        ArrayList nullList = new ArrayList();
        nullList.add("a");
        nullList.add("b");
        nullList.add(null);
        nullList.add("d");
        context.put("nullList", nullList);

        // test silent references with a null tostring
        context.put("nullToString",new NullToStringObject());
    }

    /**
     * Runs the test.
     */
    public void runTest ()
        throws Exception
    {
        Template template = RuntimeSingleton.getTemplate
            (getFileName(null, baseFileName, TMPL_FILE_EXT));

        assureResultsDirectoryExists(RESULT_DIR);

        /* get the file to write to */
        FileOutputStream fos =
            new FileOutputStream (getFileName(
                RESULT_DIR, baseFileName, RESULT_FILE_EXT));

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        /* process the template */
        template.merge( context, writer);

        /* close the file */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR,COMPARE_DIR,baseFileName,
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Processed template "+getFileName(
                RESULT_DIR, baseFileName, RESULT_FILE_EXT)+" did not match expected output");
        }
    }
}
