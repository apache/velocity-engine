package org.apache.velocity.anakia;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This class is for escaping CDATA sections. The code was 
 * "borrowed" from the JDOM code. I also added in escaping
 * of the " -> &amp;quot; character.
 *
 * @version $Id: Escape.java,v 1.5 2004/02/27 18:43:13 dlr Exp $
 */
public class Escape
{
    /**
     * Empty constructor
     */
    public Escape()
    {
        // left blank on purpose
    }
    
    /**
     * Do the escaping.
     */
    public static final String getText(String st)
    {
        StringBuffer buff = new StringBuffer();
        char[] block = st.toCharArray();
        String stEntity = null;
        int i, last;

        for (i=0, last=0; i < block.length; i++)
        {
            switch(block[i])
            {
                case '<' :
                    stEntity = "&lt;";
                    break;
                case '>' :
                    stEntity = "&gt;";
                    break;
                case '&' :
                    stEntity = "&amp;";
                    break;
                case '"' :
                    stEntity = "&quot;";
                    break;
                default :
                    /* no-op */ ;
            }
            if (stEntity != null)
            {
                buff.append(block, last, i - last);
                buff.append(stEntity);
                stEntity = null;
                last = i + 1;
            }
        }
        if(last < block.length)
        {
            buff.append(block, last, i - last);
        }
        return buff.toString();
    }
}
