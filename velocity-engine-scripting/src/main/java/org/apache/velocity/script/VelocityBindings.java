package org.apache.velocity.script;

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

import org.apache.velocity.context.Context;
import org.apache.velocity.script.util.ScriptUtil;

import javax.script.Bindings;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A mapping of key/value pairs, all of whose keys are Strings.
 * There are two fundamental ways to instantiate this.
 * 1. By providing a pre enriched map as initial values which will override the default VelocityBindings values.
 * 2. Default constructor which will adheres to the default setting with an starting empty map.
 */
public class VelocityBindings implements Bindings, Context {

  /**
     * Map which keeps the binding of names and associated values
     */
    private Map<String,Object> map;

  /**
     *
     * @param map  pre created binding map passed to initialize.
     */
    public VelocityBindings(Map<String,Object> map) {
        if(map == null) {
            ScriptUtil.addExceptionToErrorWriter(new NullPointerException("Cannot pass a null map to initialize VelocityBindings"));
          throw new NullPointerException("Cannot pass a null map to initialize VelocityBindings");
        }
        this.map = map;
    }

  /**
     * Default constructor which creates a new Map instance
     */
    public VelocityBindings(){
        this.map = new HashMap<String, Object>();
    }

  /**
     *   Set a named value.
     * @param s The name associated with the value.
     * @param o The value associated with the name.
     * @return   The value previously associated with the given key/name. Returns null if no value was previously associated with the name.
     */
    public Object put(String s, Object o) {
        validateKey(s);
        return map.put(s,o);
    }

    /**
     *   Inherits from org.apache.velocity.Context
     * @param key The name of the desired value.
     * @return  Returns the value associate with the given key
     */
    public Object get(String key) {
        validateKey(key);
        return map.get(key);
    }

    /**
     *   Inherits from org.apache.velocity.Context
     * @param key The name of the desired value.
     * @return  Returns the value associate with the given key
     */
    public Object get(Object key) {
        return get(String.valueOf(key));
    }    
    
    /**
     *  Inherits from org.apache.velocity.Context
     * @return   All names added in the map
     */
    public String[] getKeys() {
        return map.keySet().toArray(new String[map.size()]);
    }

  /**
     *
     * @param map , All the values in this map will be add in to the binding
     */
    public void putAll(Map<? extends String, ? extends Object> map) {
         for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
             String key = entry.getKey();
             validateKey(key);
             put(key, entry.getValue());
         }
    }

  /**
     * Clears the map
     */
    public void clear() {
     map.clear();
    }

  /**
     *
     * @return  returns the keys as a Set collection
     */
    public Set<String> keySet() {
        return map.keySet();
    }

  /**
     *
     * @return  Returns all values associated win the map
     */
    public Collection<Object> values() {
        return map.values();
    }

  /**
     *  EntrySet in interface java.util.Map<java.lang.String,java.lang.Object>
     * @return   entrySet
     */
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

  /**
     *
     * @return the size of the map
     */
    public int size() {
        return map.size();
    }

  /**
     *
     * @return whether the map is empty
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

  /**
     *  Check whether the given name is already binded to a value
     * @param o  name
     * @return  true if the given name contains a binding else false
     */
    public boolean containsKey(String k) {
        validateKey(k);
        return map.containsKey(k);
    }

  /**
     *  Check whether the given name is already binded to a value
     * @param o  name
     * @return  true if the given name contains a binding else false
     */
    public boolean containsKey(Object k) {
        return containsKey(String.valueOf(k));
    }

    
  /**
     *  Check whether the given value is already binded to a name in the map
     * @param o  name
     * @return  true if the given value contains a binding else false
     */
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

  /**
     *   Removes the mapping for this key from this map if it is present (optional operation).
     * @param o  name
     * @return  Remove the value binded to the given name and the name itself from the mapping
     */
    public Object remove(String k) {
        validateKey(k);
        return map.remove(k);
    }

  /**
     *   Removes the mapping for this key from this map if it is present (optional operation).
     * @param o  name
     * @return  Remove the value binded to the given name and the name itself from the mapping
     */
    public Object remove(Object k) {
        return remove(String.valueOf(k));
    }

    
 /**
     *  Validates key and throw corresponding exceptions for JSR 223 compliance
     * Throws: java.lang.NullPointerException - if key is null
     *             java.lang.ClassCastException - if key is not String
     *             java.lang.IllegalArgumentException - if key is empty String
     * @param key   which used to validate the binding
     */
    private void validateKey(String key) {
        if (key == null) {
            ScriptUtil.addExceptionToErrorWriter(new NullPointerException("Cannot pass a null map to initialize VelocityBindings"));
            throw new NullPointerException("The key cannot be null..!!");
        }
        if (!(key instanceof String)) {
            ScriptUtil.addExceptionToErrorWriter(new ClassCastException("The key must be of the type String..!!"));
            throw new ClassCastException("The key must be of the type String..!!");
        }
        if (key.equals("")) {
            ScriptUtil.addExceptionToErrorWriter(new IllegalArgumentException("The key cannot be empty..!!"));
            throw new IllegalArgumentException("The key cannot be empty..!!");
        }
    }

}
