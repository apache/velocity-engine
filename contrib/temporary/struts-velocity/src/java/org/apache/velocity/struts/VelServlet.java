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

import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.Properties;

import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import org.apache.velocity.struts.MessageBean;
import org.apache.velocity.struts.ErrorsBean;

import org.apache.struts.action.Action;


/**
 * <p>
 *  Kluged class to handle rendering of Velocity templates for
 *  the <a href="http://jakarta.apache.org/struts/">Struts</a> framework.
 *  </p>
 *  <p>
 *  This ain't done...
 *  </p>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * $Id: VelServlet.java,v 1.1 2001/04/16 03:27:55 geirm Exp $
 */
public class VelServlet extends VelocityServlet
{  
    /**
     *  sets up the configuration for Velocity.  Right now
     *  we just want to set the path for the template loader
     *
     *  @param config ServletConfig - can use to get params if needed
     *  @return Properties used for initializing Velocity
     */
    protected Properties loadConfiguration(ServletConfig config )
        throws IOException, FileNotFoundException
    {
        Properties p = new Properties();
        
        String path = getServletContext().getRealPath("/");
        p.setProperty("file.resource.loader.path", getServletContext().getRealPath("/") );
        p.setProperty("runtime.log", getServletContext().getRealPath("/velocity.log") );

        return p;
    }
        
    /**
     *  Handled the request.  Current responsibilities :
     *  <ul>
     *  <li> fill context with all context/session/request attributes
     *  <li> create MessageBean to handle Strut's message resource system
     *  <li> create ErrorsBean to handle Strut's Action error system
     *  <li> find and return Template
     *  </ul>
     *  @param request client request
     *  @param response client response
     *  @param ctx  VelocityContext to fill
     *  @return Velocity Template object or null
     */
    protected Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context ctx ) 
        throws Exception
    {

        ServletContext sc = getServletContext();
        HttpSession sess = request.getSession();

        /*
         *  we need to fill the context with whatever we can find.  Need a better way of doing this
         *  but there doesn't seem to be a map of what Struts shoves where
         */
        
        fillContext( ctx, sc, sess, request );

        /*
         *  kludge - this should really be externally specified
         */

        ctx.put("message", new MessageBean(sc, sess, request) );
        ctx.put("errors", new ErrorsBean(sc, sess, request));

        return getTemplate(request.getServletPath() );
    }

    /**
     *  kludgy routine to scrape out all 'stuff' punched into the 3 contexts by Struts
     */
    private boolean fillContext( Context ctx, ServletContext sc, HttpSession sess, HttpServletRequest request )
    {
        /* --- CONTEXT --- */

        for (Enumeration e = sc.getAttributeNames(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();
            Object o = sc.getAttribute( name );

            if ( o != null )
                ctx.put(name, o );         
        }

        /* ---- SESSION ----- */

       for (Enumeration e = sess.getAttributeNames(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();

            Object o = sess.getAttribute( name );

            if ( o != null )
                ctx.put(name, o );

            /*
             *  little kludge to get the transaction toke key thing to work
             */

            if (name.equals( Action.TRANSACTION_TOKEN_KEY ))
            {
                System.out.println("SessionToken => " + o );
                ctx.put( "sessionToken", o );
            }
        }

        /* --- REQUEST --- */

        for (Enumeration e = request.getAttributeNames(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();

            Object o = request.getAttribute( name );

            if ( o != null )
                ctx.put(name, o );
        }

        return true;
    }

}



