package org.apache.velocity.app;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

import java.util.Enumeration;
import java.util.Locale;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *  Wrapper for the HttpServletRequest to help get past the introspection
 *  issues when HttpServletRequest is an interface to non-public class methods.
 *  <br><br>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: HttpServletRequestWrap.java,v 1.3 2001/09/11 18:04:45 geirm Exp $
 *
 * @deprecated Deprecated as of v1.2 because no longer necessary.
 *
 */

public class HttpServletRequestWrap implements HttpServletRequest
{
    /*
     *  The object that we are wrapping, set in the CTOR
     */
    private HttpServletRequest req = null;

    /**
     *  CTOR
     *
     *  @param req HttpServletRequest object to wrap
     */
    public HttpServletRequestWrap( HttpServletRequest req )
    {
        this.req = req;
    }

    /**
     *  Accessor to allow one to retrieve the wrapped 
     *  HttpServletRequest.
     *
     *  @return Wrapped HttpServletRequest
     */
    public HttpServletRequest getWrappedObject()
    {
        return req;
    }

    /* --------- HttpServletRequest -----------  */

    public String getAuthType()
    {
        return req.getAuthType();
    }

    public Cookie[] getCookies()
    {
        return req.getCookies();
    }

    public String getContextPath()
    {
        return req.getContextPath();
    }

    public long getDateHeader(String name)
    {
        return req.getDateHeader( name );
    }
   
    public String getHeader(String name)
    {
        return req.getHeader( name );
    }

    public Enumeration getHeaders(String name)
    {
        return req.getHeaders( name );
    }

    public Enumeration getHeaderNames()
    {
        return req.getHeaderNames();
    }

    public int getIntHeader(String name)
    {
        return req.getIntHeader( name );
    }
    
    public String getMethod()
    {
        return req.getMethod();
    }
     
    public String getPathInfo()
    {
        return req.getPathInfo();
    }

    public String getPathTranslated()
    {
        return req.getPathTranslated();
    }

    public String getQueryString()
    {
        return req.getQueryString();
    }

    public String getRemoteUser()
    {
        return req.getRemoteUser();
    }

    public boolean isUserInRole(String role)
    {
        return req.isUserInRole( role );
    }

    public java.security.Principal getUserPrincipal()
    {
        return req.getUserPrincipal();
    }

    public String getRequestedSessionId()
    {
        return req.getRequestedSessionId();
    }
    
    public String getRequestURI()
    {
        return req.getRequestURI();
    }
    
    public String getServletPath()
    {
        return req.getServletPath();
    }
 
    public HttpSession getSession(boolean create)
    {
        return req.getSession( create );
    }
   
    public HttpSession getSession()
    {
        return req.getSession();
    }

    public boolean isRequestedSessionIdValid()
    {
        return req.isRequestedSessionIdValid();
    }
 
    public boolean isRequestedSessionIdFromCookie()
    {
        return req.isRequestedSessionIdFromCookie();
    }
    
    public boolean isRequestedSessionIdFromURL()
    {
        return req.isRequestedSessionIdFromURL();
    }

    /**
     *  @deprecated
     */
    public boolean isRequestedSessionIdFromUrl()
    {
        return req.isRequestedSessionIdFromUrl();
    }

    /* ----------------  ServletRequest -------------- */


    public Object getAttribute(String name)
    {
        return req.getAttribute( name );
    }
    
    public Enumeration getAttributeNames()
    {
        return req.getAttributeNames();
    }

    public String getCharacterEncoding()
    {
        return req.getCharacterEncoding();
    }

    public int getContentLength()
    {
        return req.getContentLength();
    }

    public String getContentType()
    {
        return req.getContentType();
    }
    
    public ServletInputStream getInputStream() 
        throws IOException
    {
        return req.getInputStream();
    }
     
    public String getParameter(String name)
    {
        return req.getParameter( name );
    }

    public Enumeration getParameterNames()
    {
        return req.getParameterNames();
    }

    public String[] getParameterValues(String name)
    {
        return req.getParameterValues( name );
    }

  
    public String getProtocol()
    {
        return req.getProtocol();
    }

    public String getScheme()
    {
        return req.getScheme();
    }

    public String getServerName()
    {
        return req.getServerName();
    }
 
    public int getServerPort()
    {
        return req.getServerPort();
    }
    
    public BufferedReader getReader() 
        throws IOException
    {
        return req.getReader();
    }

    public String getRemoteAddr()
    {
        return req.getRemoteAddr();
    }

    public String getRemoteHost()
    {
        return req.getRemoteHost();
    }

    public void setAttribute(String name, Object o)
    {
        req.setAttribute( name, o );
    }

    public void removeAttribute(String name)
    {
        req.removeAttribute( name );
    }
    
    public Locale getLocale()
    {
        return req.getLocale();
    }
 
    public Enumeration getLocales()
    {
        return req.getLocales();
    }
 
    public boolean isSecure()
    {
        return req.isSecure();
    }
    
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return req.getRequestDispatcher( path );
    }
   
    /**
     *  @deprecated
     */
    public String getRealPath( String path )
    {
        return req.getRealPath( path );
    }
}
