package org.apache.velocity.runtime.parser.node;

import org.apache.velocity.runtime.parser.*;

public class NodeUtils
{
    public static String specialText(Token t)
    {
        String specialText = "";
        
        if (t.specialToken == null || t.specialToken.image.startsWith("##"))
            return specialText;
            
        Token tmp_t = t.specialToken;
        
        while (tmp_t.specialToken != null)
            tmp_t = tmp_t.specialToken;
        
        while (tmp_t != null)
        {
            specialText += tmp_t.image;
            tmp_t = tmp_t.next;
        }            

        return specialText;
    }
}
