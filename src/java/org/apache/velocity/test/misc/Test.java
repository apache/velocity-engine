package org.apache.velocity.test.misc;

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

import java.io.*;

import java.util.*;

import org.apache.velocity.Context;
import org.apache.velocity.Template;

import org.apache.velocity.io.*;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.test.provider.TestProvider;

/**
 * This class the testbed for Velocity. It is used to
 * test all the directives support by Velocity.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Test.java,v 1.4 2000/11/04 04:58:52 jon Exp $
 */
public class Test
{
    /**
     * Cache of writers
     */
    private static Stack writerStack = new Stack();

    public Test(String templateFile)
    {
//        Writer writer = null;
        JspWriterImpl vw = null;
        TestProvider provider = new TestProvider();
        ArrayList al = provider.getCustomers();
        Hashtable h = new Hashtable();
        
        h.put("Bar", "this is from a hashtable!");

        try
        {
            Runtime.init("velocity.properties");
            if (templateFile == null)
                templateFile = "examples/example.vm";
            Template template = Runtime.getTemplate(templateFile);

            Context context = new Context();
            context.put("provider", provider);
            context.put("name", "jason");
            context.put("providers", provider.getCustomers2());
            context.put("list", al);
            context.put("hashtable", h);
            context.put("search", provider.getSearch());
            context.put("relatedSearches", provider.getRelSearches());
            context.put("searchResults", provider.getRelSearches());
            context.put("menu", provider.getMenu());
            context.put("stringarray", provider.getArray());

//            writer = new BufferedWriter(new OutputStreamWriter(System.out));
//            template.merge(context, writer);
            try
            {
                vw = (JspWriterImpl) writerStack.pop();
            }
            catch (Exception e)
            {
            }
            if (vw == null)
                vw = new JspWriterImpl(new OutputStreamWriter(System.out), 4*1024, true);
            else
                vw.recycle(new OutputStreamWriter(System.out));
            template.merge(context, vw);
            writerStack.push(vw);

        }
        catch( Exception e )
        {
            Runtime.error(e);
        }
        finally
        {
            try
            {
                if (vw != null)
                {
                    vw.flush();
                }                
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
    }

    public static void main(String[] args)
    {
        Test t;
        /*
        if (args.length > 0)
            t = new Test(args[0]);
        else
            t = new Test(null);
*/
            t = new Test(null);
            t = new Test(null);
            t = new Test(null);
            t = new Test(null);
            
    }
}
