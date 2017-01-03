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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.parser.Parser;

/**
 *  Handles <code>arg1 != arg2</code> by negating evaluation of ASTEQNode
 */
public class ASTNENode extends ASTEQNode
{
    public ASTNENode(int id)
    {
        super(id);
    }

    public ASTNENode(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(InternalContextAdapter context) throws MethodInvocationException
    {
        return !super.evaluate(context);
    }

}
