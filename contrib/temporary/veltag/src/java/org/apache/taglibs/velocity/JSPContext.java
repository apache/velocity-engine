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

import java.util.HashMap;
import javax.servlet.jsp.PageContext;

import org.apache.velocity.context.AbstractContext;

/**
 *  <p>
 *  Velocity Context implementationfor use in JSP's,
 *  where the servlet API 'scope' is used directly.
 *  </p>
 *  <p>
 *  This context will 'search' the scopes looking for an 
 *  item, working outwards 
 *  page->request->session->application
 *  </p>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: JSPContext.java,v 1.1.10.1 2004/03/04 00:18:29 geirm Exp $ 
 */
public class JSPContext extends AbstractContext
{
    private HashMap context = new HashMap();
    private PageContext pageContext = null;

    public JSPContext( PageContext pageContext )
    {
        this.pageContext = pageContext;
    }

    public Object internalGet( String key )
    {
        Object o = context.get( key );

        if ( o == null)
        {
            o = pageContext.getAttribute( key, PageContext.PAGE_SCOPE);

            if (o == null)
            {
                o = pageContext.getAttribute( key, PageContext.REQUEST_SCOPE);

                if ( o == null)
                {
                    o = pageContext.getAttribute( key, PageContext.SESSION_SCOPE);
                    
                    if ( o == null )
                    {
                        o = pageContext.getAttribute( key, PageContext.APPLICATION_SCOPE);
                    }
                }
            }

            if ( o != null)
                context.put( key, o );
        }
        return o;
    }        

    public Object internalPut( String key, Object value )
    {
        return context.put( key, value );
    }

    public  boolean internalContainsKey(Object key)
    {
        return context.containsKey( key );
    }

    public  Object[] internalGetKeys()
    {
        return context.keySet().toArray();
    }

    public  Object internalRemove(Object key)
    {
        return context.remove( key );
    }
}
