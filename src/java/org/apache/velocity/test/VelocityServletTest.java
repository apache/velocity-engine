package org.apache.velocity.test;

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

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.Velocity;
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
