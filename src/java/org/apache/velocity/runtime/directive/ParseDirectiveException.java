package org.apache.velocity.runtime.directive;

/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Stack;

/**
 * Exception for #parse() problems
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ParseDirectiveException.java,v 1.4.14.1 2004/03/03 23:22:56 geirm Exp $
 */
public class ParseDirectiveException extends Exception
{
    private Stack filenameStack = new Stack();
    private String msg = "";
    private int depthCount = 0;

    /**
     * Constructor
     */
    ParseDirectiveException( String m, int i )
    {
        msg = m;
        depthCount = i;
    }

    /**
     * Get a message.
     */
    public String getMessage()
    {
        String returnStr  =  "#parse() exception : depth = " + 
            depthCount + " -> " + msg;

        returnStr += " File stack : ";

        try
        {
            while( !filenameStack.empty())
            {
                returnStr += (String) filenameStack.pop();
                returnStr += " -> ";
            }
        }
        catch( Exception e)
        {
        }

        return returnStr;
    }

    /**
     * Add a file to the filename stack
     */
    public void addFile( String s )
    {
        filenameStack.push( s );
    }

}
