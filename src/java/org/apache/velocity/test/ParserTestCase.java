package org.apache.velocity.test;
/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;

import java.io.StringWriter;

/**
 *  More specific parser tests where just templating
 *  isn't enough.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id: ParserTestCase.java,v 1.1 2002/03/25 00:40:55 geirm Exp $
 */
public class ParserTestCase extends TestCase
{
    public ParserTestCase(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
       return new TestSuite(ParserTestCase.class);
    }

    /**
     *  Test to make sure that using '=' in #if() throws a PEE
     */
    public void testEquals()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();

        ve.init();

        /*
         *  this should parse fine -> uses ==
         */

        String template = "#if($a == $b) foo #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

        /*
         *  this should throw an exception
         */

        template = "#if($a = $b) foo #end";

        try
        {
            ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);
            assertTrue(false);
        }
        catch(ParseErrorException pe)
        {
        }
    }

    /**
     *  Test to see if we force the first arg to #macro() to be a word
     */
    public void testMacro()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();

        ve.init();

        /*
         * this should work
         */

        String template = "#macro(foo) foo #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

         /*
         *  this should throw an exception
         */

        template = "#macro($x) foo #end";

        try
        {
            ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);
            assertTrue(false);
        }
        catch(ParseErrorException pe)
        {
        }
    }

    /**
     *  Test to see if don't tolerage passing word tokens in anything but the
     *  0th arg to #macro() and the 1th arg to foreach()
     */
    public void testArgs()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();

        ve.init();

        /*
         * this should work
         */

        String template = "#macro(foo) foo #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

         /*
         *  this should work - spaces intentional
         */

        template = "#foreach(  $i     in  $woogie   ) end #end";

        ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);

        /*
        *  this should bomb
        */

       template = "#macro(   foo $a) $a #end #foo(woogie)";

        try
        {
            ve.evaluate(new VelocityContext(), new StringWriter(), "foo", template);
            assertTrue(false);
        }
        catch(ParseErrorException pe)
        {
            System.out.println("Caught pee!");
        }
    }

}
