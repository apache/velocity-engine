package org.apache.velocity.app.event.implement;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.RuntimeServicesAware;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Base class for escaping references.  To use it, override the following methods:
 * <DL>
 * <DT><code>String escape(String text)</code></DT>
 * <DD>escape the provided text</DD>
 * <DT><code>String getMatchAttribute()</code></DT>
 * <DD>retrieve the configuration attribute used to match references (see below)</DD>
 * </DL>
 *
 * <P>By default, all references are escaped.  However, by setting the match attribute
 * in the configuration file to a regular expression, users can specify which references
 * to escape.  For example the following configuration property tells the EscapeSqlReference
 * event handler to only escape references that start with "sql".
 * (e.g. <code>$sql</code>, <code>$sql.toString(),</code>, etc).
 *
 * <PRE>
 * <CODE>eventhandler.escape.sql.match = /sql.*<!-- -->/
 * </CODE>
 * </PRE>
 * <!-- note: ignore empty HTML comment above - breaks up star slash avoiding javadoc end -->
 *
 * Regular expressions should follow the "regex5" format used by the java.util.regex package
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain </a>
 * @version $Id$
 */
public abstract class EscapeReference implements ReferenceInsertionEventHandler,RuntimeServicesAware {


    private RuntimeServices rs;

    private String matchRegExp = null;

    private Pattern pattern = null;

    /**
     * Escape the given text.  Override this in a subclass to do the actual
     * escaping.
     *
     * @param text the text to escape
     * @return the escaped text
     */
    protected abstract String escape(Object text);

    /**
     * Specify the configuration attribute that specifies the
     * regular expression.  Ideally should be in a form
     * <pre><code>eventhandler.escape.XYZ.match</code></pre>
     *
     * <p>where <code>XYZ</code> is the type of escaping being done.
     * @return configuration attribute
     */
    protected abstract String getMatchAttribute();

    /**
     * Escape the provided text if it matches the configured regular expression.
     */
    public Object referenceInsert(String reference, Object value)
    {
        if(value == null)
        {
            return value;
        }

        if (pattern == null)
        {
            return escape(value);
        }
        else if (pattern.matcher(reference).find())
        {
            return escape(value);
        }
        else
        {
            return value;
        }
    }

    /**
     * Called automatically when event cartridge is initialized.
     */
    public void setRuntimeServices(RuntimeServices rs) throws Exception
    {
        this.rs = rs;

        /**
         * Get the regular expression pattern.
         */
        matchRegExp = rs.getConfiguration().getString(getMatchAttribute());

        if (matchRegExp != null)
        {
            matchRegExp = matchRegExp.trim();
            if (matchRegExp.startsWith("/") && matchRegExp.endsWith("/"))
            {
                matchRegExp = matchRegExp.substring(1, matchRegExp.length() - 1);
            }

            if (matchRegExp.length() == 0)
            {
                matchRegExp = null;
            }
        }

        /**
         * Test the regular expression for a well formed pattern
         */
        if (matchRegExp != null)
        {
            try
            {
                pattern = Pattern.compile(matchRegExp);
            }
            catch (PatternSyntaxException pse)
            {
                rs.getLog().error("Invalid regular expression '" + matchRegExp
                        + "'.  No escaping will be performed.");
            }
        }
    }

    /**
     * Retrieve a reference to RuntimeServices.  Use this for checking additional
     * configuration properties.
     * @return
     */
    protected RuntimeServices getRuntimeServices()
    {
        return rs;
    }

}
