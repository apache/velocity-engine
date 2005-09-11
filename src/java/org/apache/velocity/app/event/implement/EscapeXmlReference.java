package org.apache.velocity.app.event.implement;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Escape all XML entities.
 * @see http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeHtml(java.lang.String)
 * @author wglass
 */
public class EscapeXmlReference extends EscapeReference
{

    /**
     * Escape all XML entities.  
     * @see http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeXml(java.lang.String)
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
