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
 * The actual message in the forum.
 * 
 * @author <a href="mailto:daveb@minceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.1.14.1 $
 * $Id: Message.java,v 1.1.14.1 2004/03/04 00:18:29 geirm Exp $
 */
public class Message
{
    private Integer id;
    private String subject;
    private String name;
    private String email;
    private String contents;
    private Date submitted;
    private Vector replies = null;

    /**
     * Constructor 
     * 
     * Gets the unique ID from the generator.
     */
    public Message()
    {
        submitted = new Date();
        replies = new Vector(); 
    }

    /**
     * The index number
     */
    public void setId( Integer index )
    {
        this.id=index;
    }

    /**
     * Get the index number
     * @return the id
     */
    public String getId()
    {
        return id.toString();
    }

    /**
     * Return replies for this message
     * 
     * @return Vector
     */
    public Vector getReplies()
    {
        return replies;
    }

    /**
     * Add a reply
     * @param reply
     */
    public void addReply( Message m )
    {
        replies.addElement( m );
    }

    /**
     * @return a count of the number of replies for this message
     */
    public int numberOfReplies()
    {
        return replies.size();
    }
    
    /**
     * Get the value of submitted.
     * @return value of submitted.
     */
    public String getSubmitted() 
    {
        return submitted.toString();
    }
    
    /**
     * Set the value of submitted.
     * @param v  Value to assign to submitted.
     */
    public void setSubmitted(Date  v) {this.submitted = v;}
    
    /**
     * Get the value of contents.
     * @return value of contents.
     */
    public String getContents() {return contents;}
    
    /**
     * Set the value of contents.
     * @param v  Value to assign to contents.
     */
    public void setContents(String  v) {this.contents = v;}
    
    /**
     * Get the value of email.
     * @return value of email.
     */
    public String getEmail() {return email;}
    
    /**
     * Set the value of email.
     * @param v  Value to assign to email.
     */
    public void setEmail(String  v) {this.email = v;}
        
    /**
     * Get the value of name.
     * @return value of name.
     */
    public String getName() {return name;}
    
    /**
     * Set the value of name.
     * @param v  Value to assign to name.
     */
    public void setName(String  v) {this.name = v;}
        
    /**
     * Get the value of subject.
     * @return value of subject.
     */
    public String getSubject() {return subject;}
    
    /**
     * Set the value of subject.
     * @param v  Value to assign to subject.
     */
    public void setSubject(String  v) {this.subject = v;}
}

