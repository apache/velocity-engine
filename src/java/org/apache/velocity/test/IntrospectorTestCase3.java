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

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Method;

import org.apache.velocity.runtime.RuntimeSingleton;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *  Simple introspector test case for primitive problem found in 1.3
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id: IntrospectorTestCase3.java,v 1.1.2.2 2002/07/14 22:04:09 geirm Exp $
 */
public class IntrospectorTestCase3 extends BaseTestCase
{
    /**
      * Creates a new instance.
      */
    public IntrospectorTestCase3(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(IntrospectorTestCase3.class);
    }

    public void testSimple()
        throws Exception
    {
        Method method;
        String result;
        String type;

        MethodProvider mp = new MethodProvider();

        /*
         * string integer
         */

        Object[] listIntInt = { new ArrayList(), new Integer(1), new Integer(2) };
        Object[] intInt = {  new Integer(1), new Integer(2) };
        Object[] longInt = {  new Long(1), new Integer(2) };
        Object[] longLong = {  new Long(1), new Long(2) };

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "lii", listIntInt);
        result = (String) method.invoke(mp, listIntInt);

        assertTrue(result.equals("lii"));

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "ii", intInt);
        result = (String) method.invoke(mp, intInt);

        assertTrue(result.equals("ii"));

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "ll", longInt);
        result = (String) method.invoke(mp, longInt);

        assertTrue(result.equals("ll"));

        method = RuntimeSingleton.getIntrospector().getMethod(
            MethodProvider.class, "ll", longLong);
        result = (String) method.invoke(mp, longLong);

        assertTrue(result.equals("ll"));
    }

    public static class MethodProvider
    {
        public String ii(int p, int d)
        {
            return "ii";
        }

        public String lii(List s, int p, int d)
        {
            return "lii";
        }

        public String lll(List s, long p, long d)
        {
            return "lll";
        }
        public String ll(long p, long d)
        {
            return "ll";
        }

    }
}
