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

import org.apache.velocity.util.introspection.Introspector;

/**
 * Test case for the Velocity Introspector which uses
 * the Java Reflection API to determine the correct
 * signature of the methods used in VTL templates.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: IntrospectorTestCase.java,v 1.1 2000/11/10 04:07:46 jvanzyl Exp $
 */
public class IntrospectorTestCase extends BaseTestCase
{
    /**
      * Creates a new instance.
      */
    public IntrospectorTestCase (String name)
    {
        super(name);
    }

    public void runTest()
    {
        MethodProvider mp = new MethodProvider();
    
        Class[] booleanParam = { Boolean.class };
        Class[] byteParam = { Byte.class };
        Class[] charParam = { Character.class };
        Class[] doubleParam = { Double.class };
        Class[] floatParam = { Float.class };
        Class[] intParam = { Integer.class };
        Class[] longParam = { Long.class };
        Class[] shortParam = { Short.class };
    }

    /**
      * Get the containing <code>TestSuite</code>.  This is always
      * <code>VelocityTestSuite</code>.
      *
      * @return The <code>TestSuite</code> to run.
      */
    public static junit.framework.Test suite ()
    {
        return new VelocityTestSuite();
    }

    static class MethodProvider
    {
        /*
         * Methods with native parameter types.
         */
        public String booleanMethod (boolean p) { return "boolean"; }
        public String byteMethod (byte p) { return "byte"; }
        public String charMethod (char p) { return "char"; }
        public String doubleMethod (double p) { return "double"; }
        public String floatMethod (float p) { return "float"; }
        public String intMethod (int p) { return "int"; }
        public String longMethod (long p) { return "long"; }
        public String shortMethod (short p) { return "short"; }
    }
}
