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

/**
 * This is a test case for Anakia. Right now, it simply will compare
 * two index.html files together. These are produced as a result of
 * first running Anakia and then running this test.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: AnakiaTestCase.java,v 1.6 2003/05/04 17:46:35 geirm Exp $
 */
public class AnakiaTestCase extends BaseTestCase
{
    private static final String COMPARE_DIR = "../test/anakia/compare";
    private static final String RESULTS_DIR = "../test/anakia/results";
    private static final String FILE_EXT = ".html";

    /**
     * Creates a new instance.
     *
     */
    public AnakiaTestCase()
    {
        super("AnakiaTestCase");
    }

    public static junit.framework.Test suite()
    {
        return new AnakiaTestCase();
    }

    /**
     * Runs the test. This is empty on purpose because the
     * code to do the Anakia output is in the .xml file that runs
     * this test.
     */
    public void runTest ()
    {
        try
        {
            assureResultsDirectoryExists(RESULTS_DIR);
            
            if (!isMatch(RESULTS_DIR,COMPARE_DIR,"index",FILE_EXT,FILE_EXT))
            {
                fail("Output is incorrect!");
            }
            else
            {
                System.out.println ("Passed!");
            }
        }            
        catch(Exception e)
        {
            /*
             * do nothing.
             */
        }
    }
}
