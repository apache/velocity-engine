package org.apache.velocity.runtime.parser.node;

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

import org.apache.velocity.context.Context.Scope;
import org.apache.velocity.runtime.parser.Parser;

/**
 * Implements the #local directive functionality
 * This directive shadows the functionality of <code>ASTSetDirective</code>
 * but alters it's behavior to only affect the local scope. This is done
 * by overriding the <code>getScope()</code> method.
 */
public class ASTLocalDirective extends ASTSetDirective
{
    public ASTLocalDirective(int id)
    {
         super(id);
    }

    public ASTLocalDirective(Parser p, int id)
    {
        super(p, id);
    }

    public Scope getScope()
    {
        return Scope.LOCAL;
    }
}
