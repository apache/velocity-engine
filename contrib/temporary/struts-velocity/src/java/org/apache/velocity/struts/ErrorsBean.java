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
 * $Id: ErrorsBean.java,v 1.1.12.1 2004/03/04 00:18:28 geirm Exp $
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
