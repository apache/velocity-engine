package org.apache.velocity.test.issues;

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

import org.apache.velocity.test.BaseTestCase;

/**
 * This class tests VELOCITY-762.
 */
public class Velocity762TestCase extends BaseTestCase
{
    public Velocity762TestCase(String name)
    {
        super(name);
    }

    public void testForeachIsLast()
    {
        String template = "#foreach( $i in [1..3] )$foreach.last #end";
        assertEvalEquals("false false true ", template);
    }

    public void testAllForeachProps()
    {
        String template = "#foreach( $number in [1..3] )"+
                          "number:$number hasNext:$foreach.hasNext "+
                           "first:$foreach.first last:$foreach.last "+
                           "count:$foreach.count index:$foreach.index \n"+
                          "#end";
        String expect = "number:1 hasNext:true first:true last:false count:1 index:0 \n"+
                        "number:2 hasNext:true first:false last:false count:2 index:1 \n"+
                        "number:3 hasNext:false first:false last:true count:3 index:2 \n";
        assertEvalEquals(expect, template);
    }

}
