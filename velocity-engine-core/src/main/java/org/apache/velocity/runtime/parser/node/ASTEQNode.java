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

import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.util.DuckType;

/**
 *  Handles <code>arg1 == arg2</code>
 *
 *  This operator requires that the LHS and RHS are both of the
 *  same Class, both numbers or both coerce-able to strings.
 *
 *  @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 *  @author <a href="mailto:pero@antaramusic.de">Peter Romianowski</a>
 *  @author Nathan Bubna
 */
public class ASTEQNode extends ASTComparisonNode
{
    public ASTEQNode(int id)
    {
        super(id);
    }

    public ASTEQNode(Parser p, int id)
    {
        super(p, id);
    }

    @Override
    public boolean compareNull(Object left, Object right)
    {
        // at least one is null, see if other is null or acts as a null
        return left == right || DuckType.asNull(left == null ? right : left);
    }

    @Override
    public String getLiteralOperator()
    {
        return "==";
    }

    public boolean numberTest(int compareResult)
    {
        return compareResult == 0;
    }

    @Override
    public boolean compareNonNumber(Object left, Object right)
    {
        /**
         * if both are not null, then assume that if one class
         * is a subclass of the other that we should use the equals operator
         */
        if (left.getClass().isAssignableFrom(right.getClass()) ||
            right.getClass().isAssignableFrom(left.getClass()))
        {
            return left.equals(right);
        }

        // coerce to string, remember getAsString() methods may return null
        left = DuckType.asString(left);
        right = DuckType.asString(right);
        if (left == right)
        {
            return true;
        }
        else if (left == null || right == null)
        {
            return false;
        }
        else
        {
            return left.equals(right);
        }
    }

}
