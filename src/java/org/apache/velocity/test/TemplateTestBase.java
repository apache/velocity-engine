package org.apache.velocity.test;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This is a base interface that contains a bunch of static final
 * strings that are of use when testing templates.
 *
 * @version $Id: TemplateTestBase.java,v 1.3 2004/02/27 18:43:19 dlr Exp $
 */
public interface TemplateTestBase
{
    /**
     * VTL file extension.
     */
    public final static String TMPL_FILE_EXT = "vm";

    /**
     * Comparison file extension.
     */
    public final static String CMP_FILE_EXT = "cmp";

    /**
     * Comparison file extension.
     */
    public final static String RESULT_FILE_EXT = "res";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    public final static String FILE_RESOURCE_LOADER_PATH = 
                          "../test/templates";

    /**
     * Properties file that lists which template tests to run.
     */
    public final static String TEST_CASE_PROPERTIES = 
                          FILE_RESOURCE_LOADER_PATH + "/templates.properties";

    /**
     * Results relative to the build directory.
     */
    public final static String RESULT_DIR = 
                          FILE_RESOURCE_LOADER_PATH + "/results";

    /**
     * Results relative to the build directory.
     */
    public final static String COMPARE_DIR = 
                          FILE_RESOURCE_LOADER_PATH + "/compare";

}