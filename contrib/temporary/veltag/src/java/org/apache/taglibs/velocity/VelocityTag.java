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

import java.io.Reader;
import java.util.Enumeration;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.BodyContent;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

/**
 *  <p>
 *  Simple implementation of JSP tag to allow 
 *  use of VTL in JSP's.
 *  </p>
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelocityTag.java,v 1.1 2001/08/14 00:07:39 geirm Exp $ 
 */
public class VelocityTag implements BodyTag
{
    protected Tag          parent = null;
    protected BodyContent  bodyContent = null;
    protected PageContext  pageContext = null;

    /*
     *  strictaccess : determines if the JSPContext is used
     *  to autofetch information from the 'scopes' 
     *  or if the scopetool() is used
     */
    protected boolean      strictAccess = false;

    /**
     *  CTOR : current implementation uses the Singleton
     *  model for velocity. 
     */
    public VelocityTag()
    {
        try
        {
            Velocity.init();
        }
        catch(Exception e)
        {
            System.out.println("VelocityTag() : " + e );
        }
    }

    /**
     *  switch for strictaccess
     *
     *  @param sa if true, then normal VelocityContext is used
     *            and template must directly get beans from scopes
     *            Otherwise, the JSPContext is used which searches for
     *            objects/beans in the scopes automatically.
     */
    public void setStrictaccess( boolean sa )
    {
        this.strictAccess = sa;
    }

    public Tag getParent()
    {
        return parent;
    }

    public void setParent( Tag parent)
    {
        this.parent = parent;
        return;
    }

    public int doStartTag()
        throws JspException
    {
        return EVAL_BODY_TAG;
    }

    public void setBodyContent( BodyContent bc )
    {
        this.bodyContent = bc;
        return;
    }

    public void setPageContext( PageContext pc )
    {
        this.pageContext = pc;
        return;
    }

    public void doInitBody()
        throws JspException
    {
        return;
    }
   
    public int doAfterBody()
        throws JspException
    {
        return 0;
    }

    public void release()            
    {
        return;
    } 

    /**
     *  This is the real worker for this taglib. 
     *  There are efficiencies to be added - the plan 
     *  is to cache the AST to avoid reparsing every
     *  time.
     */
    public int doEndTag()
        throws JspException
    {
        /*
         *  if there is no body, we are done
         */

        if ( bodyContent == null)
            return EVAL_PAGE;

        try
        {
            JspWriter writer = pageContext.getOut();

            /*
             *  get our body
             */

            Reader bodyreader = bodyContent.getReader();

            /*
             * now make a JSPContext
             */

            Context vc = null;

            /*
             *  if strictAccess == true, then we want to use a regular
             *  VelocityContext, as we assume that the template will
             *  bring into the context any beans using the scope tool
             *  or the like
             */
            if (strictAccess)
            {
                vc = (Context) new VelocityContext();
            }
            else
            {
               vc = (Context)  new JSPContext( pageContext );
            }

            /*
             *  add the scope tool
             */

            vc.put( "scopetool", new ScopeTool( pageContext ) );

            Velocity.evaluate( vc , writer, "JSP for me!", bodyreader );
        }
        catch( Exception e )
        {
            System.out.println( e.toString() );
        }

        return EVAL_PAGE;
    }
}
