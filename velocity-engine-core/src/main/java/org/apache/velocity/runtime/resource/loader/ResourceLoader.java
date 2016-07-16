package org.apache.velocity.runtime.resource.loader;

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

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.velocity.io.UnicodeInputStream;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCacheImpl;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.commons.collections.ExtendedProperties;

/**
 * This is abstract class the all text resource loaders should
 * extend.
 *
 * @deprecated - use {@link ResourceLoader2}
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public @Deprecated abstract class ResourceLoader extends ResourceLoader2
{
    /**
     * Get the InputStream that the Runtime will parse
     * to create a template.
     *
     * @param source
     * @return The input stream for the requested resource.
     * @throws ResourceNotFoundException
     * @deprecated Use {@link #getResourceReader(String, String)}
     */
    public
    @Deprecated
    abstract InputStream getResourceStream(String source)
            throws ResourceNotFoundException;

    /**
     * Get the Reader that the Runtime will parse
     * to create a template.
     *
     * @param source
     * @return The reader for the requested resource.
     * @throws ResourceNotFoundException
     * @since 2.0
     */
    public Reader getResourceReader(String source, String encoding)
            throws ResourceNotFoundException
    {
        /*
         * We provide a default implementation that relies on the deprecated method getResourceStream()
         * to enhance backward compatibility. The day getResourceStream() is removed, this method should
         * become abstract.
         */
        InputStream rawStream = null;
        try
        {
            rawStream = getResourceStream(source);
            return buildReader(rawStream, encoding);
        }
        catch(IOException ioe)
        {
            if (rawStream != null)
            {
                try
                {
                    rawStream.close();
                }
                catch (IOException e) {}
            }
            String msg = "Exception while loading resousrce " + source;
            log.error(msg, ioe);
            throw new VelocityException(msg, ioe);
        }
    }
}
