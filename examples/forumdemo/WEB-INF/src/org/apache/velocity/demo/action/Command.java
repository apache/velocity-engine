package org.apache.velocity.demo.action;

/*
 * Copyright 1999,2004 The Apache Software Foundation.
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

// Java Stuff
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Velocity stuff
import org.apache.velocity.context.Context;

/**
 * Base class for commands
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.2.14.1 $
 * $Id: Command.java,v 1.2.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public abstract class Command
{
    protected HttpServletRequest request = null;
    protected HttpServletResponse response = null;
    
    /**
     * Constructor
     */
    public Command( HttpServletRequest req, HttpServletResponse resp )
    {
        this.request=req;
        this.response=resp;
    }
    
    /**
     * Implemented by classes that extends this class
     *
     * @param the context
     * @return the name of the template to execute
     */
    public abstract String exec( Context context );
}

