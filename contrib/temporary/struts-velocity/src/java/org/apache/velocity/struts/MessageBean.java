package org.apache.velocity.struts;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
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
 */

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;

import org.apache.struts.action.Action;
import org.apache.struts.util.MessageResources;

/**
 * <p>
 *  Kluged class to handle the Struts Action message resourcesystem.  It's
 *  based on outright theft of the code from MessageTag.
 *  </p>
 *  <p>
 *  This will be redone to avoid the code duplication.
 *  </p>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * $Id: MessageBean.java,v 1.1 2001/04/16 03:27:54 geirm Exp $
 */
public class MessageBean extends VelocityBean
{
    /**
     *  CTOR
     */
    public MessageBean( ServletContext context, HttpSession sess, ServletRequest request )
    {
        super( context, sess, request );
    }

    /**
     *  Returns the requested string resource for the
     *  locale stuffed in the session.
     *
     *  Note : I want to change this to 'key()' as that
     *  is a closer parallel to the MessageTag.
     *
     *  @param msgKey string resource name
     *  @return resource string
     */
    public String get( String msgKey )
    {
        /*
         *  Acquire the resources object containing our messages
         */
        MessageResources resources = (MessageResources)  
            context.getAttribute( Action.MESSAGES_KEY );

        if (resources == null)
        {
            return "";
        }

        /*
         *  Calculate the Locale we will be using
         */
        Locale locale = null;

        locale = (Locale) session.getAttribute( Action.LOCALE_KEY );

        if (locale == null)
        {
            locale = Locale.getDefault();
        }
        
        /*
         * Construct the optional arguments array we will be using
         */
        Object args[] = new Object[5];
        args[0] = null;
        args[1] = null;
        args[2] = null;
        args[3] = null;
        args[4] = null;
        
        /*
         *  Retrieve the message string we are looking for
         */
        return resources.getMessage(locale, msgKey, args);
    }

}
