package org.apache.velocity.test;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

import junit.framework.*;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.velocity.Context;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.test.provider.TestProvider;

/**
 * Automated test case for Apache Velocity.
 *
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: VelocityTest.java,v 1.6 2000/10/22 22:57:34 dlr Exp $
 */
public class VelocityTest extends TemplateTestCase
{
    /**
     * The name of the test case's template file.
     */
    private static final String TEMPLATE_FILE_NAME = "test.vm";

    private TestProvider provider;
    private ArrayList al;
    private Hashtable h;

    /**
     * Creates a new instance.
     */
    public VelocityTest (String name)
    {
        super(name);

        provider = new TestProvider();
        al = provider.getCustomers();
        h = new Hashtable();
    }

    /**
     * Get the containing <code>TestSuite</code>.
     *
     * @return The <code>TestSuite</code> to run.
     */
    public static junit.framework.Test suite ()
    {
        return TemplateTestCase.suite();
    }

    /**
     * Sets up the test.
     */
    protected void setUp ()
    {
        h.put("Bar", "this is from a hashtable!");
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        try
        {
            Context context = new Context();
            context.put("provider", provider);
            context.put("name", "jason");
            context.put("providers", provider.getCustomers2());
            context.put("list", al);
            context.put("hashtable", h);
            context.put("search", provider.getSearch());
            context.put("relatedSearches", provider.getRelSearches());
            context.put("searchResults", provider.getRelSearches());
            
            Template template = Runtime.getTemplate(TEMPLATE_FILE_NAME);
            template.merge(context, getWriter(System.out));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
