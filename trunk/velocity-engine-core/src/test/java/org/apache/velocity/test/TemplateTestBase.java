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

/**
 * This is a base interface that contains a bunch of static final
 * strings that are of use when testing templates.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id$
 */
public interface TemplateTestBase
{
    /**
     * Directory relative to the distribution root, where the
     * values to compare test results to are stored.
     */
    String TEST_COMPARE_DIR = System.getProperty("test.compare.dir");

    /**
     * Directory relative to the distribution root, where the
     * test cases should put their output
     */
    String TEST_RESULT_DIR = System.getProperty("test.result.dir");


    /**
     * VTL file extension.
     */
    String TMPL_FILE_EXT = "vm";

    /**
     * Comparison file extension.
     */
    String CMP_FILE_EXT = "cmp";

    /**
     * Comparison file extension.
     */
    String RESULT_FILE_EXT = "res";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    String FILE_RESOURCE_LOADER_PATH =
                          TEST_COMPARE_DIR + "/templates";

    /**
     * Properties file that lists which template tests to run.
     */
    String TEST_CASE_PROPERTIES =
                          FILE_RESOURCE_LOADER_PATH + "/templates.properties";

    /**
     * Results relative to the build directory.
     */
    String RESULT_DIR =
                          TEST_RESULT_DIR + "/templates";

    /**
     * Results relative to the build directory.
     */
    String COMPARE_DIR =
                          FILE_RESOURCE_LOADER_PATH + "/compare";

}
