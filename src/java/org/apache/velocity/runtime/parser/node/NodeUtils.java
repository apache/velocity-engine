package org.apache.velocity.runtime.parser.node;
/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.*;

/**
 * Utilities for dealing with the AST node structure.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: NodeUtils.java,v 1.16.4.1 2004/03/03 23:22:59 geirm Exp $
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
     */
    public static String specialText(Token t)
    {
        String specialText = "";
        
        if (t.specialToken == null || t.specialToken.image.startsWith("##") )
            return specialText;
            
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
            
            specialText += sb.toString();

            tmp_t = tmp_t.next;
        }            

        return specialText;
    }
    
    /**
     *  complete node literal
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
     */
    public static String interpolate(String argStr, Context vars)
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
