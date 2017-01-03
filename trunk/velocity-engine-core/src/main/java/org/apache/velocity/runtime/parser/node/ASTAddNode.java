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
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.util.DuckType;

/**
 * Handles number addition of nodes.<br><br>
 *
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:pero@antaramusic.de">Peter Romianowski</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ASTAddNode extends ASTMathNode
{
    /**
     * @param id
     */
    public ASTAddNode(int id)
    {
        super(id);
    }

    /**
     * @param p
     * @param id
     */
    public ASTAddNode(Parser p, int id)
    {
        super(p, id);
    }

    @Override
    protected Object handleSpecial(Object left, Object right, InternalContextAdapter context)
    {
        // check for strings, but don't coerce
        String lstr = DuckType.asString(left, false);
        String rstr = DuckType.asString(right, false);
        if (lstr != null || rstr != null)
        {
            if (lstr == null)
            {
                lstr = left != null ? left.toString() : jjtGetChild(0).literal();
            }
            else if (rstr == null)
            {
                rstr = right != null ? right.toString() : jjtGetChild(1).literal();
            }
            return lstr.concat(rstr);
        }
        return null;
    }

    @Override
    public String getLiteralOperator()
    {
        return "+";
    }

    public Number perform(Number left, Number right, InternalContextAdapter context)
    {
        return MathUtils.add(left, right);
    }

}




