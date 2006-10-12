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

import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
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
     * Collect all the <SPECIAL_TOKEN>s that
     * are carried along with a token. Special
     * tokens do not participate in parsing but
     * can still trigger certain lexical actions.
     * In some cases you may want to retrieve these
     * special tokens, this is simply a way to
     * extract them.
     * @param t
     * @return String with the special tokens.
     */
    public static String specialText(Token t)
    {
        StringBuffer specialText = new StringBuffer();

        if (t.specialToken == null || t.specialToken.image.startsWith("##") )
        {
            return "";
        }

        Token tmp_t = t.specialToken;

        while (tmp_t.specialToken != null)
        {
            tmp_t = tmp_t.specialToken;
        }

        while (tmp_t != null)
        {
            String st = tmp_t.image;

            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < st.length(); i++)
            {
                char c = st.charAt(i);

                if ( c == '#' || c == '$' )
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
                    for( ok = true; ok && j < st.length(); j++)
                    {
                        char cc = st.charAt( j );

                        if (cc == '\\')
                        {
                            /*
                             *  if we see a \, keep going
                             */
                            continue;
                        }
                        else if( cc == '$' )
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

            // This is a potential JDK 1.3/JDK 1.4 gotcha. If we remove
            // the toString() method call, then when compiling under JDK 1.4,
            // this will be mapped to StringBuffer.append(StringBuffer) and
            // under JDK 1.3, it will be mapped to StringBuffer.append(Object).
            // So the JDK 1.4 compiled jar will bomb out under JDK 1.3 with a
            // MethodNotFound error.
            //
            // @todo Once we are JDK 1.4+ only, remove the toString(), make this
            // loop perform a little bit better.
            specialText.append(sb.toString());

            tmp_t = tmp_t.next;
        }

        return specialText.toString();
    }

    /**
     *  complete node literal
     * @param t
     * @return A node literal.
     *
     */
    public static String tokenLiteral( Token t )
    {
        return specialText( t ) + t.image;
    }

    /**
     * Utility method to interpolate context variables
     * into string literals. So that the following will
     * work:
     *
     * #set $name = "candy"
     * $image.getURI("${name}.jpg")
     *
     * And the string literal argument will
     * be transformed into "candy.jpg" before
     * the method is executed.
     * @param argStr
     * @param vars
     * @return Interpoliation result.
     * @throws MethodInvocationException
     */
    public static String interpolate(String argStr, Context vars) throws MethodInvocationException
    {
        StringBuffer argBuf = new StringBuffer();

        for (int cIdx = 0 ; cIdx < argStr.length();)
        {
            char ch = argStr.charAt(cIdx);

            switch (ch)
            {
                case '$':
                    StringBuffer nameBuf = new StringBuffer();
                    for (++cIdx ; cIdx < argStr.length(); ++cIdx)
                    {
                        ch = argStr.charAt(cIdx);
                        if (ch == '_' || ch == '-'
                            || Character.isLetterOrDigit(ch))
                            nameBuf.append(ch);
                        else if (ch == '{' || ch == '}')
                            continue;
                        else
                            break;
                    }

                    if (nameBuf.length() > 0)
                    {
                        Object value = vars.get(nameBuf.toString());

                        if (value == null)
                            argBuf.append("$").append(nameBuf.toString());
                        else
                            argBuf.append(value.toString());
                    }
                    break;

                default:
                    argBuf.append(ch);
                    ++cIdx;
                    break;
            }
        }

        return argBuf.toString();
    }
}
