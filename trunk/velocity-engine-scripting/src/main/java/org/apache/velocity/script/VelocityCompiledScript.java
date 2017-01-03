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

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.io.Writer;

public class VelocityCompiledScript extends CompiledScript
{
    protected VelocityScriptEngine engine;
    protected Template template;

    public VelocityCompiledScript(VelocityScriptEngine e, Template t)
    {
        engine = e;
        template = t;
    }

    @Override
    public Object eval(ScriptContext scriptContext) throws ScriptException
    {
        VelocityContext velocityContext = VelocityScriptEngine.getVelocityContext(scriptContext);
        Writer out = scriptContext.getWriter();
        if (out == null)
        {
            out = new StringWriter();
            scriptContext.setWriter(out);
        }
        try
        {
            template.merge(velocityContext, out);
            out.flush();
        }
        catch (Exception exp)
        {
            throw new ScriptException(exp);
        }
        return out;
    }

    @Override
    public ScriptEngine getEngine()
    {
        return engine;
    }
}
