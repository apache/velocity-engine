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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import org.apache.velocity.util.FieldMethodizer;

import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.test.provider.TestProvider;

/**
 * This class the testbed for Velocity. It is used to
 * test all the directives support by Velocity.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Test.java,v 1.12 2001/01/21 20:54:00 geirm Exp $
 */
public class Test
{
    /**
     * Cache of writers
     */
    private static Stack writerStack = new Stack();

    public Test(String templateFile)
    {
        Writer writer = null;
        TestProvider provider = new TestProvider();
        ArrayList al = provider.getCustomers();
        Hashtable h = new Hashtable();
  
        /*
         *  put this in to test introspection $h.Bar or $h.get("Bar") etc
         */
        
        h.put("Bar", "this is from a hashtable!");
       
        /*
         *  adding simple vector with strings for testing late introspection stuff
         */

        Vector v = new Vector();

        v.addElement( new String("hello") );
        v.addElement( new String("hello2") );
            
        try
        {
            /*
             *  this is another way to do properties when initializing Runtime.
             *  make a Properties 
             */

            Properties p = new Properties();

            /*
             *  now, if you want to, load it from a file (or whatever)
             */
            
            try
            {
                FileInputStream fis =  new FileInputStream( 
                    new File("velocity.properties" ));
            
                if( fis != null)
                    p.load( fis );
            }
            catch (Exception ex)
            {
                /* no worries. no file... */
            }

            /*
             *  add some individual properties if you wish
             */

            p.setProperty(Runtime.RUNTIME_LOG_ERROR_STACKTRACE, "true");
            p.setProperty(Runtime.RUNTIME_LOG_WARN_STACKTRACE, "true");
            p.setProperty(Runtime.RUNTIME_LOG_INFO_STACKTRACE, "true");
            
            /*
             *  and now call init
             */

            Runtime.init(p);

            /*
             *  now, do what we want to do.  First, get the Template
             */

            if (templateFile == null)
                templateFile = "examples/example.vm";
         
            Template template = Runtime.getTemplate(templateFile);

            /*
             * now, make a Context object and populate it.
             */

            VelocityContext context = new VelocityContext();

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
            context.put("vector", v);
            context.put("mystring", new String());
            context.put("hashmap", new HashMap() );
            context.put("runtime", new FieldMethodizer( "org.apache.velocity.runtime.Runtime" ));
            context.put("provider", new FieldMethodizer( provider ));
 
            /*
             *  make a writer, and merge the template 'against' the context
             */

 
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
            template.merge( context , writer);

            writer.flush();
            writer.close();
        }
        catch( Exception e )
        {
            Runtime.error(e);
        }
    }

    public static void main(String[] args)
    {
        Test t;
        t = new Test(args[0]);
    }
}
