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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;

import java.util.Enumeration;
import java.util.Locale;

import java.io.PrintWriter;
import java.io.IOException;

/**
 *  Wrapper for the HttpServletResponse to help get past the introspection
 *  issues when HttpServletResponse is an interface to non-public class methods.
 *  <br><br>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: HttpServletResponseWrap.java,v 1.3 2001/09/11 18:04:45 geirm Exp $
 *
 * @deprecated Deprecated as of v1.2 because no longer necessary.
 *
 */
public class HttpServletResponseWrap implements HttpServletResponse
{
    /**
     *  The object that we are wrapping, set in the CTOR
     */
    private HttpServletResponse resp = null;

    /**
     *  CTOR
     *
     *  @param req HttpServletResponse object to wrap
     */
    public HttpServletResponseWrap( HttpServletResponse resp )
    {
        this.resp = resp;
    }

    /**
     *  Accessor to allow one to retrieve the wrapped 
     *  HttpServletRequest.
     *
     *  @return Wrapped HttpServletResponse
     */
    public HttpServletResponse getWrappedObject()
    {
        return resp;
    }

    /* ------------ HttpServletResponse --------------- */

    public void addCookie(Cookie cookie)
    {
        resp.addCookie( cookie );
    }

    public boolean containsHeader(String name)
    {
        return resp.containsHeader( name );
    }

    public String encodeURL(String url)
    {
        return resp.encodeURL( url );
    }

    public String encodeRedirectURL(String url)
    {
        return resp.encodeRedirectURL( url );
    }
    
    /**
     * @deprecated
     */
    public String encodeUrl(String url)
    {
        return resp.encodeUrl( url );
    }
    
    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String url)
    {
        return resp.encodeRedirectUrl( url );
    }

    public void sendError(int sc, String msg) 
        throws IOException
    {
        resp.sendError( sc, msg );
    }

    
    public void sendError(int sc) 
        throws IOException
    {
        resp.sendError( sc );
    }

    public void sendRedirect(String location) 
        throws IOException
    {
        resp.sendRedirect( location );
    }
    
    public void setDateHeader(String name, long date)
    {
        resp.setDateHeader( name, date );
    }
    
    public void addDateHeader(String name, long date)
    {
        resp.addDateHeader( name, date );
    }
    
    public void setHeader(String name, String value)
    {
        resp.setHeader( name, value );
    }
    
    public void addHeader(String name, String value)
    {
        resp.addHeader( name, value );
    }

    public void setIntHeader(String name, int value)
    {
        resp.setIntHeader( name, value );
    }

    public void addIntHeader(String name, int value)
    {
        resp.addIntHeader( name, value );
    }
    
    /**
     * @deprecated
     */
    public void setStatus(int sc)
    {
        resp.setStatus( sc );
    }
  
    /**
     * @deprecated
     */
    public void setStatus(int sc, String sm)
    {
        resp.setStatus( sc, sm );
    }

    /* ---------------- ServletResponse ---------------- */
  
    public String getCharacterEncoding()
    {
        return resp.getCharacterEncoding();
    }
    
    public ServletOutputStream getOutputStream() 
        throws IOException
    {
        return resp.getOutputStream();
    }

    public PrintWriter getWriter() 
        throws IOException
    {
        return resp.getWriter();
    }
    
    public void setContentLength(int len)
    {
         resp.setContentLength( len );
    }
    
    public void setContentType(String type)
    {
         resp.setContentType( type );
    }
    

    public void setBufferSize(int size)
    {
        resp.setBufferSize( size );
    }
    
    public int getBufferSize()
    {
        return resp.getBufferSize();
    }

    public void flushBuffer() 
        throws IOException
    {
        resp.flushBuffer();
    }
        
    public boolean isCommitted()
    {
        return resp.isCommitted();
    }
   
    public void reset()
    {
        resp.reset();
    }
    
    public void setLocale(Locale loc)
    {
        resp.setLocale( loc );
    }
   
    public Locale getLocale()
    {
        return resp.getLocale();
    }
}



