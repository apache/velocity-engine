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
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.context.ProxyVMContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;

/**
 * Tests scope of velocimacros with localscope setting. 
 * 
 * @author <a href="mailto:stephenh@chase3000.com">Stephen Habermann</a>
 * @version $Id$
 */
public class VMContextLocalscopeTestCase extends TestCase {

    private RuntimeInstance instance;

    public void setUp() throws Exception 
    {
        this.instance = new RuntimeInstance();
        this.instance.setProperty(RuntimeConstants.VM_CONTEXT_LOCALSCOPE, Boolean.TRUE);
        this.instance.init();
    }

    public void testLocalscopePutDoesntLeakButGetDoes() 
    {
        VelocityContext base = new VelocityContext();
        base.put("outsideVar", "value1");

        ProxyVMContext vm = new ProxyVMContext(new InternalContextAdapterImpl(base), this.instance, true);
        vm.put("newLocalVar", "value2");

        // New variable put doesn't leak
        assertNull(base.get("newLocalVar"));
        assertEquals("value2", vm.get("newLocalVar"));

        // But we can still get to "outsideVar"
        assertEquals("value1", vm.get("outsideVar"));

        // If we decide to try and set outsideVar it won't leak
        vm.put("outsideVar", "value3");
        assertEquals("value3", vm.get("outsideVar"));
        assertEquals("value1", base.get("outsideVar"));
    }

}
