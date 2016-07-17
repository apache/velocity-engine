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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.StringWriter;

/**
 * Test comments
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id$
 */
public class CommentsTestCase extends BaseTestCase
{

    public static Test suite()
    {
       return new TestSuite(CommentsTestCase.class);
    }
    
    /**
     * Default constructor.
     * @param name
     */
    public CommentsTestCase(String name)
    {
        super(name);
    }

    
    /**
     * Test multiline comments
     * @throws Exception
     */
    public void testMultiLine()
    throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        
        Context context = new VelocityContext();        
        StringWriter writer = new StringWriter();
        ve.evaluate(context, writer, "test","abc #* test\r\ntest2*#\r\ndef");
        assertEquals("abc \r\ndef", writer.toString());
    }

    /**
     * Test single line comments
     * @throws Exception
     */
    public void testSingleLine()
    throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        
        Context context = new VelocityContext();        
        StringWriter writer = new StringWriter();
        ve.evaluate(context, writer, "test","123 ## test test\r\nabc");
        assertEquals("123 abc", writer.toString());        
    
        context = new VelocityContext();        
        writer = new StringWriter();
        ve.evaluate(context, writer, "test","123 \r\n## test test\r\nabc");
        assertEquals("123 \r\nabc", writer.toString());        
    
    }

    /**
     * Test combined comments
     * @throws Exception
     */
    public void testCombined()
    throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        
        Context context = new VelocityContext();        
        StringWriter writer = new StringWriter();
        ve.evaluate(context, writer, "test","test\r\n## #* *# ${user \r\nabc");
        assertEquals("test\r\nabc", writer.toString());        
    
    }
}
