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
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;
import org.apache.struts.util.ErrorMessages;
import org.apache.struts.util.MessageResources;


/**
 * <p>
 *  Kluged class to handle the Struts Action error system.  It's
 *  based on outright theft of the code from ErrorsTag.
 *  </p>
 *  <p>
 *  This will be redone to avoid the code duplication.
 *  </p>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * $Id: ErrorsBean.java,v 1.1 2001/04/16 03:27:54 geirm Exp $
 */
public class ErrorsBean extends VelocityBean
{
    /**
     *  key used to find the error stuff produced by Struts
     */
    private String name = Action.ERROR_KEY;
 
    /**
     *  CTOR
     */
    public ErrorsBean( ServletContext context, HttpSession sess, ServletRequest request )
    {
        super( context, sess, request );
    }

    /**
     *  Public method to retrieve formatted error string
     *
     *  @return string containing the error message, formatted
     */
    public String getErrors()
    {
        /*
         *  Were any error messages specified?
         */
        
        ActionErrors errors = new ActionErrors();
	
	    Object value = request.getAttribute(name);
               
	    if (value == null) 
        {
            ;
	    }
        else if (value instanceof String) 
        {
            errors.add( ActionErrors.GLOBAL_ERROR,  new ActionError((String) value));
	    } 
        else if (value instanceof String[]) 
        {
            String keys[] = (String[]) value;
            
            for (int i = 0; i < keys.length; i++)
            {
                errors.add(ActionErrors.GLOBAL_ERROR,
                           new ActionError(keys[i]));
            }
        } 
        else if (value instanceof ErrorMessages) 
        {
            String keys[] = ((ErrorMessages) value).getErrors();
            
            if (keys == null)
                keys = new String[0];
            
            for (int i = 0; i < keys.length; i++)
                errors.add(ActionErrors.GLOBAL_ERROR,
                           new ActionError(keys[i]));
        }
        else if (value instanceof ActionErrors) 
        {
            errors = (ActionErrors) value;
	    }
  
        if (errors.empty())
        {
            return "";
        }

        /*
         *  Render the error messages appropriately
         */

        Locale locale = null;
   
	    locale = (Locale) session.getAttribute(Action.LOCALE_KEY);
	
        if (locale == null)
            locale =  Locale.getDefault();

        MessageResources messages = (MessageResources)
            context.getAttribute(Action.MESSAGES_KEY);

        String message = null;
        StringBuffer results = new StringBuffer();

        message = messages.getMessage(locale, "errors.header");
	
        if (message != null) 
        {
            results.append(message);
            results.append("\r\n");
        }
        
        Iterator reports = errors.get();
        
        while (reports.hasNext()) 
        {
            ActionError report = (ActionError) reports.next();
            message =
                messages.getMessage(locale,
                                    report.getKey(), report.getValues());

            if (message != null) 
            {
                results.append(message);
                results.append("\r\n");
            }
        }
        
        message = messages.getMessage(locale, "errors.footer");

        if (message != null) 
        {
            results.append(message);
            results.append("\r\n");
        }

        return results.toString();
    }
}
