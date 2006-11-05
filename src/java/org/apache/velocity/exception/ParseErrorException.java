package org.apache.velocity.exception;

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

import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.util.introspection.Info;

/**
 *  Application-level exception thrown when a resource of any type
 *  has a syntax or other error which prevents it from being parsed.
 *  <br>
 *  When this resource is thrown, a best effort will be made to have
 *  useful information in the exception's message.  For complete
 *  information, consult the runtime log.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class ParseErrorException extends VelocityException
{
    /**
     * Version Id for serializable
     */
    private static final long serialVersionUID = -6665197935086306472L;

    /**
     * The column number of the parsing error, or -1 if not defined.
     */
    private int columnNumber = -1;

    /**
     * The line number of the parsing error, or -1 if not defined.
     */
    private int lineNumber = -1;

    /**
     * The name of the template containing the error, or null if not defined.
     */
    private String templateName = "*unset*";

    /**
     * If applicable, contains the invalid syntax or reference that triggered this exception
     */
    private String invalidSyntax;

    /**
     * Create a ParseErrorException with the given message.
     *
     * @param exceptionMessage the error exception message
     */
    public ParseErrorException(String exceptionMessage)
      {
          super(exceptionMessage);
    }

    /**
     * Create a ParseErrorException with the given ParseException.
     *
     * @param pex the parsing exception
     */
    public ParseErrorException(ParseException pex)
    {
        super(pex.getMessage());

        // Don't use a second C'tor, TemplateParseException is a subclass of
        // ParseException...
        if (pex instanceof ExtendedParseException)
        {
            ExtendedParseException xpex = (ExtendedParseException) pex;

            columnNumber = xpex.getColumnNumber();
            lineNumber = xpex.getLineNumber();
            templateName = xpex.getTemplateName();
        }
        else
        {
            //  ugly, ugly, ugly...

            if (pex.currentToken != null && pex.currentToken.next != null)
            {
                columnNumber = pex.currentToken.next.beginColumn;
                lineNumber = pex.currentToken.next.beginLine;
            }
        }
    }

    /**
     * Create a ParseErrorException with the given ParseException.
     *
     * @param pex the parsing exception
     */
    public ParseErrorException(VelocityException pex)
    {
        super(pex.getMessage());

        // Don't use a second C'tor, TemplateParseException is a subclass of
        // ParseException...
        if (pex instanceof ExtendedParseException)
        {
            ExtendedParseException xpex = (ExtendedParseException) pex;

            columnNumber = xpex.getColumnNumber();
            lineNumber = xpex.getLineNumber();
            templateName = xpex.getTemplateName();
        }
        else if (pex.getWrappedThrowable() instanceof ParseException)
        {
            ParseException pex2 = (ParseException) pex.getWrappedThrowable();

            if (pex2.currentToken != null && pex2.currentToken.next != null)
            {
                columnNumber = pex2.currentToken.next.beginColumn;
                lineNumber = pex2.currentToken.next.beginLine;
            }
        }
    }


    /**
     * Create a ParseErrorRuntimeException with the given message and info
     * 
     * @param exceptionMessage the error exception message
     * @param info an Info object with the current template info
     */
    public ParseErrorException(String exceptionMessage, Info info)
    {
        super(exceptionMessage);
        columnNumber = info.getColumn();
        lineNumber = info.getLine();
        templateName = info.getTemplateName();        
    }    

    /**
     * Create a ParseErrorRuntimeException with the given message and info
     * 
     * @param exceptionMessage the error exception message
     * @param info an Info object with the current template info
     * @param invalidSyntax the invalid syntax or reference triggering this exception
     */
    public ParseErrorException(String exceptionMessage, 
            Info info, String invalidSyntax)
    {
        super(exceptionMessage);
        columnNumber = info.getColumn();
        lineNumber = info.getLine();
        templateName = info.getTemplateName();  
        this.invalidSyntax = invalidSyntax;       
    }    


    /**
     * Return the column number of the parsing error, or -1 if not defined.
     *
     * @return column number of the parsing error, or -1 if not defined
     */
    public int getColumnNumber()
    {
        return columnNumber;
    }

    /**
     * Return the line number of the parsing error, or -1 if not defined.
     *
     * @return line number of the parsing error, or -1 if not defined
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Return the name of the template containing the error, or null if not
     * defined.
     *
     * @return the name of the template containing the parsing error, or null
     *      if not defined
     */
    public String getTemplateName()
    {
        return templateName;
    }

    /**
     * Return the invalid syntax or reference that triggered this error, or null
     * if not defined.
     * 
     * @return Return the invalid syntax or reference that triggered this error, or null
     * if not defined
     */
    public String getInvalidSyntax()
    {
        return invalidSyntax;
    }

}
