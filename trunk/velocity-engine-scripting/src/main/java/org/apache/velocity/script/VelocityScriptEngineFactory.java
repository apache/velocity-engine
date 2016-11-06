package org.apache.velocity.script;

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met: Redistributions of source code 
 * must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of 
 * conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution. Neither the name of the Sun Microsystems nor the names of 
 * is contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission. 

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Factory class for the Velocity scripting interface. Please refer to the
 * javax.script.ScriptEngineFactory documentation for details.
 *
 * @author A. Sundararajan
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id: VelocityScriptEngineFactory.java$
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class VelocityScriptEngineFactory implements ScriptEngineFactory
{

    private static final String VELOCITY_NAME = "Velocity";
    private static final String VELOCITY_VERSION = "2.0";
    private static final String VELOCITY_LANGUAGE = "VTL";

    private static List<String> names;
    private static List<String> extensions;
    private static List<String> mimeTypes;

    private static Properties parameters;
    
    static
    {
        names = new ArrayList();
        names.add("velocity");
        names.add("Velocity");
        names = Collections.unmodifiableList(names);
        extensions = new ArrayList();
        extensions.add("vm");
        extensions.add("vtl");
        extensions.add("vhtml");
        extensions = Collections.unmodifiableList(extensions);
        mimeTypes = new ArrayList();
        mimeTypes.add("text/x-velocity");
        mimeTypes = Collections.unmodifiableList(mimeTypes);
        parameters = new Properties();
        parameters.put(ScriptEngine.NAME, VELOCITY_NAME);
        parameters.put(ScriptEngine.ENGINE_VERSION, VELOCITY_VERSION);
        parameters.put(ScriptEngine.ENGINE, VELOCITY_NAME);
        parameters.put(ScriptEngine.LANGUAGE, VELOCITY_LANGUAGE);
        parameters.put(ScriptEngine.LANGUAGE_VERSION, VELOCITY_VERSION);
        parameters.put("THREADING", "MULTITHREADED");
    }

    /**
     * get engine name
     * @return engine name, aka "Velocity"
     */
    public String getEngineName()
    { 
        return VELOCITY_NAME;
    }

    /**
     * get engine version
     * @return engine version string
     */
    public String getEngineVersion()
    {
        return VELOCITY_VERSION;
    }

    /**
     * get the list of file extensions handled by Velocity: vm, vtl, vhtml
     * @return extentions list
     */
    public List<String> getExtensions()
    {
        return extensions;
    }

    /**
     * get language name
     * @return language name, aka "VTL"
     */
    public String getLanguageName()
    {
        return VELOCITY_NAME;
    }

    /**
     * get language version (same as engine version)
     * @return language version string
     */
    public String getLanguageVersion()
    {
        return VELOCITY_VERSION;
    }

    /**
     * get Velocity syntax for calling method 'm' on bject 'obj' with provided arguments
     * @param obj
     * @param m
     * @param args
     * @return VTL call ${obj.m(args...)}
     */
    public String getMethodCallSyntax(String obj, String m, String... args)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("${");
        buf.append(obj);
        buf.append(".");
        buf.append(m);
        buf.append("(");
        if (args.length != 0)
        {
            int i = 0;
            for (; i < args.length - 1; i++)
            {
                buf.append("$" + args[i]);
                buf.append(", ");
            }
            buf.append("$" + args[i]);
        }        
        buf.append(")}");
        return buf.toString();
    }

    /**
     * get the list of Velocity mime types
     * @return singleton { 'text/x-velocity' }
     */
    public List<String> getMimeTypes()
    {
        return mimeTypes;
    }

    /**
     * get the list of names
     * @return { 'velocity', 'Velocity' }
     */
    public List<String> getNames()
    {
        return names;
    }

    /**
     * get VTL expression used to display specified string
     * @param toDisplay
     * @return escaped string #[[toDisplay]]#
     */
    public String getOutputStatement(String toDisplay)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("#[[").append(toDisplay).append("]]#");
        return buf.toString();
    }

    /**
     * get engine parameter for provided key
     * @param key
     * @return found parameter, or null
     */
    public String getParameter(String key)
    {
        return parameters.getProperty(key);
    }

    /**
     * get whole VTL program given VTL lines
     * @param statements VTL lines
     * @return lines concatenated with carriage returns
     */
    public String getProgram(String... statements)
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < statements.length; i++)
        {
            buf.append(statements[i]);
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * get a Velocity script engine
     * @return a new Velocity script engine
     */
    public ScriptEngine getScriptEngine()
    {
        return new VelocityScriptEngine(this);
    }
}
