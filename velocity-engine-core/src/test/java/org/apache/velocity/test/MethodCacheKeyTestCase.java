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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.runtime.parser.node.ASTMethod;

/**
 * Checks that the equals method works correctly when caching method keys.
 *
 * @author <a href="Will Glass-Husain">wglass@forio.com</a>
 * @version $Id$
 */
public class MethodCacheKeyTestCase extends TestCase 
{
   
    public void testMethodKeyCacheEquals()
    {
        Class [] elements1 = new Class [] { Object.class };
        ASTMethod.MethodCacheKey mck1 = new ASTMethod.MethodCacheKey("test",elements1);
        
        selfEqualsAssertions(mck1);
        
        Class [] elements2 = new Class [] { Object.class };
        ASTMethod.MethodCacheKey mck2 = new ASTMethod.MethodCacheKey("test",elements2);
        
        assertTrue(mck1.equals(mck2));
        
        Class [] elements3 = new Class [] { String.class };
        ASTMethod.MethodCacheKey mck3 = new ASTMethod.MethodCacheKey("test",elements3);
        
        assertFalse(mck1.equals(mck3));
        
        Class [] elements4 = new Class [] { Object.class };
        ASTMethod.MethodCacheKey mck4 = new ASTMethod.MethodCacheKey("boo",elements4);
        
        assertFalse(mck1.equals(mck4));
        
        /** check for potential NPE's **/
        Class [] elements5 = ArrayUtils.EMPTY_CLASS_ARRAY;
        ASTMethod.MethodCacheKey mck5 = new ASTMethod.MethodCacheKey("boo",elements5);
        selfEqualsAssertions(mck5);
        
        Class [] elements6 = null;
        ASTMethod.MethodCacheKey mck6 = new ASTMethod.MethodCacheKey("boo",elements6);
        selfEqualsAssertions(mck6);
        
        Class [] elements7 = new Class [] {};
        ASTMethod.MethodCacheKey mck7 = new ASTMethod.MethodCacheKey("boo",elements7);
        selfEqualsAssertions(mck7);
        
        Class [] elements8 = new Class [] {null};
        ASTMethod.MethodCacheKey mck8 = new ASTMethod.MethodCacheKey("boo",elements8);
        selfEqualsAssertions(mck8);      
        
        Class [] elements9 = new Class [] { Object.class };
        ASTMethod.MethodCacheKey mck9 = new ASTMethod.MethodCacheKey("boo",elements9);
        selfEqualsAssertions(mck9);      
        
    }
    
    private void selfEqualsAssertions(ASTMethod.MethodCacheKey mck)
    {
        assertTrue(mck.equals(mck));
        assertTrue(!mck.equals(null));
        assertTrue(!mck.equals((ASTMethod.MethodCacheKey) null));
    }
    
   
}
