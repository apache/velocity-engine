package org.apache.velocity.demo.om;

/* 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Jakarta-Regexp", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

// Java
import java.util.*;

/**
 * The actual message in the forum.
 * 
 * @author <a href="mailto:daveb@minceda-data.com">Dave Bryson</a>
 * @version $Revision: 1.1 $
 * $Id: Message.java,v 1.1 2000/11/03 02:51:02 daveb Exp $
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

