package org.apache.velocity.demo.om;

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

/**
 * This is just a simple memory database for demo purposes.
 * 
 * @author <a href="mailto:daveb@miceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.1.14.1 $
 * $Id: ForumDatabase.java,v 1.1.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public class ForumDatabase
{
    private static Hashtable messages = new Hashtable();
    private static int nextId = 0;
    private static ForumDatabase me = null;
    
    /** Constructor */
    private ForumDatabase()
    {}
    
    /**
     * Post a new message.
     */
    public static synchronized void postMessage( Message message )
    {
        Integer nextNumber = new Integer( nextId++ );
        message.setId( nextNumber );
        messages.put( nextNumber,  message );
    }
    
    /**
     * List all messages in the store
     */
    public static Object[] listAll()
    {
        return messages.values().toArray();
    }
    
    /**
     * Get a specific message
     */
    public static synchronized Message getMessage( String index )
    {
        return (Message)messages.get( new Integer( index ) );
    }
    
    /**
     * Post a reply to a message
     */
    public static synchronized void postReply( Message reply, String parent )
    {
        Message thread = getMessage( parent );
        thread.addReply( reply );
    }
}




