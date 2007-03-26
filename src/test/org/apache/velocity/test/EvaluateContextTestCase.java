package org.apache.velocity.test;

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

import junit.framework.TestCase;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.EvaluateContext;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.test.misc.TestContext;

/**
 * Tests scope of EvaluateContext.  
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class EvaluateContextTestCase extends TestCase
{
    public void testLocalscopePutDoesntLeakButGetDoes() 
    throws Exception
    {
        RuntimeInstance instance;
        
        instance = new RuntimeInstance();
        instance.setProperty(RuntimeConstants.VM_CONTEXT_LOCALSCOPE, Boolean.TRUE);
        instance.init();

        VelocityContext base = new VelocityContext();
        base.put("outsideVar", "value1");

        EvaluateContext evc = new EvaluateContext(new InternalContextAdapterImpl(base), instance);
        evc.put("newLocalVar", "value2");

        // New variable put doesn't leak
        assertNull(base.get("newLocalVar"));
        assertEquals("value2", evc.get("newLocalVar"));

        // But we can still get to "outsideVar"
        assertEquals("value1", evc.get("outsideVar"));

        // If we decide to try and set outsideVar it won't leak
        evc.put("outsideVar", "value3");
        assertEquals("value3", evc.get("outsideVar"));
        assertEquals("value1", base.get("outsideVar"));
        
        assertEquals(2, evc.getKeys().length);
    }

    /**
     * Test that local context can be configured.
     * @throws Exception
     */
    public void testSetLocalContext()
    throws Exception
    {
        RuntimeInstance instance = new RuntimeInstance();
        instance.setProperty(RuntimeConstants.EVALUATE_CONTEXT_CLASS, TestContext.class.getName());
        instance.init();

        VelocityContext base = new VelocityContext();
        base.put("outsideVar", "value1");
        EvaluateContext evc = new EvaluateContext(new InternalContextAdapterImpl(base), instance);

        // original entry
        assertEquals(1,evc.getKeys().length);
        
        // original plus local entry
        evc.put("test","result");
        assertEquals(2,evc.getKeys().length);
        
        // local context is case insensitive, so the count remains the same
        evc.put("TEST","result");
        assertEquals(2,evc.getKeys().length);

        assertEquals("result",evc.get("test"));
        assertEquals("result",evc.get("TEst"));
    
        assertNull(evc.get("OUTSIDEVAR"));
    }

    public void testSetLocalContextWithErrors()
    throws Exception
    {
        VelocityContext base = new VelocityContext();

        try 
        {
            // initialize with bad class name
            RuntimeInstance instance = new RuntimeInstance();
            instance.setProperty(RuntimeConstants.EVALUATE_CONTEXT_CLASS, "org.apache");
            instance.init();
            EvaluateContext evc = new EvaluateContext(new InternalContextAdapterImpl(base), instance);
            fail ("Expected an exception");
        }
        catch (Exception e) {}
        
        try 
        {
            // initialize with class not implementing Context
            RuntimeInstance instance = new RuntimeInstance();
            instance.setProperty(RuntimeConstants.EVALUATE_CONTEXT_CLASS, org.apache.velocity.test.EvaluateContextTestCase.class.getName());
            instance.init();
            EvaluateContext evc = new EvaluateContext(new InternalContextAdapterImpl(base), instance);
            fail ("Expected an exception");
        }
        catch (Exception e) {}
    }       
}
