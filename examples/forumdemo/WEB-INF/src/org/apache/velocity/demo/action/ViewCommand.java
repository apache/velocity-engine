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

// Servlet
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Velocity
import org.apache.velocity.context.Context;

// Demo
import org.apache.velocity.demo.om.*;

/**
 * View a specific message and it's replies
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.2.14.1 $
 * $Id: ViewCommand.java,v 1.2.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public class ViewCommand extends Command
{
    /** The template to call */
    public static final String VIEW = "view.vm";
    
    public ViewCommand( HttpServletRequest req, HttpServletResponse resp )
    {
        super( req, resp );
    }

    /**
     * Loads the specific message from the store.
     */
    public String exec( Context ctx )
    {
        String value = request.getParameter("id");
        Message m = ForumDatabase.getMessage( value );
       
        // Populate the context
        ctx.put("message", m );
        ctx.put("id", value);
        
        // Put Replies in the context
        if ( m.numberOfReplies() > 0 )
        {
            ctx.put("hasReplies", Boolean.TRUE);
            ctx.put("replies", m.getReplies());
        }
        else
        {
            ctx.put("hasReplies", Boolean.FALSE);
        }
        
        return VIEW;
    }
}


