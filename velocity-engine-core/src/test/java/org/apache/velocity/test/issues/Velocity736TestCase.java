package org.apache.velocity.test.issues;

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

import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-736.
 */
public class Velocity736TestCase extends BaseTestCase
{
    public Velocity736TestCase(String name)
    {
       super(name);
       DEBUG = true;
    }

    public void testPublicMethodInheritedFromAbstractProtectedClass() throws Exception
    {
        try
        {
            toobig(100);
        }
        catch (Exception e)
        {
            context.put("e", e);
            assertEvalEquals("100", "$e.permittedSize");
        }
    }

    public void toobig(long permitted) throws Exception
    {
        throw new FileSizeLimitExceededException(permitted);
    }

    public static class FileUploadException extends Exception {}

    protected abstract static class SizeException extends FileUploadException
    {
        private final long permitted;
        protected SizeException(long permitted)
        {
            this.permitted = permitted;
        }
        public long getPermittedSize()
        {
            return this.permitted;
        }
    }

    public static class FileSizeLimitExceededException extends SizeException
    {
        public FileSizeLimitExceededException(long permitted)
        {
            super(permitted);
        }
    }

}
