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

import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;
import org.apache.velocity.app.tools.*;

import org.apache.velocity.demo.om.*;

/**
 * Handles listing messages
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.3.14.1 $
 * $Id: ListCommand.java,v 1.3.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public class ListCommand extends Command
{
    /** Name of the Template */
    public static final String LIST = "list.vm";
    
    public ListCommand( HttpServletRequest req, HttpServletResponse resp )
    {
        super( req, resp );
    }
    
    /**
     * Get a Vector of Messages and put them into the
     * context for the template
     */
    public String exec( Context ctx )
    {
        Object[] list = ForumDatabase.listAll();
        
        if ( list == null || list.length == 0 )
        {
            ctx.put("hasMessages", Boolean.FALSE );    
        }
        else
        {
            ctx.put("hasMessages", Boolean.TRUE );
            ctx.put("listall", list );
        }
        
        return LIST;
    }
}

