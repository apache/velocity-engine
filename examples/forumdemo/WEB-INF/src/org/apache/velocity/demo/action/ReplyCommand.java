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
 * This command is called when a user
 * wants to add a reply to a message.
 * It simply adds the parent message ID to the
 * reply from and the subject.
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.2.14.1 $
 * $Id: ReplyCommand.java,v 1.2.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public class ReplyCommand extends Command
{
    /** The form to call */
    public static final String REPLY = "reply.vm";
            
    public ReplyCommand( HttpServletRequest req, HttpServletResponse resp )
    {
        super( req, resp );
    }
    
    /**
     * Add the message ID and the subject to 
     * the Form reply.
     */
    public String exec( Context ctx )
    {
        String id = request.getParameter("id");
        String subject = request.getParameter("subject");
        ctx.put("id", id );
        ctx.put("subject", subject);
        
        return REPLY;
    }
}
