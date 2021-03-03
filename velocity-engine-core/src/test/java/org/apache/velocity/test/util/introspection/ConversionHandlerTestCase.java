package org.apache.velocity.test.util.introspection;

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

import junit.framework.TestSuite;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.test.BaseTestCase;
import org.apache.velocity.test.misc.TestLogger;
import org.apache.velocity.util.introspection.Converter;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.IntrospectionUtils;
import org.apache.velocity.util.introspection.TypeConversionHandler;
import org.apache.velocity.util.introspection.TypeConversionHandlerImpl;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.UberspectImpl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    @Override
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
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, TEST_COMPARE_DIR + "/conversion");
        ve.init();
        Uberspect uberspect = ve.getUberspect();
        assertTrue(uberspect instanceof UberspectImpl);
        UberspectImpl ui = (UberspectImpl)uberspect;
        TypeConversionHandler ch = ui.getConversionHandler();
        assertTrue(ch != null);
        ch.addConverter(Float.class, Obj.class, new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                return 4.5f;
            }
        });
        ch.addConverter(TypeUtils.parameterize(List.class, Integer.class), String.class, new Converter<List<Integer>>()
        {
            @Override
            public List<Integer> convert(Object o)
            {
                return Arrays.<Integer>asList(1,2,3);
            }
        });
        ch.addConverter(TypeUtils.parameterize(List.class, String.class), String.class, new Converter<List<String>>()
        {
            @Override
            public List<String> convert(Object o)
            {
                return Arrays.<String>asList("a", "b", "c");
            }
        });
        VelocityContext context = new VelocityContext();
        context.put("obj", new Obj());
        Writer writer = new StringWriter();
        ve.evaluate(context, writer, "test", "$obj.integralFloat($obj) / $obj.objectFloat($obj)");
        assertEquals("float ok: 4.5 / Float ok: 4.5", writer.toString());
        writer = new StringWriter();
        ve.evaluate(context, writer, "test", "$obj.iWantAStringList('anything')");
        assertEquals("correct", writer.toString());
        writer = new StringWriter();
        ve.evaluate(context, writer, "test", "$obj.iWantAnIntegerList('anything')");
        assertEquals("correct", writer.toString());
    }

    /* converts *everything* to string "foo" */
    public static class MyCustomConverter implements TypeConversionHandler
    {
        Converter<String> myCustomConverter = new Converter<String>()
        {

            @Override
            public String convert(Object o)
            {
                return "foo";
            }
        };

        @Override
        public boolean isExplicitlyConvertible(Type formal, Class<?> actual, boolean possibleVarArg)
        {
            return true;
        }

        @Override
        public Converter<?> getNeededConverter(Type formal, Class<?> actual)
        {
            return myCustomConverter;
        }

        @Override
        public void addConverter(Type formal, Class<?> actual, Converter<?> converter)
        {
            throw new RuntimeException("not implemented");
        }
    }

    public void testCustomConversionHandlerInstance()
    {
        RuntimeInstance ve = new RuntimeInstance();
        ve.setProperty( Velocity.VM_PERM_INLINE_LOCAL, Boolean.TRUE);
        ve.setProperty(Velocity.RUNTIME_LOG_INSTANCE, log);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, TEST_COMPARE_DIR + "/conversion");
        ve.setProperty(RuntimeConstants.CONVERSION_HANDLER_INSTANCE, new MyCustomConverter());
        ve.init();
        Uberspect uberspect = ve.getUberspect();
        assertTrue(uberspect instanceof UberspectImpl);
        UberspectImpl ui = (UberspectImpl)uberspect;
        TypeConversionHandler ch = ui.getConversionHandler();
        assertTrue(ch != null);
        assertTrue(ch instanceof MyCustomConverter);
        VelocityContext context = new VelocityContext();
        context.put("obj", new Obj());
        Writer writer = new StringWriter();
        ve.evaluate(context, writer, "test", "$obj.objectString(1.0)");
        assertEquals("String ok: foo", writer.toString());
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

        log.setEnabledLevel(TestLogger.LOG_LEVEL_ERROR);

        Template template = ve.getTemplate(templateFile);
        template.merge(context, writer);

        /*
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

    public void testOtherConversions() throws Exception
    {
        VelocityEngine ve = createEngine(false);
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();
        ve.evaluate(context, writer,"test", "$strings.join(['foo', 'bar'], ',')");
        assertEquals("foo,bar", writer.toString());
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
        ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "file");
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
        @Override
        public Object methodException(Context context,
                                      Class claz,
                                      String method,
                                      Exception e,
                                      Info info)
        {
            // JDK 11+ changed the exception message for big decimal conversion exceptions,
            // which breaks the (brittle) tests. Clearly, it would be preferred to fix this
            // right by comparing the result according to the JDK version, this is just a
            // quick fix to get the build to pass on JDK 11+
            //
            if (e.getClass() == NumberFormatException.class  && e.getMessage() != null && e.getMessage().startsWith("Character"))
            {
                return method + " -> " + e.getClass().getSimpleName() + ": null"; // compatible with JDK8
            }

            return method + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private VelocityContext createContext()
    {
        VelocityContext context = new VelocityContext();
        Map<String, Object> map = new TreeMap<>();
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
        map.put("V. locale", "fr_FR");
        map.put("W. BigInteger zero", BigInteger.ZERO);
        map.put("X. BigInteger one", BigInteger.ONE);
        map.put("Y. BigInteger ten", BigInteger.TEN);
        map.put("Y. BigInteger bigint", new BigInteger("12345678901234567890"));
        map.put("Z. BigDecimal zero", BigDecimal.ZERO);
        map.put("ZA. BigDecimal one", BigDecimal.ONE);
        map.put("ZB. BigDecimal ten", BigDecimal.TEN);
        map.put("ZC. BigDecimal bigdec", new BigDecimal("12345678901234567890.01234567890123456789"));
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
                        BigInteger.class,
                        Float.class,
                        Double.class,
                        BigDecimal.class,
                        Number.class,
                        String.class,
                        Object.class
                };
        context.put("types", types);
        context.put("introspect", new Introspect());
        context.put("strings", new StringUtils());
        return context;
    }

    public static class Obj
    {
        public enum Color { RED, GREEN }

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
        public String objectBigInteger(BigInteger bi) { return "BigInteger ok: " + bi; }
        public String objectFloat(Float f) { return "Float ok: " + f; }
        public String objectDouble(Double d) { return "Double ok: " + d; }
        public String objectBigDecimal(BigDecimal bd) { return "BigDecimal ok: " + bd; }
        public String objectCharacter(Character c) { return "Character ok: " + c; }
        public String objectNumber(Number b) { return "Number ok: " + b; }
        public String objectObject(Object o) { return "Object ok: " + o; }
        public String objectString(String s) { return "String ok: " + s; }
        public String objectEnum(Color c) { return "Enum ok: " + c; }
        public String locale(Locale loc) { return "Locale ok: " + loc; }

        public String toString() { return "instance of Obj"; }

        public String iWantAStringList(List<String> list)
        {
            if (list != null && list.size() == 3 && list.get(0).equals("a") && list.get(1).equals("b") && list.get(2).equals("c"))
                return "correct";
            else return "wrong";
        }

        public String iWantAnIntegerList(List<Integer> list)
        {
            if (list != null && list.size() == 3 && list.get(0).equals(1) && list.get(1).equals(2) && list.get(2).equals(3))
                return "correct";
            else return "wrong";
        }
    }

    public static class Introspect
    {
        private TypeConversionHandler handler;
        public Introspect()
        {
            handler = new TypeConversionHandlerImpl();
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
