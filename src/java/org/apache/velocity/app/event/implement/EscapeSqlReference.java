package org.apache.velocity.app.event.implement;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Escapes the characters in a String to be suitable to pass to an SQL query.
 * @see http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeSql(java.lang.String)
 * @author wglass
 */
public class EscapeSqlReference extends EscapeReference
{

    /**
     * Escapes the characters in a String to be suitable to pass to an SQL query.
     * @see http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeXml(java.lang.String)
     */
    protected String escape(Object text)
    {
        return StringEscapeUtils.escapeSql(text.toString());
    }

    /**
     * @return attribute "eventhandler.escape.sql.match"
     */
    protected String getMatchAttribute()
    {
        return "eventhandler.escape.sql.match";
    }

}
