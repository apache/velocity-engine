package org.apache.velocity.exception;

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

import org.apache.velocity.runtime.parser.ParseException;

/**
 *  Application-level exception thrown when a resource of any type
 *  has a syntax or other error which prevents it from being parsed.
 *  <br>
 *  When this resource is thrown, a best effort will be made to have
 *  useful information in the exception's message.  For complete 
 *  information, consult the runtime log.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */
public class ParseErrorException extends VelocityException
{
    /**
     * Version Id for serializable
     */
    private static final long serialVersionUID = -6665197935086306474L;

    /**
     * The column number of the parsing error, or -1 if not defined.
     */
    private int columnNumber = -1;
    
    /**
     * The line number of the parsing error, or -1 if not defined.
     */
    private int lineNumber;
    
    /**
     * The name of the template containing the error, or null if not defined.
     */
    private String templateName;
    
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
        
        columnNumber = pex.columnNumber;
        lineNumber = pex.lineNumber;
        templateName = pex.templateName;
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
}
