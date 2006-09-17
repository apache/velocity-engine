package org.apache.velocity.app.event.implement;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Escapes the characters in a String to be suitable to pass to an SQL query.
 * @see <a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeSql(java.lang.String)">StringEscapeUtils</a>
 * @author wglass
 */
public class EscapeSqlReference extends EscapeReference
{

    /**
     * Escapes the characters in a String to be suitable to pass to an SQL query.
     * @param text 
     * @return An escaped string.
     * @see <a href="http://jakarta.apache.org/commons/lang/api/org/apache/commons/lang/StringEscapeUtils.html#escapeSql(java.lang.String)">StringEscapeUtils</a>
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
