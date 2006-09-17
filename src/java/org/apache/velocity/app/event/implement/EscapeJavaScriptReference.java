package org.apache.velocity.app.event.implement;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Escapes the characters in a String to be suitable for use in JavaScript.
 * @see <a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeJavaScript(java.lang.String)">StringEscapeUtils</a>
 * @author wglass
 */
public class EscapeJavaScriptReference extends EscapeReference
{

    /**
     * Escapes the characters in a String to be suitable for use in JavaScript.
     * @param text 
     * @return An escaped String.
     * @see <a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeJavaScript(java.lang.String)">StringEscapeUtils</a>
     */
    protected String escape(Object text)
    {
        return StringEscapeUtils.escapeJavaScript(text.toString());
    }

    /**
     * @return attribute "eventhandler.escape.javascript.match"
     */
    protected String getMatchAttribute()
    {
        return "eventhandler.escape.javascript.match";
    }

}
