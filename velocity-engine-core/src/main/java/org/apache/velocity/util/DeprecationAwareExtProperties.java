/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.velocity.util;


import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.DeprecatedRuntimeConstants;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * This class extends ExtProperties to handle deprecated propery key names.
 * @since 2.1
 * @version $Revision: $
 * @version $Id: DeprecationAwareExtProperties.java$
 *
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @deprecated it will disappear along with deprecated key names in 3.0.
 */
@Deprecated
public class DeprecationAwareExtProperties extends Hashtable<String, Object>
{
    /**
     * <p>Logger used to log the use of deprecated properties names.</p>
     * <p>Since at the time Velocity properties are set Velocity is not yet initialized,
     * this logger namespace can only be a child of the default logger name: <code>org.apache.velocity.deprecation</code>.</p>
     * <p>It won't honor the <code>runtime.log.instance</code> or <code>runtime.log.name</code> settings.</p>
     */
    protected static Logger logger = LoggerFactory.getLogger(RuntimeConstants.DEFAULT_RUNTIME_LOG_NAME + ".deprecation");

    /**
     * Emit a warning in the log for adeprecated property name
     * @param oldName old property name
     * @param newName new property name
     */
    protected void warnDeprecated(String oldName, String newName)
    {
        if (warned.add(oldName))
        {
            logger.warn("configuration key '{}' has been deprecated in favor of '{}'", oldName, newName);
        }
    }

    /**
     * Translate if needed a deprecated key into its replacement key, and emit a warning for deprecated keys
     * @param key provided key
     * @return translated key
     */
    protected String translateKey(String key)
    {
        // check for a replacement key
        String replacement = propertiesReplacementMap.get(key);
        if (replacement != null)
        {
            warnDeprecated(key, replacement);
            return replacement;
        }
        // check for a resource loader property
        int i = key.indexOf(".resource.loader.");
        if (i != -1)
        {
            replacement = "resource.loader." + key.substring(0, i + 1) + key.substring(i + 17);
            warnDeprecated(key, replacement);
            return replacement;
        }
        // check for a control scope property
        if (key.endsWith(".provide.scope.control"))
        {
            replacement = RuntimeConstants.CONTEXT_SCOPE_CONTROL + "." + key.substring(0, key.length() - 22);
            warnDeprecated(key, replacement);
            return replacement;
        }
        // looks good
        return key;
    }

    /**
     * Property getter which checks deprecated property keys
     * @param key provided key
     * @return found value under this key or under the corresponding deprecated one, if any
     */
    public Object get(String key)
    {
        return super.get(translateKey(key));
    }

    /**
     * Property setter which checks deprecated property keys
     * @param key provided key
     * @param value provided value
     * @return previous found value, if any
     */
    @Override
    public Object put(String key, Object value)
    {
        return super.put(translateKey(key), value);
    }

    /**
     * Property getter which checks deprecated property keys
     * @param key provided key
     * @return found value under this key or under the corresponding deprecated one, if any
     */
    public boolean containsKey(String key)
    {
        return super.containsKey(translateKey(key));
    }

    /**
     * Set of old property names for which a warning has already been emitted
     */
    private Set<String> warned = new HashSet<>();

    /**
     * Property keys replacement map, from old key name to new key name
     */
    static private Map<String, String> propertiesReplacementMap = new HashMap<>();

    static
    {
        {
            try
            {
                Field oldFields[] = DeprecatedRuntimeConstants.class.getDeclaredFields();
                for (Field oldField : oldFields)
                {
                    String name = oldField.getName();
                    if (!name.startsWith("OLD_")) throw new VelocityException("Could not initialize property keys deprecation map because DeprecatedRuntimeConstants." + name + " field isn't properly named");
                    if (oldField.getType() != String.class) continue;
                    String oldValue = (String)oldField.get(null);
                    if (oldValue == null) throw new VelocityException("Could not initialize property keys deprecation map because DeprecatedRuntimeConstants." + name + " field isn't initialized");
                    name = name.substring(4);
                    Field newField = RuntimeConstants.class.getDeclaredField(name);
                    String newValue = (String)newField.get(null);
                    if (newValue == null) throw new VelocityException("Could not initialize property keys deprecation map because RuntimeConstants." + name + " field isn't initialized");
                    if (!newValue.equals(oldValue))
                    {
                        propertiesReplacementMap.put(oldValue, newValue);
                    }
                }
            }
            catch (IllegalAccessException | NoSuchFieldException e)
            {
                throw new VelocityException("Could not initialize property keys deprecation map", e);
            }
        }
    }

}
