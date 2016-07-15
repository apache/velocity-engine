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

import org.apache.velocity.VelocityContext;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The class who used to connect Script Engines with objects, such as scoped Bindings, in hosting applications. Each scope is a set of named
 * attributes whose values can be set and retrieved using the VelocityScriptContext methods. VelocityScriptContext also expose Readers and
 * Writers that can be used by the VelocityScriptEngines for input and output.
 *
 * No additional params required to do instantiate this. Has only the default constructor.
 */

public class VelocityScriptContext implements ScriptContext {

    /**
     *    A binding reference dedicated to  cover engine scope
     */
    private Bindings engineScope;


    /**
     *   A binding reference dedicated to  cover global scope
     */
    private Bindings globalScope;


    /**
     *   Script context reader  for system in put. In other words Reader to be used by the script to read input.
     */
    private Reader reader;


    /**
     *   The Writer for scripts to use when displaying output. It defaults to system.out
     */
    private Writer writer = new PrintWriter(System.out);


    /**
     *  A dedicated Writer used to display error output.
     */
    private Writer errorWriter;


    /**
     *    List which stores the scopes supported by the scripting engine
     */
    private static List<Integer> scopes;


    /**
     *    Default constructor which init the scopes and readers and writers
     */
    public VelocityScriptContext() {
         init();
    }


    /**
     *   Instantiates the scopes(global and engine), readers and writers for the context
     */
    private void init() {
        engineScope = new VelocityBindings();
        globalScope = null;
        reader = new InputStreamReader(System.in);
        writer = new PrintWriter(System.out, true);
        errorWriter = new PrintWriter(System.err, true);
    }


  /**
     *  Associates a Bindings instance with a particular scope in this ScriptContext
     * @param bindings  The Bindings to associate with the given scope
     * @param i  scope
     *  Throws:
     * java.lang.IllegalArgumentException - If no Bindings is defined for the specified scope value in ScriptContexts of this type.
     * java.lang.NullPointerException - if value of scope is ENGINE_SCOPE and the specified Bindings is null.
     */
    public void setBindings(Bindings bindings, int i)  {
        switch (i){
         case ENGINE_SCOPE:
             if(bindings == null) {
               throw new NullPointerException("Engine scope cannot be null");
             }
             engineScope = bindings;
         break;

         case GLOBAL_SCOPE:
             globalScope = bindings;
         break;

         default:
             throw new IllegalArgumentException("Invalid scope value");
        }
    }


  /**
     * @param i  scope
     * @return   The associated Bindings. Returns null if it has not been set.
     * throws java.lang.IllegalArgumentException - If no Bindings is defined for the specified scope value in ScriptContext of this type.
     */
    public Bindings getBindings(int i) {
       switch (i) {
         case ENGINE_SCOPE:
              return engineScope;
         case GLOBAL_SCOPE:
              return globalScope;
         default:
             throw new IllegalArgumentException("Invalid scope value");
        }
    }



  /**
     *  Sets the value of an attribute in a given scope.
     * @param s  attribute name
     * @param o  attribute value
     * @param i  scope
     * Throws
     * java.lang.IllegalArgumentException - if the name is empty or if the scope is invalid.
     * java.lang.NullPointerException - if the name is null.
     */
    public void setAttribute(String s, Object o, int i) {

         if(s == null) {
             throw new NullPointerException("Name cannot be null .");
         }

         switch (i) {
         case ENGINE_SCOPE:
              engineScope.put(s,o);
              return;
         case GLOBAL_SCOPE:
             if(globalScope != null) {
              globalScope.put(s,o);
             }
              return;
         default:
             throw new IllegalArgumentException("Invalid scope value");
        }
    }



  /**
     *  Gets the value of an attribute in a given scope.
     * @param s  name of the attribute
     * @param i  scope
     * @return  The value of the attribute. Returns null is the name does not exist in the given scope.
     * Throws :
     *  java.lang.IllegalArgumentException - if the name is empty or if the value of scope is invalid.
     * java.lang.NullPointerException - if the name is null.
     */
    public Object getAttribute(String s, int i) {

         if(s == null) {
             throw new NullPointerException("Name cannot be null .");
         }

        switch (i) {
        case ENGINE_SCOPE:
             return engineScope.get(s);
        case GLOBAL_SCOPE:
            if(globalScope != null) {
             return globalScope.get(s);
            }
            return null;
        default:
            throw new IllegalArgumentException("Invalid scope value");
       }

    }



  /**
     *   Remove an attribute in a given scope.
     * @param s   The name of the attribute to remove
     * @param i  The scope in which to remove the attribute
     * @return   The removed value
     * Throws:
     * java.lang.IllegalArgumentException - if the name is empty or if the scope is invalid.
     * java.lang.NullPointerException - if the name is null.
     */
    public Object removeAttribute(String s, int i) {

         if(s == null) {
             throw new NullPointerException("Name cannot be null .");
         }

        switch (i) {
        case ENGINE_SCOPE:
             return engineScope.remove(s);
        case GLOBAL_SCOPE:
            if(globalScope != null) {
             return globalScope.remove(s);
            }
            return null;
        default:
            throw new IllegalArgumentException("Invalid scope value");
       }

    }


  /**
     * Retrieves the value of the attribute with the given name in the scope occurring earliest in the search order.
     * The order is determined by the numeric value of the scope parameter (lowest scope values first.
     * @param s   name of the attribute to return
     * @return   The value of the attribute in the lowest scope for which an attribute with the given name is defined. Returns null
     * if no attribute with the name exists in any scope.
     * Throws:
     * java.lang.NullPointerException - if the name is null.
     * java.lang.IllegalArgumentException - if the name is empty.
     */
    public Object getAttribute(String s) {
         if(s == null) {
             throw new NullPointerException("Name cannot be null .");
         }

        if(engineScope.containsKey(s)) {
           return getAttribute(s,ENGINE_SCOPE);
        } else if (globalScope != null && (globalScope.containsKey(s))) {
           return getAttribute(s,GLOBAL_SCOPE);
        }
        return null;
    }


  /**
     *  Get the lowest scope in which an attribute is defined.
     * @param s name of the attribute
     * @return  The lowest scope. Returns -1 if no attribute with the given name is defined in any scope.
     * Throws:
     * java.lang.NullPointerException - if name is null.
     * java.lang.IllegalArgumentException - if name is empty.
     */
    public int getAttributesScope(String s) {

        if(s == null) {
             throw new NullPointerException("Name cannot be null .");
        }
        if(engineScope.containsKey(s)) {
           return ENGINE_SCOPE;
        } else if(globalScope != null && globalScope.containsKey(s)) {
           return GLOBAL_SCOPE;
        } else {
          return -1;
        }
    }


  /**
     *  Returns the Writer for scripts to use when displaying output.
     * @return
     */
    public Writer getWriter() {
        return writer;
    }


  /**
     *  Returns the Writer used to display error output.
     * @return
     */
    public Writer getErrorWriter() {
        return errorWriter;
    }


  /**
     *   Sets the Writer for scripts to use when displaying output.
     * @param writer
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }


  /**
     *  Sets the Writer used to display error output.
     * @param writer
     */
    public void setErrorWriter(Writer writer) {
        this.errorWriter = writer;
    }


  /**
     *  Returns a Reader to be used by the script to read input.
     * @return
     */
    public Reader getReader() {
        return reader;
    }


  /**
     *  Sets the Reader for scripts to read input .
     * @param reader
     */
    public void setReader(Reader reader) {
        this.reader = reader;
    }


  /**
     *  Returns immutable List of all the valid values for scope in the ScriptContext.
     * @return
     */
    public List<Integer> getScopes() {
        return scopes;
    }


    //Adding the two scopes the api supports in to a unmodifiable list
      static {
        scopes = new ArrayList<Integer>(2);
        scopes.add(ENGINE_SCOPE);
        scopes.add(GLOBAL_SCOPE);
        scopes = Collections.unmodifiableList(scopes);
    }
}
