package org.apache.velocity.test.provider;

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
 * Provides overloaded methods for testing method execution within a foreach
 * @author <a href="mailto:wglass@apache.org">Will Glass-Husain</a>
 * @version $Id: VelocimacroTestCase.java 75959 2004-03-19 17:13:40Z dlr $
 */
public class ForeachMethodCallHelper 
{
    public String getFoo(Integer v) { return "int "+v; }
    public String getFoo(String v) { return "str "+v; }
}
