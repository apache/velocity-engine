package org.apache.velocity.test;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.test.provider.BoolObj;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.app.FieldMethodizer;

import junit.framework.TestCase;

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
 * @version $Id: TemplateTestCase.java,v 1.34.8.1 2004/03/03 23:23:04 geirm Exp $
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
    {
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
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
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
                fail("Processed template did not match expected output");
            }
        }
        catch (Exception e)
        {
            System.out.println("EXCEPTION : " + e );

            fail(e.getMessage());
        }
    }
}
