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

import junit.framework.TestCase;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Base test case that provides utility methods for
 * the rest of the tests.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Nathan Bubna
 * @version $Id$
 */
public abstract class BaseTestCase extends TestCase implements TemplateTestBase
{
    protected VelocityEngine engine;
    protected VelocityContext context;
    protected boolean DEBUG = false;
    protected TestLogger log;
    protected String stringRepoName = "string.repo";

    public BaseTestCase(String name)
    {
        super(name);

        // if we're just running one case, then have DEBUG
        // automatically set to true
        String test = System.getProperty("test");
        if (test != null)
        {
            DEBUG = test.equals(getClass().getSimpleName());
        }
    }

    protected VelocityEngine createEngine()
    {
        VelocityEngine ret = new VelocityEngine();
        ret.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, log);

        // use string resource loader by default, instead of file
        ret.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,string");
        ret.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        ret.addProperty("string.resource.loader.repository.name", stringRepoName);
        ret.addProperty("string.resource.loader.repository.static", "false");

        setUpEngine(ret);
        return ret;
    }

    protected void setUp() throws Exception
    {
        //by default, make the engine's log output go to the test-report
        log = new TestLogger(false, false);
        engine = createEngine();
        context = new VelocityContext();
        setUpContext(context);
    }

    protected void setUpEngine(VelocityEngine engine)
    {
        // extension hook
    }

    protected void setUpContext(VelocityContext context)
    {
        // extension hook
    }

    protected StringResourceRepository getStringRepository()
    {
        StringResourceRepository repo =
            (StringResourceRepository)engine.getApplicationAttribute(stringRepoName);
        if (repo == null)
        {
            engine.init();
            repo =
                (StringResourceRepository)engine.getApplicationAttribute(stringRepoName);
        }
        return repo;
    }

    protected void addTemplate(String name, String template)
    {
        info("Template '"+name+"':  "+template);
        getStringRepository().putStringResource(name, template);
    }

    protected void removeTemplate(String name)
    {
        info("Removed: '"+name+"'");
        getStringRepository().removeStringResource(name);
    }

    public void tearDown()
    {
        engine = null;
        context = null;
    }

    protected void info(String msg)
    {
        info(msg, null);
    }

    protected void info(String msg, Throwable t)
    {
        if (DEBUG)
        {
            try
            {
                if (engine == null)
                {
                    Velocity.getLog().info(msg, t);
                }
                else
                {
                    engine.getLog().info(msg, t);
                }
            }
            catch (Throwable t2)
            {
                System.out.println("Failed to log: "+msg+(t!=null?" - "+t: ""));
                System.out.println("Cause: "+t2);
                t2.printStackTrace();
            }
        }
    }

    public void testBase()
    {
        if (DEBUG && engine != null)
        {
            assertSchmoo("");
            assertSchmoo("abc\n123");
        }
    }

    /**
     * Compare an expected string with the given loaded template
     */
    protected void assertTmplEquals(String expected, String template)
    {
        info("Expected:  " + expected + " from '" + template + "'");

        StringWriter writer = new StringWriter();
        try
        {
            engine.mergeTemplate(template, "utf-8", context, writer);
        }
        catch (RuntimeException re)
        {
            info("RuntimeException!", re);
            throw re;
        }
        catch (Exception e)
        {
            info("Exception!", e);
            throw new RuntimeException(e);
        }

        info("Result:  " + writer.toString());
        assertEquals(expected, writer.toString());
    }

    /**
     * Ensure that a context value is as expected.
     */
    protected void assertContextValue(String key, Object expected)
    {
        info("Expected value of '"+key+"': "+expected);
        Object value = context.get(key);
        info("Result: "+value);
        assertEquals(expected, value);
    }

    /**
     * Ensure that a template renders as expected.
     */
    protected void assertEvalEquals(String expected, String template)
    {
        info("Expectation: "+expected);
        assertEquals(expected, evaluate(template));
    }

    /**
     * Ensure that the given string renders as itself when evaluated.
     */
    protected void assertSchmoo(String templateIsExpected)
    {
        assertEvalEquals(templateIsExpected, templateIsExpected);
    }

    /**
     * Ensure that an exception occurs when the string is evaluated.
     */
    protected Exception assertEvalException(String evil)
    {
        return assertEvalException(evil, null);
    }

    /**
     * Ensure that a specified type of exception occurs when evaluating the string.
     */
    protected Exception assertEvalException(String evil, Class exceptionType)
    {
        try
        {
            if (!DEBUG)
            {
                log.off();
            }
            if (exceptionType != null)
            {
                info("Expectation: "+exceptionType.getName());
            }
            else
            {
                info("Expectation: "+Exception.class.getName());
            }
            evaluate(evil);
            String msg = "Template '"+evil+"' should have thrown an exception.";
            info("Fail: "+msg);
            fail(msg);
        }
        catch (Exception e)
        {
            if (exceptionType != null && !exceptionType.isAssignableFrom(e.getClass()))
            {
                String msg = "Was expecting template '"+evil+"' to throw "+exceptionType+" not "+e;
                info("Fail: "+msg);
                fail(msg);
            }
            return e;
        }
        finally
        {
            if (!DEBUG)
            {
                log.on();
            }
        }
        return null;
    }

    /**
     * Ensure that the error message of the expected exception has the proper location info.
     */
    protected Exception assertEvalExceptionAt(String evil, String template,
                                              int line, int col)
    {
        String loc = template+"[line "+line+", column "+col+"]";
        info("Expectation: Exception at "+loc);
        Exception e = assertEvalException(evil);

        info("Result: "+e.getClass().getName()+" - "+e.getMessage());
        if (e.getMessage().indexOf(loc) < 1)
        {
            fail("Was expecting exception at "+loc+" instead of "+e.getMessage());
        }
        return e;
    }

    /**
     * Only ensure that the error message of the expected exception
     * has the proper line and column info.
     */
    protected Exception assertEvalExceptionAt(String evil, int line, int col)
    {
         return assertEvalExceptionAt(evil, "", line, col);
    }

    /**
     * Evaluate the specified String as a template and return the result as a String.
     */
    protected String evaluate(String template)
    {
        StringWriter writer = new StringWriter();
        try
        {
            info("Template: "+template);

            // use template as its own name, since our templates are short
            // unless it's not that short, then shorten it...
            String name = (template.length() <= 15) ? template : template.substring(0,15);
            engine.evaluate(context, writer, name, template);

            String result = writer.toString();
            info("Result: "+result);
            return result;
        }
        catch (RuntimeException re)
        {
            info("RuntimeException!", re);
            throw re;
        }
        catch (Exception e)
        {
            info("Exception!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Concatenates the file name parts together appropriately.
     *
     * @return The full path to the file.
     */
    protected String getFileName(final String dir, final String base, final String ext)
    {
        return getFileName(dir, base, ext, false);
    }

    protected String getFileName(final String dir, final String base, final String ext, final boolean mustExist)
    {
        StringBuffer buf = new StringBuffer();
        try
        {
            File baseFile = new File(base);
            if (dir != null)
            {
                if (!baseFile.isAbsolute())
                {
                    baseFile = new File(dir, base);
                }

                buf.append(baseFile.getCanonicalPath());
            }
            else
            {
                buf.append(baseFile.getPath());
            }

            if (org.apache.commons.lang3.StringUtils.isNotEmpty(ext))
            {
                buf.append('.').append(ext);
            }

            if (mustExist)
            {
                File testFile = new File(buf.toString());

                if (!testFile.exists())
                {
                    String msg = "getFileName() result " + testFile.getPath() + " does not exist!";
                    info(msg);
                    fail(msg);
                }

                if (!testFile.isFile())
                {
                    String msg = "getFileName() result " + testFile.getPath() + " is not a file!";
                    info(msg);
                    fail(msg);
                }
            }
        }
        catch (IOException e)
        {
            fail("IO Exception while running getFileName(" + dir + ", " + base + ", "+ ext + ", " + mustExist + "): " + e.getMessage());
        }

        return buf.toString();
    }

    /**
     * Assures that the results directory exists.  If the results directory
     * cannot be created, fails the test.
     */
    protected void assureResultsDirectoryExists(String resultsDirectory)
    {
        File dir = new File(resultsDirectory);
        if (!dir.exists())
        {
            info("Template results directory ("+resultsDirectory+") does not exist");
            if (dir.mkdirs())
            {
                info("Created template results directory");
                if (DEBUG)
                {
                    info("Created template results directory: "+resultsDirectory);
                }
            }
            else
            {
                String errMsg = "Unable to create '"+resultsDirectory+"'";
                info(errMsg);
                fail(errMsg);
            }
        }
    }


    /**
     * Normalizes lines to account for platform differences.  Macs use
     * a single \r, DOS derived operating systems use \r\n, and Unix
     * uses \n.  Replace each with a single \n.
     *
     * @return source with all line terminations changed to Unix style
     */
    protected String normalizeNewlines (String source)
    {
        return source.replaceAll("\r\n?", "\n");
    }

    /**
     * Returns whether the processed template matches the
     * content of the provided comparison file.
     *
     * @return Whether the output matches the contents
     *         of the comparison file.
     *
     * @exception Exception Test failure condition.
     */
    protected boolean isMatch (String resultsDir,
                               String compareDir,
                               String baseFileName,
                               String resultExt,
                               String compareExt) throws Exception
    {
        if (DEBUG)
        {
            info("Result: "+resultsDir+'/'+baseFileName+'.'+resultExt);
        }
        String result = getFileContents(resultsDir, baseFileName, resultExt);
        return isMatch(result,compareDir,baseFileName,compareExt);
    }


    protected String getFileContents(String dir, String baseFileName, String ext)
    {
        String fileName = getFileName(dir, baseFileName, ext, true);
        return StringUtils.fileContentsToString(fileName);
    }

    /**
     * Returns whether the processed template matches the
     * content of the provided comparison file.
     *
     * @return Whether the output matches the contents
     *         of the comparison file.
     *
     * @exception Exception Test failure condition.
     */
    protected boolean isMatch (String result,
                               String compareDir,
                               String baseFileName,
                               String compareExt) throws Exception
    {
        String compare = getFileContents(compareDir, baseFileName, compareExt);

        // normalize each wrt newline
        result = normalizeNewlines(result);
        compare = normalizeNewlines(compare);
        if (DEBUG)
        {
            info("Expection: "+compareDir+'/'+baseFileName+'.'+compareExt);
        }
        return result.equals(compare);
    }

    /**
     * Turns a base file name into a test case name.
     *
     * @param s The base file name.
     * @return  The test case name.
     */
    protected static final String getTestCaseName(String s)
    {
        StringBuffer name = new StringBuffer();
        name.append(Character.toTitleCase(s.charAt(0)));
        name.append(s.substring(1, s.length()).toLowerCase());
        return name.toString();
    }
}
