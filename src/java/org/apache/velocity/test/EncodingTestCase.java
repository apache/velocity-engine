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
import java.io.Writer;

import java.util.Vector;

import org.apache.velocity.VelocityContext;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.test.provider.TestProvider;
import org.apache.velocity.util.StringUtils;

import junit.framework.TestCase;

/**
 * Tests input encoding handling.  The input target is UTF-8, having
 * chinese and and a spanish enyay (n-twiddle)
 *
 *  Thanks to Kent Johnson for the example input file.
 *
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: EncodingTestCase.java,v 1.4 2001/08/07 22:20:28 geirm Exp $
 */
public class EncodingTestCase extends BaseTestCase implements TemplateTestBase
{
    public EncodingTestCase()
    {
        super("EncodingTestCase");
        
        try
        {
	        Velocity.setProperty(
	            Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
	        
            Velocity.setProperty( Velocity.INPUT_ENCODING, "UTF-8" );

            Velocity.init();
	    }
	    catch (Exception e)
	    {
            System.err.println("Cannot setup EncodingTestCase!");
            e.printStackTrace();
            System.exit(1);
	    }
    }

    public static junit.framework.Test suite()
    {
        return new EncodingTestCase();
    }

    /**
     * Runs the test.
     */
    public void runTest ()
    {
        
        VelocityContext context = new VelocityContext();
       
        try
        {
            assureResultsDirectoryExists(RESULT_DIR);
            
            /*
             *  get the template and the output
             */

            /*
             *  Chinese and spanish
             */

            Template template = Velocity.getTemplate(
                getFileName(null, "encodingtest", TMPL_FILE_EXT), "UTF-8");

            FileOutputStream fos = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "encodingtest", RESULT_FILE_EXT));

            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
           
            template.merge(context, writer);
            writer.flush();
            writer.close();
            
            if (!isMatch(RESULT_DIR,COMPARE_DIR,"encodingtest",
                    RESULT_FILE_EXT,CMP_FILE_EXT) )
            {
                fail("Output 1 incorrect.");
            }

            /*
             *  a 'high-byte' chinese example from Michael Zhou
             */

            template = Velocity.getTemplate( 
                  getFileName( null, "encodingtest2", TMPL_FILE_EXT), "UTF-8");

            fos = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "encodingtest2", RESULT_FILE_EXT));

            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
           
            template.merge(context, writer);
            writer.flush();
            writer.close();
            
            if (!isMatch(RESULT_DIR,COMPARE_DIR,"encodingtest2",
                    RESULT_FILE_EXT,CMP_FILE_EXT) )
            {
                fail("Output 2 incorrect.");
            }

            /*
             *  a 'high-byte' chinese from Ilkka
             */

            template = Velocity.getTemplate( 
                  getFileName( null, "encodingtest3", TMPL_FILE_EXT), "GBK");

            fos = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "encodingtest3", RESULT_FILE_EXT));

            writer = new BufferedWriter(new OutputStreamWriter(fos, "GBK"));
           
            template.merge(context, writer);
            writer.flush();
            writer.close();
            
            if (!isMatch(RESULT_DIR,COMPARE_DIR,"encodingtest3",
                    RESULT_FILE_EXT,CMP_FILE_EXT) )
            {
                fail("Output 3 incorrect.");
            }

            /*
             *  Russian example from Vitaly Repetenko
             */

            template = Velocity.getTemplate( 
                  getFileName( null, "encodingtest_KOI8-R", TMPL_FILE_EXT), "KOI8-R");

            fos = 
                new FileOutputStream (
                    getFileName(RESULT_DIR, "encodingtest_KOI8-R", RESULT_FILE_EXT));

            writer = new BufferedWriter(new OutputStreamWriter(fos, "KOI8-R"));
           
            template.merge(context, writer);
            writer.flush();
            writer.close();
            
            if (!isMatch(RESULT_DIR,COMPARE_DIR,"encodingtest_KOI8-R",
                    RESULT_FILE_EXT,CMP_FILE_EXT) )
            {
                fail("Output 4 incorrect.");
            }
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}



