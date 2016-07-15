package org.apache.velocity.script.test;


import junit.framework.TestCase;
import org.apache.velocity.script.VelocityScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public abstract class AbstractScriptTest  extends TestCase {
   protected ScriptEngine engine;
   protected  ScriptEngineFactory engineFactory;
   protected  ScriptEngineManager manager;

    @Override
    public void setUp() {
        manager = new ScriptEngineManager();
    }

    public void setupEngine(ScriptEngineFactory scriptEngineFactory){
        manager.registerEngineName("velocity", scriptEngineFactory);
        engine = manager.getEngineByName("velocity");
    }

    public void setupWithDefaultFactory() {
        manager.registerEngineName("velocity", new VelocityScriptEngineFactory());
        engine = manager.getEngineByName("velocity");
        engineFactory = engine.getFactory();
    }
}
