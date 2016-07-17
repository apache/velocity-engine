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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for testing EvaluateContext.  For testing purposes, this is a case insensitive
 * context.  
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class TestContext implements Context
{
    Context innerContext = new VelocityContext();
    Map<String, String> originalKeys = new HashMap<String, String>();
    
    public boolean containsKey(String key)
    {
        return innerContext.containsKey(normalizeKey(key));
    }

    public Object get(String key)
    {
        return innerContext.get(normalizeKey(key));
    }

    public String[] getKeys()
    {
        return originalKeys.values().toArray(new String[originalKeys.size()]);
    }

    public Object put(String key, Object value)
    {
        String normalizedKey = normalizeKey(key);
        originalKeys.put(key, normalizedKey);
        return innerContext.put(normalizedKey, value);
    }

    public Object remove(String key)
    {
        originalKeys.remove(key);
        return innerContext.remove(normalizeKey(key));
    }

    private String normalizeKey(String key)
    {
        if (key == null)
        {
            return null;
        }
        else
        {
            return key.toUpperCase();
        }
    }
}
