package org.apache.velocity.example;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;

/**
 * This class is a simple demonstration of how the Velocity Template Engine
 * can be used in a standalone application using the Velocity utility class.
 *
 * It demonstrates two of the 'helper' methods found in the org.apache.velocity.util.Velocity
 * class, mergeTemplate() and evaluate().
 *
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id$
 */

public class Example2
{
    public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        try
        {
            Velocity.init();
        }
        catch(Exception e)
        {
            System.out.println("Problem initializing Velocity : " + e );
            return;
        }

        /* lets make a Context and put data into it */

        VelocityContext context = new VelocityContext();

        context.put("name", "Velocity");
        context.put("project", "Jakarta");

        /* lets render a template */

        StringWriter w = new StringWriter();

        try
        {
            Velocity.mergeTemplate("example2.vm", "ISO-8859-1", context, w );
        }
        catch (Exception e )
        {
            System.out.println("Problem merging template : " + e );
        }

        System.out.println(" template : " + w );

        /*
         *  lets dynamically 'create' our template
         *  and use the evaluate() method to render it
         */

        String s = "We are using $project $name to render this.";
        w = new StringWriter();

        try
        {
            Velocity.evaluate( context, w, "mystring", s );
        }
        catch( ParseErrorException pee )
        {
            /*
             * thrown if something is wrong with the
             * syntax of our template string
             */
            System.out.println("ParseErrorException : " + pee );
        }
        catch( MethodInvocationException mee )
        {
            /*
             *  thrown if a method of a reference
             *  called by the template
             *  throws an exception. That won't happen here
             *  as we aren't calling any methods in this
             *  example, but we have to catch them anyway
             */
            System.out.println("MethodInvocationException : " + mee );
        }
        catch( Exception e )
        {
            System.out.println("Exception : " + e );
        }

        System.out.println(" string : " + w );
    }
}
