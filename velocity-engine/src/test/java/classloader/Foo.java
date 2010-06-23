package classloader;

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
 *  Simple class Foo to be used in classloader testing
 *  This class should be kept here and not in velocity.jar
 *  to keep out of the parent classloader of the test
 *  classloader
 */
public class Foo
{
    /*
     *  the ClassloaderChangeTest
     *  depends on this string as
     *  is.  If this changes (there is no reason
     *  to ever do that, BTW), then
     *  udpate ClassloaderChangeTest as well.
     */
    private static String MSG =
        "Hello From Foo";

	public String doIt()
	{
		return MSG;
	}
}

