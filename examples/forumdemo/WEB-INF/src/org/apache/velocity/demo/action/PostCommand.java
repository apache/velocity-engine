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

// Java
import java.util.*;

// Java Servlet
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Velocity
import org.apache.velocity.context.Context;

// Demo
import org.apache.velocity.demo.om.*;

/**
 * Handles posting new messages to the Forum
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.2.14.1 $
 * $Id: PostCommand.java,v 1.2.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public class PostCommand extends Command
{
    public PostCommand( HttpServletRequest req, HttpServletResponse resp )
    {
        super( req, resp );
    }

    /**
     * Collects the parameters from the Form.
     * Creates a new message and stores it then
     * gets a fresh list and calls the list template
     */
    public String exec( Context ctx )
    {
        String name = request.getParameter("name");
        String subject = request.getParameter("subject");
        String email = request.getParameter("email");
        String content = request.getParameter("content");
        
        Message message = new Message();
        message.setName( name );
        message.setSubject( subject );
        message.setEmail( email );
        message.setContents( content );
        
        ForumDatabase.postMessage( message );
        
        Object[] list = ForumDatabase.listAll();
        
        ctx.put("listall", list );
        ctx.put("hasMessages", Boolean.TRUE );
        
        return ListCommand.LIST;
    }
}

