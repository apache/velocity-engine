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
import org.apache.velocity.runtime.parser.ParserConstants;
import org.apache.velocity.runtime.parser.Token;

/**
 * Utilities for dealing with the AST node structure.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class NodeUtils
{
    /**
     * Collect all the &lt;SPECIAL_TOKEN&gt;s that
     * are carried along with a token. Special
     * tokens do not participate in parsing but
     * can still trigger certain lexical actions.
     * In some cases you may want to retrieve these
     * special tokens, this is simply a way to
     * extract them.
     * @param t the Token
     * @return StrBuilder with the special tokens.
     * @since 2.0.0
     */
    public static StringBuilder getSpecialText(Parser parser, Token t)
    {
        StringBuilder sb = new StringBuilder();

        Token tmp_t = t.specialToken;

        while (tmp_t.specialToken != null)
        {
            tmp_t = tmp_t.specialToken;
        }

        while (tmp_t != null)
        {
            String st = tmp_t.image;

            for(int i = 0, is = st.length(); i < is; i++)
            {
                char c = st.charAt(i);

                if ( c == parser.hash() || c == parser.dollar() )
                {
                    sb.append( c );
                }

                /*
                 *  more dreaded MORE hack :)
                 *
                 *  looking for ("\\")*"$" sequences
                 */

                if ( c == '\\')
                {
                    boolean ok = true;
                    boolean term = false;

                    int j = i;
                    for( ok = true; ok && j < is; j++)
                    {
                        char cc = st.charAt( j );

                        if (cc == '\\')
                        {
                            /*
                             *  if we see a \, keep going
                             */
                            continue;
                        }
                        else if( cc == parser.dollar() )
                        {
                            /*
                             *  a $ ends it correctly
                             */
                            term = true;
                            ok = false;
                        }
                        else
                        {
                            /*
                             *  nah...
                             */
                            ok = false;
                        }
                    }

                    if (term)
                    {
                        String foo =  st.substring( i, j );
                        sb.append( foo );
                        i = j;
                    }
                }
            }

            tmp_t = tmp_t.next;
        }
        return sb;
    }

    /**
     *  complete node literal
     * @param t
     * @return A node literal.
     */
    public static String tokenLiteral( Parser parser, Token t )
    {
        // Look at kind of token and return "" when it's a block comment
        if (t.kind == ParserConstants.MULTI_LINE_COMMENT)
        {
            return "";
        }
        else if (t.specialToken == null || t.specialToken.image.startsWith(parser.lineComment()))
        {
            return t.image;
        }
        else
        {
            StringBuilder special = getSpecialText(parser, t);
            if (special.length() > 0)
            {
                return special.append(t.image).toString();
            }
            return t.image;
        }
    }

    /**
     * Fix children indentation in structured space gobbling mode.
     * @param parent
     * @param parentIndentation
     */
    public static void fixIndentation(SimpleNode parent, String parentIndentation)
    {
        IndentationFixer fixer = new IndentationFixer(parentIndentation);
        parent.childrenAccept(fixer, null);
    }
}
