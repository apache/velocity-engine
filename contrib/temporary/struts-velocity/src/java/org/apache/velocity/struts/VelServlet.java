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
 * $Id: VelServlet.java,v 1.1.12.1 2004/03/04 00:18:28 geirm Exp $
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



