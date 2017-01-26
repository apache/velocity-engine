/**
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
package org.apache.velocity.test.util.introspection;

import junit.framework.TestSuite;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.util.introspection.ConversionHandler;
import org.apache.velocity.util.introspection.ConversionHandlerImpl;
import org.apache.velocity.util.introspection.Converter;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.IntrospectionUtils;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.UberspectImpl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test case for conversion handler
 */
public class ConversionHandlerTestCase extends BaseTestCase
{
    private static final String RESULT_DIR = TEST_RESULT_DIR + "/conversion";

    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/conversion/compare";

    public ConversionHandlerTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        super.setUp();
    }

    /**
     * Test suite
     * @return test suite
     */
    public static junit.framework.Test suite()
    {
        return new TestSuite(ConversionHandlerTestCase.class);
    }

    public void testConversionsWithoutHandler()
    throws Exception
    {
        /*
         *  local scope, cache on
         */
        VelocityEngine ve = createEngine(false);

        testConversions(ve, "test_conv.vtl", "test_conv_without_handler");
    }

    public void testConversionsWithHandler()
            throws Exception
    {
        /*
         *  local scope, cache on
         */
        VelocityEngine ve = createEngine(true);

        testConversions(ve, "test_conv.vtl", "test_conv_with_handler");
    }

    public void testConversionMatrix()
            throws Exception
    {
        VelocityEngine ve = createEngine(true);
        testConversions(ve, "matrix.vhtml", "matrix");
    }

    public void testCustomConverter()
    {
        RuntimeInstance ve = new RuntimeInstance();
        ve.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve.setProperty(Velocity.RUNTIME_LOG_INSTANCE, log);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, TEST_COMPARE_DIR + "/conversion");
        ve.init();
        Uberspect uberspect = ve.getUberspect();
        assertTrue(uberspect instanceof UberspectImpl);
        UberspectImpl ui = (UberspectImpl)uberspect;
        ConversionHandler ch = ui.getConversionHandler();
        assertTrue(ch != null);
        ch.addConverter(Float.class, Obj.class, new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                return 4.5f;
            }
        });
        VelocityContext context = new VelocityContext();
        context.put("obj", new Obj());
        Writer writer = new StringWriter();
        ve.evaluate(context, writer, "test", "$obj.integralFloat($obj) / $obj.objectFloat($obj)");
        assertEquals("float ok: 4.5 / Float ok: 4.5", writer.toString());
    }

    /**
     * Test conversions
     * @param ve
     * @param templateFile template
     * @param outputBaseFileName
     * @throws Exception
     */
    private void testConversions(VelocityEngine ve, String templateFile, String outputBaseFileName)
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);

        FileOutputStream fos = new FileOutputStream (getFileName(
                RESULT_DIR, outputBaseFileName, RESULT_FILE_EXT));

        VelocityContext context = createContext();

        Writer writer = new BufferedWriter(new OutputStreamWriter(fos));

        Template template = ve.getTemplate(templateFile);
        template.merge(context, writer);

        /**
         * Write to the file
         */
        writer.flush();
        writer.close();

        if (!isMatch(RESULT_DIR, COMPARE_DIR, outputBaseFileName,
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            String result = getFileContents(RESULT_DIR, outputBaseFileName, RESULT_FILE_EXT);
            String compare = getFileContents(COMPARE_DIR, outputBaseFileName, CMP_FILE_EXT);

            String msg = "Processed template did not match expected output\n"+
                "-----Result-----\n"+ result +
                "----Expected----\n"+ compare +
                "----------------";

            fail(msg);
        }
    }

    /**
     * Return and initialize engine
     * @return
     */
    private VelocityEngine createEngine(boolean withConversionsHandler)
    throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve.setProperty(Velocity.RUNTIME_LOG_INSTANCE, log);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, TEST_COMPARE_DIR + "/conversion");
        if (withConversionsHandler)
        {
            ve.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, PrintException.class.getName());
        }
        else
        {
            ve.setProperty(RuntimeConstants.CONVERSION_HANDLER_CLASS, "none");
        }
        ve.init();

        return ve;
    }

    public static class PrintException implements MethodExceptionEventHandler
    {
        public Object methodException(Context context,
                                      Class claz,
                                      String method,
                                      Exception e,
                                      Info info)
        {
            return method + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private VelocityContext createContext()
    {
        VelocityContext context = new VelocityContext();
        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("A. bool-true", true);
        map.put("B. bool-false", false);
        map.put("C. byte-0", (byte)0);
        map.put("D. byte-1", (byte)1);
        map.put("E. short", (short)125);
        map.put("F. int", 24323);
        map.put("G. long", 5235235L);
        map.put("H. float", 34523.345f);
        map.put("I. double", 54235.3253d);
        map.put("J. char", '@');
        map.put("K. object", new Obj());
        map.put("L. enum", Obj.Color.GREEN);
        map.put("M. string", new String("foo"));
        map.put("M. string-green", new String("green"));
        map.put("N. string-empty", new String());
        map.put("O. string-false", new String("false"));
        map.put("P. string-true", new String("true"));
        map.put("Q. string-zero", new String("0"));
        map.put("R. string-integral", new String("123"));
        map.put("S. string-big-integral", new String("12345678"));
        map.put("T. string-floating", new String("123.345"));
        map.put("U. null", null);
        context.put("map", map);
        context.put("target", new Obj());
        Class[] types =
                {
                        Boolean.TYPE,
                        Character.TYPE,
                        Byte.TYPE,
                        Short.TYPE,
                        Integer.TYPE,
                        Long.TYPE,
                        Float.TYPE,
                        Double.TYPE,
                        Boolean.class,
                        Character.class,
                        Byte.class,
                        Short.class,
                        Integer.class,
                        Long.class,
                        Float.class,
                        Double.class,
                        Number.class,
                        String.class,
                        Object.class
                };
        context.put("types", types);
        context.put("introspect", new Introspect());
        return context;
    }

    public static class Obj
    {
        public enum Color { RED, GREEN };

        public String integralBoolean(boolean b) { return "boolean ok: " + b; }
        public String integralByte(byte b) { return "byte ok: " + b; }
        public String integralShort(short s) { return "short ok: " + s; }
        public String integralInt(int i) { return "int ok: " + i; }
        public String integralLong(long l) { return "long ok: " + l; }
        public String integralFloat(float f) { return "float ok: " + f; }
        public String integralDouble(double d) { return "double ok: " + d; }
        public String integralChar(char c) { return "char ok: " + c; }
        public String objectBoolean(Boolean b) { return "Boolean ok: " + b; }
        public String objectByte(Byte b) { return "Byte ok: " + b; }
        public String objectShort(Short s) { return "Short ok: " + s; }
        public String objectInt(Integer i) { return "Integer ok: " + i; }
        public String objectLong(Long l) { return "Long ok: " + l; }
        public String objectFloat(Float f) { return "Float ok: " + f; }
        public String objectDouble(Double d) { return "Double ok: " + d; }
        public String objectCharacter(Character c) { return "Character ok: " + c; }
        public String objectNumber(Number b) { return "Number ok: " + b; }
        public String objectObject(Object o) { return "Object ok: " + o; }
        public String objectString(String s) { return "String ok: " + s; }
        public String objectEnum(Color c) { return "Enum ok: " + c; }

        public String toString() { return "instance of Obj"; }
    }

    public static class Introspect
    {
        private ConversionHandler handler;
        public Introspect()
        {
            handler = new ConversionHandlerImpl();
        }
        public boolean isStrictlyConvertible(Class expected, Class provided)
        {
            return IntrospectionUtils.isStrictMethodInvocationConvertible(expected, provided, false);
        }
        public boolean isImplicitlyConvertible(Class expected, Class provided)
        {
            return IntrospectionUtils.isMethodInvocationConvertible(expected, provided, false);
        }
        public boolean isExplicitlyConvertible(Class expected, Class provided)
        {
            return handler.isExplicitlyConvertible(expected, provided, false);
        }
    }
}
