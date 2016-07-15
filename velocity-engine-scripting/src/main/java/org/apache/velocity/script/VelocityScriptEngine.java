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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.script.util.ScriptResourceHolder;
import org.apache.velocity.script.util.ScriptUtil;

import javax.script.*;
import java.io.*;
import java.util.Properties;

/**
 *  VelocityScriptEngine is the fundamental class whose methods inherited from .javax.script.ScriptEngine interface
 * These methods provide basic scripting functionality. This includes methods that execute scripts, and ones that set and get values.
 * The values are key/value pairs of two types.
 * This Provides a standard implementation for several of the variants of the eval method.
 *  eval(Reader)
 * eval(String)
 * eval(String, Bindings)
 * eval(Reader, Bindings)
 *
 * There are two ways to instantiate VelocityScriptEngine. One is directly using the script engine factory which was used to create this
 * engine. Other one is by passing the factory and the pre defined bindings required for the engine.
 */
public class VelocityScriptEngine implements ScriptEngine {

    /**
     * ScriptEngineFactory reference from whom this engine got created
     */
    private ScriptEngineFactory scriptEngineFactory;


    /**
     * Velocity core engine reference
     */
    private VelocityEngine velocityEngine;


    /**
     * unmodifiable property name which is ued to obtain properties from context as well as from system to initialize velocity core engine
     */
    public static final String VELOCITY_PROPERTIES = "org.apache.velocity.engine.properties";


    /**
     * Default velocity log tag
     */
    public static final String DEFAULT_LOG_TAG = "default_log_tag";


    /**
     * script context reference which belongs to this engine instance
     */
    private ScriptContext scriptContext;


    /**
     * Constructor which gets created engine factory reference as input
     *
     * @param scriptEngineFactory ScriptEngineFactory reference from whom this engine got created
     */
    public VelocityScriptEngine(ScriptEngineFactory scriptEngineFactory) {
        this.scriptEngineFactory = scriptEngineFactory;
        this.scriptContext = new VelocityScriptContext();
        ScriptUtil.setScriptContext(scriptContext);
    }

    /**
     * @param scriptEngineFactory ScriptEngineFactory reference from whom this engine got created
     * @param bindings            required binding needs to initialize this engine, unless it defaults to engine scope
     */
    public VelocityScriptEngine(ScriptEngineFactory scriptEngineFactory, Bindings bindings) {
        this.scriptEngineFactory = scriptEngineFactory;
        this.scriptContext = new VelocityScriptContext();
        ScriptUtil.setScriptContext(scriptContext);
        if (bindings != null) {
            this.scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        } else {
            ScriptUtil.addExceptionToErrorWriter(new NullPointerException("Bindings cannot be null"));
            throw new NullPointerException("Bindings cannot be null");
        }
    }


    /**
     * @return script engine factory who created this engine
     */
    public ScriptEngineFactory getFactory() {

        //        if null return a newly created one
        if (scriptEngineFactory == null) {
            createNewFactory();
        }
        return scriptEngineFactory;
    }

    private void createNewFactory() {
//          Added creation inside sync block to avoid creating two factories from a engine by two parallel threads at the same time.
//          Also the additional null check out from sync block is to avoid every  thread to get blocked inside it even there is an already created factory.
        synchronized (this) {
            if (scriptEngineFactory == null) {
                scriptEngineFactory = new VelocityScriptEngineFactory();
            }
        }
    }

    /**
     * Creates the velocity core engine by initializing it from reading property file/system properties
     *
     * @param context
     */
    private void constructVelocityEngine(ScriptContext context) {

        Properties props = getPropertiesFromContext(context);
        //Check if property exists in context
        if (props != null) {
            initVelocityEngine(props);
            return;
        } else {
            props = getPropertiesFromSystem();
            //Check if properties exists in System
            if (props != null) {
                initVelocityEngine(props);
                return;
            }
        }
        //Init velocity engine with default settings
        initVelocityEngine();
    }

    /**
     * Init velocity engine without properties.
     */
    private void initVelocityEngine() {

        if (velocityEngine != null) {

//            Add sync block from a parallel thread creating two velocity engine instances
            synchronized (this) {
                velocityEngine = new VelocityEngine();
                velocityEngine.init();
            }
        }
    }

    /**
     * Initializes the velocity engine with pre defined properties
     *
     * @param props
     */
    private void initVelocityEngine(Properties props) {
        if (velocityEngine == null) {
            synchronized (this) {
                velocityEngine = new VelocityEngine();
                velocityEngine.init(props);
            }
        }
    }

    /**
     * Obtain properties from a property file which is taken from a system property
     *
     * @return
     */
    private Properties getPropertiesFromSystem() {
        String propFileName = System.getProperty(VELOCITY_PROPERTIES);
        File propFile = new File(propFileName);
        if (propFile.exists()) {
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(propFile));
                return properties;
            } catch (IOException e) {
                ScriptUtil.addExceptionToErrorWriter(e);
                return null;
            }
        }
        //TODO log error msg saying no such property file
        return null;
    }

    private Properties getPropertiesFromContext(ScriptContext context) {
        Object props = context.getAttribute(VELOCITY_PROPERTIES);
        if (props instanceof Properties) {
            return (Properties) props;
        } else {
            return null;
        }
    }


    /**
     * Causes the immediate execution of the script whose source is the String passed as the first argument. The script may be
     * re-parsed or recompiled before execution. State left in the engine from previous executions, including variable values and
     * compiled procedures may be visible during this execution.
     *
     * @param s             The script to be executed by the script engine.
     * @param scriptContext A ScriptContext exposing sets of attributes in different scopes. The meanings of the
     *                      scopes ScriptContext.GLOBAL_SCOPE, and ScriptContext.ENGINE_SCOPE are defined in the specification.
     * @return The value returned from the execution of the script.
     * @throws ScriptException
     */
    public Object eval(String s, ScriptContext scriptContext) throws ScriptException {
            return eval(new StringReader(s),scriptContext);
    }


    /**
     * Same as eval(String, ScriptContext) where the source of the script is read from a Reader.
     *
     * @param reader        The source of the script to be executed by the script engine.
     * @param scriptContext The ScriptContext passed to the script engine.
     * @return The value returned from the execution of the script.
     * @throws ScriptException if an error occurrs in script.
     *                         java.lang.NullPointerException - if either argument is null.
     */
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {

        if (reader == null) {
            throw new NullPointerException("Reader passed cannot be null");
        }
        constructVelocityEngine(scriptContext);
        VelocityContext velocityContext = getVelocityContext(scriptContext);

        Writer outPut;
        if (scriptContext.getWriter() != null) {
            outPut = scriptContext.getWriter();
        } else {
            outPut = new StringWriter();
        }
        boolean result;

        try {
            //Check for velocity tools vm file
            if (scriptContext.getAttribute(VelocityScriptEngine.FILENAME) != null) {
                Template template = null;
                String fileName = scriptContext.getAttribute(VelocityScriptEngine.FILENAME).toString();
                //Cache hit
                if (ScriptResourceHolder.hasTemplate(fileName)) {
                    template = ScriptResourceHolder.getTemplate(fileName);
                } else {
                    try {
                        template = velocityEngine.getTemplate(fileName);
                        ScriptResourceHolder.putTemplate(fileName,template);
                        template.merge(velocityContext, outPut);
                    } catch(ResourceNotFoundException e1){
                    }

                }
            }

            result = velocityEngine.evaluate(velocityContext, outPut, VelocityScriptEngine.DEFAULT_LOG_TAG, reader);

        } catch (Exception exp) {
            ScriptUtil.addExceptionToErrorWriter(exp);
            throw new ScriptException(exp);
        }
        return String.valueOf(result);
    }

    /**
     * Executes the specified script. The default ScriptContext for the ScriptEngine is used.
     *
     * @param s The script language source to be executed.
     * @return The value returned from the execution of the script.
     * @throws ScriptException - if error occurrs in script.
     *                         java.lang.NullPointerException - if either argument is null.
     */
    public Object eval(String s) throws ScriptException {
        return eval(s, scriptContext);
    }


    /**
     * Same as eval(String) except that the source of the script is provided as a Reader
     *
     * @param reader The source of the script.
     * @return The value returned by the script.
     * @throws ScriptException
     */
    public Object eval(Reader reader) throws ScriptException {
        return eval(reader, scriptContext);
    }


    /**
     * Executes the script using the Bindings argument as the ENGINE_SCOPE Bindings of the ScriptEngine during the script
     * execution. The Reader, Writer and non-ENGINE_SCOPE Bindings of the default ScriptContext are used.
     * The ENGINE_SCOPE Bindings of the ScriptEngine is not changed, and its mappings are unaltered by the script execution.
     *
     * @param s        The source for the script.
     * @param bindings The Bindings of attributes to be used for script execution.
     * @return The value returned by the script.
     * @throws ScriptException
     */
    public Object eval(String s, Bindings bindings) throws ScriptException {
        ScriptContext scriptContext = getGeneratedScriptContextFromBinding(bindings);
        return eval(new StringReader(s), scriptContext);
    }


    /**
     * Same as eval(String, Bindings) except that the source of the script is provided as a Reader.
     *
     * @param reader   The source of the script.
     * @param bindings The Bindings of attributes to be used for script execution.
     * @return The value returned by the script.
     * @throws ScriptException
     */
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        ScriptContext scriptContext = getGeneratedScriptContextFromBinding(bindings);
        return eval(reader, scriptContext);
    }

    private ScriptContext getGeneratedScriptContextFromBinding(Bindings bindings) {
        ScriptContext tmpContext = new VelocityScriptContext();
        Bindings globalScope = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);

        //Setting global and engine scopes to context
        if (globalScope != null) {
            tmpContext.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
        }

        if (bindings != null) {
            tmpContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        } else {
            ScriptUtil.addExceptionToErrorWriter(new NullPointerException("Engine scope Bindings cannot be null."));
            throw new NullPointerException("Engine scope Bindings cannot be null.");
        }

        tmpContext.setReader(scriptContext.getReader());
        tmpContext.setWriter(scriptContext.getWriter());
        tmpContext.setErrorWriter(scriptContext.getErrorWriter());

        return tmpContext;
    }

    /**
     * Sets a key/value pair in the state of the ScriptEngine that may either create a Java Language Binding to be used in the
     * execution of scripts or be used in some other way, depending on whether the key is reserved. Must have the same effect
     * as getBindings(ScriptContext.ENGINE_SCOPE).put.
     *
     * @param s The name of named value to add
     * @param o The value of named value to add.
     *          Throws:
     *          java.lang.NullPointerException - if key is null.
     *          java.lang.IllegalArgumentException - if key is empty.
     */
    public void put(String s, Object o) {

        if (s == null) {
            ScriptUtil.addExceptionToErrorWriter(new NullPointerException("Name cannot be null"));
            throw new NullPointerException("Name cannot be null");
        }

        if ("".equals(s)) {
            ScriptUtil.addExceptionToErrorWriter(new IllegalArgumentException("Name cannot be empty"));
            throw new IllegalArgumentException("Name cannot be empty");
        }

        Bindings engineScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        engineScope.put(s, o);
    }


    /**
     * Retrieves a value set in the state of this engine. The value might be one which was set using setValue or some other value in
     * the state of the ScriptEngine, depending on the implementation. Must have the same effect as getBindings
     * (ScriptContext.ENGINE_SCOPE).get
     *
     * @param s The key whose value is to be returned
     * @return the value for the given key
     *         Throws:
     *         java.lang.NullPointerException - if key is null.
     *         java.lang.IllegalArgumentException - if key is empty.
     */
    public Object get(String s) {

        if (s == null) {
            ScriptUtil.addExceptionToErrorWriter(new NullPointerException("Name cannot be null"));
            throw new NullPointerException("Name cannot be null");
        }

        if ("".equals(s)) {
            ScriptUtil.addExceptionToErrorWriter(new IllegalArgumentException("Name cannot be empty"));
            throw new IllegalArgumentException("Name cannot be empty");
        }

        Bindings engineScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        return engineScope.get(s);
    }


    /**
     * The Bindings instances that are returned must be identical to those returned by the getBindings method of ScriptContext
     * called with corresponding arguments on the default ScriptContext of the ScriptEngine.
     *
     * @param i scope
     * @return The Bindings with the specified scope.
     */
    public Bindings getBindings(int i) {
        return scriptContext.getBindings(i);
    }


    /**
     * Sets a scope of named values to be used by scripts. The possible scopes are:
     * ScriptContext.ENGINE_SCOPE - The specified Bindings replaces the engine scope of the ScriptEngine.
     * ScriptContext.GLOBAL_SCOPE - The specified Bindings must be visible as the GLOBAL_SCOPE.
     * Any other value of scope defined in the default ScriptContext of the ScriptEngine.
     *
     * @param bindings The Bindings for the specified scope.
     * @param i        The specified scope. Either ScriptContext.ENGINE_SCOPE, ScriptContext.GLOBAL_SCOPE, or any other valid value of scope.
     */
    public void setBindings(Bindings bindings, int i) {
        scriptContext.setBindings(bindings, i);
    }


    /**
     * @return A Bindings that can be used to replace the state of this ScriptEngine.
     */
    public Bindings createBindings() {
        return new VelocityBindings();
    }

    /**
     * Returns the default ScriptContext of the ScriptEngine whose Bindings, Reader and Writers are used for script executions when no ScriptContext is specified.
     *
     * @return The default ScriptContext of the ScriptEngine.
     */
    public ScriptContext getContext() {
        return scriptContext;
    }


    /**
     * Sets the default ScriptContext of the ScriptEngine whose Bindings, Reader and Writers are used for script executions when no ScriptContext is specified.
     *
     * @param scriptContext A ScriptContext that will replace the default ScriptContext in the ScriptEngine.
     *                      Throws: java.lang.NullPointerException - if context is null.
     */
    public void setContext(ScriptContext scriptContext) {
        if (scriptContext == null) {
            throw new NullPointerException("script context cannot be null");
        }

        this.scriptContext = scriptContext;
    }

    private VelocityContext getVelocityContext(ScriptContext ctx) {
        Bindings engineScope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        if (ctx.getBindings(ScriptContext.GLOBAL_SCOPE) == null) {
            return new VelocityContext(engineScope);
        } else {
            return new VelocityContext(engineScope, new VelocityContext(ctx.getBindings(ScriptContext.GLOBAL_SCOPE)));
        }
    }

    private String getTargetFilename(ScriptContext ctx) {
        Object fileName = ctx.getAttribute(ScriptEngine.FILENAME);
        if (fileName != null) {
            return fileName.toString();
        } else {
            return "No-Such-File";
        }
    }
}
