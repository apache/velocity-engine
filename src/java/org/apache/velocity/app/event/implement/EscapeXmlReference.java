package org.apache.velocity.app.event.implement;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Escape all XML entities.
 * @see <a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeSql(java.lang.String)">StringEscapeUtils</a>
 * @author wglass
 */
public class EscapeXmlReference extends EscapeReference
{

    /**
     * Escape all XML entities.
     * @param text
     * @return An escaped String.
     * @see <a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeSql(java.lang.String)">StringEscapeUtils</a>
     */
    protected String escape(Object text)
    {
        return StringEscapeUtils.escapeXml(text.toString());
    }

    /**
     * @return attribute "eventhandler.escape.xml.match"
     */
    protected String getMatchAttribute()
    {
        return "eventhandler.escape.xml.match";
    }

}
