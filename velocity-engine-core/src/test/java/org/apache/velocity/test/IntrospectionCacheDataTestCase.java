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
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.util.introspection.IntrospectionCacheData;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Checks that arrays are cached correctly in the Introspector.
 *
 * @author <a href="Alexey Pachenko">alex+news@olmisoft.com</a>
 * @version $Id$
 */
public class IntrospectionCacheDataTestCase extends TestCase
{

    private static class CacheHitCountingVelocityContext extends VelocityContext
    {
        public int cacheHit = 0;

        public IntrospectionCacheData icacheGet(Object key)
        {
            final IntrospectionCacheData result = super.icacheGet(key);
            if (result != null) {
                ++cacheHit;
            }
            return result;
        }

    }

    public void testCache() throws ParseErrorException, MethodInvocationException,
    ResourceNotFoundException, IOException
    {
        CacheHitCountingVelocityContext context = new CacheHitCountingVelocityContext();
        context.put("this", this);
        StringWriter w = new StringWriter();
        Velocity.evaluate(context, w, "test", "$this.exec('a')$this.exec('b')");
        assertEquals("[a][b]", w.toString());
        assertTrue(context.cacheHit > 0);
    }


    /**
     * For use when acting as a context reference.
     *
     * @param value
     * @return
     */
    public String exec(String value)
    {
        return "[" + value + "]";
    }

}
