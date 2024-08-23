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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.DeprecationAwareExtProperties;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class OldPropertiesTestCase extends TestCase implements TemplateTestBase
{
    private VelocityEngine ve = null;
    private TestLogger logger = null;

    /**
     * Default constructor.
     */
    public OldPropertiesTestCase(String name)
    {
        super(name);
    }

    @Override
    public void setUp()
            throws Exception
    {
    }

    public static Test suite ()
    {
        return new TestSuite(OldPropertiesTestCase.class);
    }

    static Pattern propPattern = Pattern.compile("^([a-z._]+)\\s*=\\s*[^#]+.*$", Pattern.CASE_INSENSITIVE);
    static Pattern warnPattern = Pattern.compile("^\\s*\\[warn\\]\\s*configuration key '([a-z._]+)' has been deprecated in favor of '([a-z._]+)'$", Pattern.CASE_INSENSITIVE);

    static class Translator extends DeprecationAwareExtProperties
    {
        @Override
        public String translateKey(String oldName) { return super.translateKey(oldName); }
    }

    /**
     * Check old properties setting and retrieval
     */
    public void testOldProperties()
        throws Exception
    {
        String oldProperties = TEST_COMPARE_DIR + "/oldproperties/velocity.properties";
        ve = new VelocityEngine();
        logger = new TestLogger(false, true);
        logger.setEnabledLevel(TestLogger.LOG_LEVEL_WARN);

        // put our test logger where it belongs for this test
        Field loggerField = DeprecationAwareExtProperties.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(null, logger);

        logger.on();
        ve.setProperties(oldProperties);
        logger.off();

        Translator translator = new Translator();

        // check getting old/new values
        List<String> oldPropSettings = Files.readAllLines(Paths.get(oldProperties), StandardCharsets.ISO_8859_1);
        Set<String> oldKeys = new HashSet<>();
        for (String oldProp : oldPropSettings)
        {
            Matcher matcher = propPattern.matcher(oldProp);
            if (matcher.matches())
            {
                String propName = matcher.group(1);
                String translated = translator.translateKey(propName);
                if (!translated.equals(propName))
                {
                    Object oldKeyValue = ve.getProperty(propName);
                    Object newKeyValue = ve.getProperty(translated);
                    assertEquals(oldKeyValue, newKeyValue);
                    oldKeys.add(propName);
                }
            }
        }

        // check warnings in the logs
        String log = logger.getLog();
        String logLines[] = log.split("\\r?\\n");
        for (String logLine : logLines)
        {
            Matcher matcher = warnPattern.matcher(logLine);
            if (matcher.matches() && matcher.groupCount() == 2)
            {
                String oldName = matcher.group(1);
                assertTrue(oldKeys.remove(oldName));
            }
        }
        if (oldKeys.size() > 0)
        {
            fail("No warning detected for the following properties: " + StringUtils.join(oldKeys, ", "));
        }
    }

    /**
     * Check default properties
     */
    public void testNewProperties()
        throws Exception
    {
        ve = new VelocityEngine();
        logger = new TestLogger(false, true);
        logger.setEnabledLevel(TestLogger.LOG_LEVEL_WARN);

        // put our test logger where it belongs for this test
        Field loggerField = DeprecationAwareExtProperties.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(null, logger);

        logger.on();
        ve.init();
        logger.off();

        // check warnings in the logs
        String log = logger.getLog();
        String logLines[] = log.split("\\r?\\n");
        for (String logLine : logLines)
        {
            Matcher matcher = warnPattern.matcher(logLine);
            if (matcher.matches() && matcher.groupCount() == 2)
            {
                fail("Default properties contain deprecated property '" + matcher.group(1) + "', deprecated in favor of '" + matcher.group(2) + "'");
            }
        }

    }
}
