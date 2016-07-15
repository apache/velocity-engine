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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VelocityScriptEngineFactory is used to describe and instantiate ScriptEngines.
 * There are two fundamental ways to instantiate this.
 *
 *   1. Create a factory by providing only name and version and allows to inherit default factory settings.
 *   2. Create a factory by providing all required attributes i.e  List<String> names, List<String> extensions
 *       List<String> mimeTypes , String name,String version, String langName, String langVersion
 */
public class VelocityScriptEngineFactory implements ScriptEngineFactory {

    /**
     *  names of the script engine
     */
    private List<String> names;


    /**
     * List of  extensions for script engine
     */
    private List<String> extensions;


    /**
     *  List of mime types for script engine
     */
    private List<String> mimeTypes;

    /**
     *  Default name of the engine
     */
    private String name = "velocity";


    /**
     * Default version of the engine
     */
    private String version = "1.8";


    /**
     *  Default script language name
     */
    private String langName = "velocity";


    /**
     * Default  version of the script language
     */
    private String langVersion = "1.8";


    /**
     * Provides full capability to change default engine settings
     *
     * @param names       Override default names for script engine
     * @param extensions  Override default extensions for script engine
     * @param mimeTypes   Override default mime types for script engine
     * @param name        Override default name for script engine
     * @param version     Override default version for script engine
     * @param langName    Override default language names for script engine
     * @param langVersion Override default language version for script engine
     */
    public VelocityScriptEngineFactory(List<String> names, List<String> extensions
            , List<String> mimeTypes
            , String name
            , String version
            , String langName
            , String langVersion
    ) {
        this.names = Collections.unmodifiableList(names);
        this.extensions = Collections.unmodifiableList(extensions);
        this.mimeTypes = Collections.unmodifiableList(mimeTypes);
        this.name = name;
        this.version = version;
        this.langName = langName;
        this.langVersion = langVersion;
    }

    /**
     * @param name    Override default name for script engine
     * @param version Override default version for script engine
     */
    public VelocityScriptEngineFactory(String name, String version) {
        this.name = name;
        this.version = version;
        initDefaultSettings();
    }

    /**
     * Simple Factory with all default settings
     */
    public VelocityScriptEngineFactory() {
        initDefaultSettings();
    }

    private void initDefaultSettings() {
        names = new ArrayList<String>(1);
        names.add("Velocity");
        names = Collections.unmodifiableList(names);
        extensions = new ArrayList<String>(3);
        extensions.add("vm");
        extensions.add("vtl");
        extensions.add("vhtml");
        extensions = Collections.unmodifiableList(extensions);
        mimeTypes = new ArrayList<String>(1);
        mimeTypes.add("text/x-velocity");
        mimeTypes = Collections.unmodifiableList(mimeTypes);
    }

  /**
     *  Returns the full name of the ScriptEngine.
     * @return
     */
    public String getEngineName() {
        return name;
    }


  /**
     *  Returns the version of the ScriptEngine.
     * @return
     */
    public String getEngineVersion() {
        return version;
    }


  /**
     *  Returns an immutable list of filename extensions, which generally identify scripts written in the language
     *  supported by this ScriptEngine.
     * @return
     */
    public List<String> getExtensions() {
        return extensions;
    }


  /**
     *  Returns an immutable list of mimetypes, associated with scripts that can be executed by the engine.
     * @return
     */
    public List<String> getMimeTypes() {
        return mimeTypes;
    }


  /**
     * Returns an immutable list of short names for the ScriptEngine, which may be used to identify the ScriptEngine by the ScriptEngineManager.
     * @return
     */
    public List<String> getNames() {
        return names;
    }


  /**
     *  Returns the name of the scripting langauge supported by this ScriptEngine.
     * @return
     */
    public String getLanguageName() {
        return langName;
    }


  /**
     *  Returns the version of the scripting language supported by this ScriptEngine.
     * @return
     */
    public String getLanguageVersion() {
        return langVersion;
    }



   /**
     *   Returns the value of an attribute whose meaning may be implementation-specific.
     * @param s
     * @return
     */
    public Object getParameter(String s) {
        if (s.equals(ScriptEngine.ENGINE)) {
            return getEngineName();
        } else if (s.equals(ScriptEngine.NAME)) {
            return getNames().get(0);
        } else if (s.equals(ScriptEngine.LANGUAGE)) {
            return getLanguageName();
        } else if (s.equals(ScriptEngine.ENGINE_VERSION)) {
            return getEngineVersion();
        } else if (s.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return getLanguageVersion();
        } else if (s.equals("THREADING")) {
            return "MULTITHREADED";
        } else {
            return null;
        }
    }



    /**
     *
     * @param s   Name of the Object to whom the method belongs to
     * @param s1  Name of the method
     * @param strings method arguments to be passed
     * @return  the method syntax for velocity script
     */
    public String getMethodCallSyntax(String s, String s1, String... strings) {
        StringBuilder syntax = new StringBuilder();
        syntax.append("$");
        syntax.append("{");
        syntax.append(s);
        syntax.append(".");
        syntax.append(s1);
        syntax.append("(");
        if (strings.length != 0) {
            int i = 0;
            for (; i < strings.length - 1; i++) {
                syntax.append("$" + strings[i]);
                syntax.append(", ");
            }
            syntax.append("$" + strings[i]);
        }
        syntax.append(")");
        syntax.append("}");
        return syntax.toString();
    }



    /**
     *
     * @param s  String to display
     * @return     //TODO
     */
    public String getOutputStatement(String s) {
        StringBuilder output = new StringBuilder();
        output.append("${context.getWriter().write(\"");
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case '"':
                output.append("\\\"");
                break;
            case '\\':
                output.append("\\\\");
                break;
            default:
                output.append(ch);
                break;
            }
        }
        output.append("\")}");
        return output.toString();
    }


  /**
     *   Returns A valid scripting language executable progam with given statements.
     * @param strings  scripting statements provided
     * @return the program from the statements given
     */
    public String getProgram(String... strings) {
        StringBuilder program = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            program.append(strings[i]);
            program.append("\n");
        }
        return program.toString();
    }

    /**
     *  Returns an instance of the ScriptEngine associated with this ScriptEngineFactory
     * @return
     */
    public ScriptEngine getScriptEngine() {
        return new VelocityScriptEngine(this);
    }
}
