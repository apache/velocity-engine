package org.apache.velocity.test;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
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

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.servlet.VelocityServlet;

import junit.framework.TestCase;


/**
 * Tests our VelocityServlet implementation.
 *
 * @author <a href="mailto:dlr@apache.org">Daniel Rall</a>
 */
public class VelocityServletTest extends TestCase
{
    /**
     * Default constructor.
     */
    public VelocityServletTest()
    {
        super("VelocityServletTest");
    }

    public static junit.framework.Test suite ()
    {
        return new VelocityServletTest();
    }

    /**
     * Runs the test.
     */
    public void runTest()
    {
        /*
         * Assure we have the encoding we think we should.
         */

        MockVelocityServlet servlet = new MockVelocityServlet();
        try
        {
            servlet.init(new MockServletConfig());
        }
        catch (ServletException e)
        {
            e.printStackTrace();
        }
        System.out.println(RuntimeConstants.OUTPUT_ENCODING + "=" +
                           RuntimeSingleton.getProperty
                           (RuntimeConstants.OUTPUT_ENCODING));
        HttpServletResponse res = new MockHttpServletResponse();
        servlet.visibleSetContentType(null, res);
        assertEquals("Character encoding not set to UTF-8",
                     "UTF-8", res.getCharacterEncoding());
    }

    class MockVelocityServlet extends VelocityServlet
    {
        void visibleSetContentType(HttpServletRequest req,
                                   HttpServletResponse res)
        {
            setContentType(req, res);
        }

        protected Properties loadConfiguration(ServletConfig config)
            throws IOException
        {
            Properties p = new Properties();
            p.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
            return p;
        }

        public ServletConfig getServletConfig()
        {
            return new MockServletConfig();
        }
    }

    static class MockServletConfig implements ServletConfig
    {
        public String getInitParameter(String ignored)
        {
            return null;
        }

        public Enumeration getInitParameterNames()
        {
            return null;
        }

        public ServletContext getServletContext()
        {
            return new MockServletContext();
        }

        public String getServletName()
        {
            return "VelocityServlet";
        }            
    }

    static class MockServletContext implements ServletContext
    {
        public Object getAttribute(String ignored)
        {
            return null;
        }

        public Enumeration getAttributeNames()
        {
            return null;
        }

        public ServletContext getContext(String ignored)
        {
            return this;
        }

        public String getServletContextName()
        {
            return "VelocityTestContext";
        }

        public String getInitParameter(String ignored)
        {
            return null;
        }

        public Enumeration getInitParameterNames()
        {
            return null;
        }

        public int getMajorVersion()
        {
            return -1;
        }

        public String getMimeType(String ignored)
        {
            return null;
        }

        public Set getResourcePaths(String string)
        {
            return null;
        }

        public int getMinorVersion()
        {
            return -1;
        }

        public RequestDispatcher getNamedDispatcher(String ignored)
        {
            return null;
        }

        public String getRealPath(String ignored)
        {
            return null;
        }

        public RequestDispatcher getRequestDispatcher(String ignored)
        {
            return null;
        }

        public URL getResource(String ignored)
            throws MalformedURLException
        {
            return null;
        }

        public InputStream getResourceAsStream(String ignored)
        {
            return null;
        }

        public String getServerInfo()
        {
            return "Velocity Test Suite";
        }

        public Servlet getServlet(String ignored)
            throws ServletException
        {
            return null;
        }

        public Enumeration getServletNames()
        {
            return null;
        }

        public Enumeration getServlets()
        {
            return null;
        }

        public void log(Exception e, String msg)
        {
        }

        public void log(String msg)
        {
        }

        public void log(String msg, Throwable t)
        {
        }

        public void removeAttribute(String name)
        {
        }

        public void setAttribute(String name, Object value)
        {
        }
    }

    static class MockHttpServletResponse implements HttpServletResponse
    {
        private String encoding;

        // ---- ServletResponse implementation -----------------------------

        public void flushBuffer() throws IOException
        {
        }

        public void resetBuffer()
        {
        }

        public int getBufferSize()
        {
            return -1;
        }

        public String getCharacterEncoding()
        {
            return (encoding != null ? encoding : "ISO-8859-1");
        }

        public java.util.Locale getLocale()
        {
            return null;
        }

        public javax.servlet.ServletOutputStream getOutputStream()
            throws IOException
        {
            return null;
        }

        public java.io.PrintWriter getWriter() throws IOException
        {
            return null;
        }

        public boolean isCommitted()
        {
            return false;
        }

        public void reset()
        {
        }

        public void setBufferSize(int i)
        {
        }

        public void setContentLength(int i)
        {
        }

        /**
         * Records the character encoding.
         */
        public void setContentType(String contentType)
        {
            if (contentType != null)
            {
                int index = contentType.lastIndexOf(';') + 1;
                if (0 <= index || index < contentType.length())
                {
                    index = contentType.indexOf("charset=", index);
                    if (index != -1)
                    {
                        index += 8;
                        this.encoding = contentType.substring(index).trim();
                    }
                }
            }
        }

        public void setLocale(java.util.Locale l)
        {
        }


        // ---- HttpServletResponse implementation ------------------------- 

        public void addCookie(javax.servlet.http.Cookie c)
        {
        }

        public void addDateHeader(String s, long l)
        {
        }

        public void addHeader(String name, String value)
        {
        }

        public void addIntHeader(String name, int value)
        {
        }

        public boolean containsHeader(String name)
        {
            return false;
        }

        public String encodeRedirectURL(String url)
        {
            return url;
        }

        public String encodeRedirectUrl(String url)
        {
            return url;
        }

        public String encodeURL(String url)
        {
            return url;
        }

        public String encodeUrl(String url)
        {
            return url;
        }

        public void sendError(int i) throws IOException
        {
        }

        public void sendError(int i, String s) throws IOException
        {
        }

        public void sendRedirect(String s) throws IOException
        {
        }

        public void setDateHeader(String s, long l)
        {
        }

        public void setHeader(String name, String value)
        {
        }

        public void setIntHeader(String s, int i)
        {
        }

        public void setStatus(int i)
        {
        }

        public void setStatus(int i , String s)
        {
        }
    }
}
