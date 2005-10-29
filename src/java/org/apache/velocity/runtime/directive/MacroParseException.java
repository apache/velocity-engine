package org.apache.velocity.runtime.directive;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

import org.apache.velocity.exception.ExtendedParseException;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;

/**
 *  Exception to indicate problem happened while constructing #macro()
 *
 *  For internal use in parser - not to be passed to app level
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class MacroParseException
        extends ParseException
        implements ExtendedParseException
{
    private final String templateName;

    /**
     * Version Id for serializable
     */
    private static final long serialVersionUID = -4985224672336070690L;

    public MacroParseException(final String msg, final String templateName, final Token currentToken)
    {
        super(msg);
        this.currentToken = currentToken;
        this.templateName = templateName;
    }

    /**
     * returns the Template name where this exception occured.
     */
    public String getTemplateName()
    {
        return templateName;
    }

    /**
     * returns the line number where this exception occured.
     */
    public int getLineNumber()
    {
        if ((currentToken != null) && (currentToken.next != null))
        {
            return currentToken.next.beginLine;
        }
        else
        {
            return -1;
        }
    }

    /**
     * returns the column number where this exception occured.
     */
    public int getColumnNumber()
    {
        if ((currentToken != null) && (currentToken.next != null))
        {
            return currentToken.next.beginColumn;
        }
        else
        {
            return -1;
        }
    }
}
