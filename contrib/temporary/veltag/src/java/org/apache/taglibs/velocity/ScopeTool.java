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

package org.apache.taglibs.velocity;

import javax.servlet.jsp.PageContext;

/**
 *  <p>
 *  Simple context tool to allow a template-in-JSP to access
 *  the scopes directly to retrieve objects/beans.
 *  </p>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ScopeTool.java,v 1.1 2001/08/14 00:07:39 geirm Exp $ 
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
