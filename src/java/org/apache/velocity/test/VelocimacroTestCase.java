package org.apache.velocity.test;

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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import java.util.Vector;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.app.Velocity;

import junit.framework.TestCase;

/**
 * This class tests strange Velocimacro issues.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: VelocimacroTestCase.java,v 1.1 2001/06/12 03:22:25 geirm Exp $
 */
public class VelocimacroTestCase extends TestCase 
{
    private String template1 = "#macro(foo $a)$a#end #macro(bar $b)#foo($b)#end #foreach($i in [1..3])#bar($i)#end";
    private String result1 = "  123";
    
    public VelocimacroTestCase()
    {
        super("VelocimacroTestCase");

        try
        {
            /*
             *  setup local scope for templates
             */
            Velocity.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
            Velocity.init();
        }
        catch (Exception e)
        {
            System.err.println("Cannot setup VelocimacroTestCase!");
            System.exit(1);
        }
    }

    public static junit.framework.Test suite()
    {
        return new VelocimacroTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        VelocityContext context = new VelocityContext();

        try
        {
            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, "vm_chain1", template1);

            String out = writer.toString();

            if( !result1.equals( out ) )
            {
                fail("output incorrect.");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
