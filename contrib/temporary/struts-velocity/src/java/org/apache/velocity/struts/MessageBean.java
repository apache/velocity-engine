package org.apache.velocity.struts;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 * $Id: MessageBean.java,v 1.1.12.1 2004/03/04 00:18:28 geirm Exp $
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
