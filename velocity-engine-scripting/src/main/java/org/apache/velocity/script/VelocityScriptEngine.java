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
 * Main class for the Velocity script engine.
 * All the variants of the eval() methods return the writer. The default writer is a PrintWriter towards System.out.
 * To specify a specific writer, use getContext().setWriter(writer). To get a resulting string, pass a StringWriter.
 *
 * You can specify a pathname towards a Velocity properties file using the "org.apache.velocity.script.properties" key,
 * either as a ScriptContext attribute, or as a System property.
 *
 * Example use:
 * <pre>
 *     ScriptEngine vel = new VelocityScriptEngine();
 *     vel.getContext().setAttribute(VelocityScriptEngine.VELOCITY_PROPERTIES_KEY, "path/to/velocity.properties");
 *     vel.getContext().setWriter(new StringWriter());
 *     vel.put("foo","World");
 *     Object result = vel.eval("Hello $foo !");
 *     String helloWorld = result.toString()
 * </pre>
 *
 * Please refer to the javax.script.ScriptEngine documentation for additional details.
 *
 * @author A. Sundararajan
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id: VelocityScriptEngine.java$
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ResourceLoader2;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class VelocityScriptEngine extends AbstractScriptEngine implements Compilable
{
    /**
     * Key used to provide this engine with the pathname of the Velocity properties file.
     * This key is first searched in the ScriptContext attributes, then as a System property
     */
    public static final String VELOCITY_PROPERTIES_KEY = "org.apache.velocity.script.properties";

    // my factory, may be null
    private volatile ScriptEngineFactory factory;
    private volatile RuntimeInstance velocityEngine;

    /**
     * contructs a new Velocity script engine, linked to the given factory
     * @param factory
     */
    public VelocityScriptEngine(ScriptEngineFactory factory)
    {
        this.factory = factory;
    }

    /**
     * constructs a new standalone Velocity script engine
     */
    public VelocityScriptEngine()
    {
        this(null);
    }

    /**
     * get the internal Velocity RuntimeInstance
     * @return the internal Velocity RuntimeInstance
     */
    protected RuntimeInstance getVelocityEngine()
    {
        return velocityEngine;
    }

    /**
     * Evaluate the given script.
     * If you wish to get a resulting string, call getContext().setWriter(new StringWriter()) then call toString()
     * on the returned writer.
     * @param str script source
     * @param ctx script context
     * @return the script context writer (by default a PrintWriter towards System.out)
     * @throws ScriptException
     */
    public Object eval(String str, ScriptContext ctx)
                       throws ScriptException
    {	
        return eval(new StringReader(str), ctx);
    }

    /**
     * Evaluate the given script.
     * If you wish to get a resulting string, call getContext().setWriter(new StringWriter()) then call toString()
     * on the returned writer.
     * @param reader script source reader
     * @param ctx script context
     * @return the script context writer (by default a PrintWriter towards System.out)
     * @throws ScriptException
     */
    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException
    { 
        initVelocityEngine(ctx);
        String fileName = getFilename(ctx);
        VelocityContext vctx = getVelocityContext(ctx);
        Writer out = ctx.getWriter();
        if (out == null)
        {
            out = new StringWriter();
            ctx.setWriter(out);
        }
        try
        {
            velocityEngine.evaluate(vctx, out, fileName, reader);
            out.flush();
        }
        catch (Exception exp)
        {
            throw new ScriptException(exp);
        }
        return out;
    }

    /**
     * get the factory used to create this script engine
     * @return factory
     */
    public ScriptEngineFactory getFactory()
    {
        if (factory == null)
        {
            synchronized (this)
            {
	            if (factory == null)
                {
	                factory = new VelocityScriptEngineFactory();
	            }
            }
        }
        return factory;
    }

    /**
     * creates a new Bindings to be used with this script
     * @return new bindings
     */
    public Bindings createBindings()
    {
        return new SimpleBindings();
    }

    private void initVelocityEngine(ScriptContext ctx)
    {
        if (ctx == null)
        {
            ctx = getContext();
        }
        if (velocityEngine == null)
        {
            synchronized (this)
            {
                if (velocityEngine != null) return;

                Properties props = getVelocityProperties(ctx);
                RuntimeInstance tmpEngine = new RuntimeInstance();
                try
                {
                    if (props != null)
                    {
                        tmpEngine.init(props);
                    }
                    else
                    {
                        tmpEngine.init();
                    }
                }
                catch (RuntimeException rexp)
                {
                    throw rexp;
                }
                catch (Exception exp)
                {
                    throw new RuntimeException(exp);
                }
                velocityEngine = tmpEngine;
            }
        }
    }

    protected static VelocityContext getVelocityContext(ScriptContext ctx)
    {
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        Bindings globalScope = ctx.getBindings(ScriptContext.GLOBAL_SCOPE);        
        Bindings engineScope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        if (globalScope != null)
        {
            return new VelocityContext(engineScope, new VelocityContext(globalScope));
        }
        else
        {
            return new VelocityContext(engineScope);
        }
    }

    protected static String getFilename(ScriptContext ctx)
    {
        Object fileName = ctx.getAttribute(ScriptEngine.FILENAME);
        return fileName != null? fileName.toString() : "<unknown>";
    }

    protected static Properties getVelocityProperties(ScriptContext ctx)
    {
        try
        {
            Object props = ctx.getAttribute(VELOCITY_PROPERTIES_KEY);
            if (props instanceof Properties)
            {
                return (Properties) props;
            }
            else
            {
                String propsName = System.getProperty(VELOCITY_PROPERTIES_KEY);
                if (propsName != null)
                {
                    File propsFile = new File(propsName);
                    if (propsFile.exists() && propsFile.canRead())
                    {
                        Properties p = new Properties();
                        p.load(new FileInputStream(propsFile));
                        return p;
                    }               
                }
            }
        }
        catch (Exception exp)
        {
            System.err.println(exp);
        }            
        return null;
    }

    /**
     * Compile a template
     * @param script template source
     * @return compiled template
     * @throws ScriptException
     */
    public CompiledScript compile(String script) throws ScriptException
    {
        return compile(new StringReader(script));
    }

    /**
     * Compile a template
     * @param script template source
     * @return compiled template
     * @throws ScriptException
     */
    public CompiledScript compile(Reader script) throws ScriptException
    {
        initVelocityEngine(null);
        ResourceLoader2 resourceLoader = new SingleResourceReader(script);
        Template template = new Template();
        template.setRuntimeServices(velocityEngine);
        template.setResourceLoader(resourceLoader);
        try
        {
            template.process();
        }
        catch(Exception e)
        {
            // CB TODO - exception may have line/col informations, that ScriptException can exploit
            throw new ScriptException(e);
        }
        return new VelocityCompiledScript(this, template);
    }

    // a dummy resource reader class, serving a single resource given by the provided resource reader
    protected static class SingleResourceReader extends StringResourceLoader
    {
        private Reader reader;

        public SingleResourceReader(Reader r)
        {
            reader = r;
        }

        @Override
        public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException
        {
            return reader;
        }
    }
}
