package org.apache.velocity.test.misc;

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

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import java.io.Reader;

/**
 * Resource Loader that always throws an exception.  Used to test
 * that RuntimeExceptions are passed through.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class ExceptionGeneratingResourceLoader extends ResourceLoader
{

    public void init(ExtProperties configuration)
    {
    }

    public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException
    {
        throw new RuntimeException("exception");
    }

    public boolean isSourceModified(Resource resource)
    {
        return false;
    }

    public long getLastModified(Resource resource)
    {
        return 0;
    }

}
