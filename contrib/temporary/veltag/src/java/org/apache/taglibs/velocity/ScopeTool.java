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

package org.apache.taglibs.velocity;

import javax.servlet.jsp.PageContext;

/**
 *  <p>
 *  Simple context tool to allow a template-in-JSP to access
 *  the scopes directly to retrieve objects/beans.
 *  </p>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ScopeTool.java,v 1.1.10.1 2004/03/04 00:18:29 geirm Exp $ 
 */
public class ScopeTool
{
    protected PageContext pageContext = null;

    public ScopeTool( PageContext pageContext )
    {
        this.pageContext = pageContext;
    }

    /**
     *  retrieves an object from the page scope
     *
     *  @param name Name of object in scope
     *  @return object if found, null otherwise
     */
    public Object getPageScope( String name )
    {
        return pageContext.getAttribute( name, PageContext.PAGE_SCOPE);
    }

    /**
     *  retrieves an object from the request scope
     *
     *  @param name Name of object in scope
     *  @return object if found, null otherwise
     */
    public Object getRequestScope( String name )
    {
        return pageContext.getAttribute( name, PageContext.REQUEST_SCOPE);
    }

    /**
     *  retrieves an object from the session scope
     *
     *  @param name Name of object in scope
     *  @return object if found, null otherwise
     */
    public Object getSessionScope( String name )
    {
        return pageContext.getAttribute( name, PageContext.SESSION_SCOPE);
    }

    /**
     *  retrieves an object from the application scope
     *
     *  @param name Name of object in scope
     *  @return object if found, null otherwise
     */
    public Object getApplicationScope( String name )
    {
        return pageContext.getAttribute( name, PageContext.APPLICATION_SCOPE);
    }

    /**
     *  retrieves a named object from anyscope, 
     *  working 'upwards':
     *  page - > request - > session - > application
     *
     *  @param name Name of object in scope
     *  @return object if found, null otherwise
     */
    public Object getAnyScope( String name )
    {
        Object o = getPageScope( name );

        if (o == null)
        {
            o = getRequestScope( name );
            
            if ( o == null)
            {
                o = getSessionScope( name );
                    
                if ( o == null )
                {
                    o = getApplicationScope( name );
                }
            }
        }
   
        return o;
    }
}
