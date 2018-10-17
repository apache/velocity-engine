package org.apache.velocity.util.introspection;

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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A conversion handler adds admissible conversions between Java types whenever Velocity introspection has to map
 * VTL methods and property accessors to Java methods. This implementation is the default Conversion Handler
 * for Velocity.
 *
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id: TypeConversionHandlerImpl.java $
 * @since 2.0
 */

public class TypeConversionHandlerImpl implements TypeConversionHandler
{
    /**
     * standard narrowing and string parsing conversions.
     */
    static Map<Pair<String, String>, Converter> standardConverterMap;

    /**
     * basic toString converter
     */
    static Converter toString;

    /**
     * cache miss converter
     */
    static Converter cacheMiss;

    /**
     * min/max byte/short/int values as long
     */
    static final long minByte = Byte.MIN_VALUE, maxByte = Byte.MAX_VALUE,
        minShort = Short.MIN_VALUE, maxShort = Short.MAX_VALUE,
        minInt = Integer.MIN_VALUE, maxInt = Integer.MAX_VALUE;

    /**
     * min/max long values as double
     */
    static final double minLong = Long.MIN_VALUE, maxLong = Long.MAX_VALUE;

    /**
     * a converters cache map, initialized with the standard narrowing and string parsing conversions.
     */
    Map<Pair<String, String>, Converter> converterCacheMap;

    static final String BOOLEAN_TYPE = "boolean";
    static final String BYTE_TYPE = "byte";
    static final String SHORT_TYPE = "short";
    static final String INTEGER_TYPE = "int";
    static final String LONG_TYPE = "long";
    static final String FLOAT_TYPE = "float";
    static final String DOUBLE_TYPE = "double";
    static final String CHARACTER_TYPE = "char";
    static final String BOOLEAN_CLASS = "java.lang.Boolean";
    static final String BYTE_CLASS = "java.lang.Byte";
    static final String SHORT_CLASS = "java.lang.Short";
    static final String INTEGER_CLASS = "java.lang.Integer";
    static final String LONG_CLASS = "java.lang.Long";
    static final String FLOAT_CLASS = "java.lang.Float";
    static final String DOUBLE_CLASS = "java.lang.Double";
    static final String NUMBER_CLASS = "java.lang.Number";
    static final String CHARACTER_CLASS = "java.lang.Character";
    static final String STRING_CLASS = "java.lang.String";

    static
    {
        standardConverterMap = new HashMap<>();

        cacheMiss = new Converter<Object>()
        {
            @Override
            public Object convert(Object o)
            {
                return o;
            }
        };

        /* number -> boolean */
        Converter<Boolean> numberToBool = new Converter<Boolean>()
        {
            @Override
            public Boolean convert(Object o)
            {
                return o == null ? null : ((Number) o).intValue() != 0;
            }
        };
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, BYTE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, SHORT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, INTEGER_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, LONG_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, FLOAT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, DOUBLE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, NUMBER_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, BYTE_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, SHORT_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, INTEGER_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, LONG_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, FLOAT_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, DOUBLE_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, BYTE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, SHORT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, INTEGER_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, LONG_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, FLOAT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, DOUBLE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, NUMBER_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, BYTE_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, SHORT_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, INTEGER_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, LONG_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, FLOAT_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, DOUBLE_TYPE), numberToBool);

        /* character -> boolean */
        Converter<Boolean> charToBoolean = new Converter<Boolean>()
        {
            @Override
            public Boolean convert(Object o)
            {
                return o == null ? null : (Character) o != 0;
            }
        };
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, CHARACTER_CLASS), charToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, CHARACTER_TYPE), charToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, CHARACTER_CLASS), charToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, CHARACTER_TYPE), charToBoolean);

        /* string -> boolean */
        Converter<Boolean> stringToBoolean = new Converter<Boolean>()
        {
            @Override
            public Boolean convert(Object o)
            {
                return Boolean.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, STRING_CLASS), stringToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, STRING_CLASS), stringToBoolean);

        /* narrowing towards byte */
        Converter<Byte> narrowingToByte = new Converter<Byte>()
        {
            @Override
            public Byte convert(Object o)
            {
                if (o == null) return null;
                long l = ((Number)o).longValue();
                if (l < minByte || l > maxByte)
                {
                    throw new NumberFormatException("value out of range for byte type: " + l);
                }
                return ((Number) o).byteValue();
            }
        };
        standardConverterMap.put(Pair.of(BYTE_CLASS, SHORT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, INTEGER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, LONG_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, FLOAT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, DOUBLE_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, NUMBER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, SHORT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, INTEGER_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, LONG_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, FLOAT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, DOUBLE_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, SHORT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, INTEGER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, LONG_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, FLOAT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, DOUBLE_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, NUMBER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, SHORT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, INTEGER_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, LONG_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, FLOAT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, DOUBLE_TYPE), narrowingToByte);

        /* string to byte */
        Converter<Byte> stringToByte = new Converter<Byte>()
        {
            @Override
            public Byte convert(Object o)
            {
                return Byte.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(BYTE_CLASS, STRING_CLASS), stringToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, STRING_CLASS), stringToByte);

        /* narrowing towards short */
        Converter<Short> narrowingToShort = new Converter<Short>()
        {
            @Override
            public Short convert(Object o)
            {
                if (o == null) return null;
                long l = ((Number)o).longValue();
                if (l < minShort || l > maxShort)
                {
                    throw new NumberFormatException("value out of range for short type: " + l);
                }
                return ((Number) o).shortValue();
            }
        };
        standardConverterMap.put(Pair.of(SHORT_CLASS, INTEGER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, LONG_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, FLOAT_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, DOUBLE_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, NUMBER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, INTEGER_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, LONG_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, FLOAT_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, DOUBLE_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, INTEGER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, LONG_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, FLOAT_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, DOUBLE_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, NUMBER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, INTEGER_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, LONG_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, FLOAT_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, DOUBLE_TYPE), narrowingToShort);

        /* string to short */
        Converter<Short> stringToShort = new Converter<Short>()
        {
            @Override
            public Short convert(Object o)
            {
                return Short.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(SHORT_CLASS, STRING_CLASS), stringToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, STRING_CLASS), stringToShort);

        /* narrowing towards int */
        Converter<Integer> narrowingToInteger = new Converter<Integer>()
        {
            @Override
            public Integer convert(Object o)
            {
                if (o == null) return null;
                long l = ((Number)o).longValue();
                if (l < minInt || l > maxInt)
                {
                    throw new NumberFormatException("value out of range for integer type: " + l);
                }
                return ((Number) o).intValue();
            }
        };
        standardConverterMap.put(Pair.of(INTEGER_CLASS, LONG_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, FLOAT_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, DOUBLE_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, NUMBER_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, LONG_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, FLOAT_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, DOUBLE_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, LONG_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, FLOAT_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, DOUBLE_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, NUMBER_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, LONG_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, FLOAT_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, DOUBLE_TYPE), narrowingToInteger);

        /* widening towards Integer */
        Converter<Integer> wideningToInteger = new Converter<Integer>()
        {
            @Override
            public Integer convert(Object o)
            {
                if (o == null) return null;
                return ((Number) o).intValue();
            }
        };
        standardConverterMap.put(Pair.of(INTEGER_CLASS, SHORT_CLASS), wideningToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, SHORT_TYPE), wideningToInteger);

        /* string to int */
        Converter<Integer> stringToInteger = new Converter<Integer>()
        {
            @Override
            public Integer convert(Object o)
            {
                return Integer.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(INTEGER_CLASS, STRING_CLASS), stringToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, STRING_CLASS), stringToInteger);

        /* narrowing towards long */
        Converter<Long> narrowingToLong = new Converter<Long>()
        {
            @Override
            public Long convert(Object o)
            {
                if (o == null) return null;
                double d = ((Number)o).doubleValue();
                if (d < minLong || d > maxLong)
                {
                    throw new NumberFormatException("value out of range for long type: " + d);
                }
                return ((Number) o).longValue();
            }
        };
        standardConverterMap.put(Pair.of(LONG_CLASS, FLOAT_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, DOUBLE_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, NUMBER_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, FLOAT_TYPE), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, DOUBLE_TYPE), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, FLOAT_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, DOUBLE_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, NUMBER_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, FLOAT_TYPE), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, DOUBLE_TYPE), narrowingToLong);

        /* widening towards Long */
        Converter<Long> wideningToLong = new Converter<Long>()
        {
            @Override
            public Long convert(Object o)
            {
                if (o == null) return null;
                return ((Number) o).longValue();
            }
        };
        standardConverterMap.put(Pair.of(LONG_CLASS, SHORT_CLASS), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, INTEGER_CLASS), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, SHORT_TYPE), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, INTEGER_TYPE), wideningToLong);

        /* string to long */
        Converter<Long> stringToLong = new Converter<Long>()
        {
            @Override
            public Long convert(Object o)
            {
                return Long.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(LONG_CLASS, STRING_CLASS), stringToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, STRING_CLASS), stringToLong);

        /* narrowing towards float */
        Converter<Float> narrowingToFloat = new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                return o == null ? null : ((Number) o).floatValue();
            }
        };
        standardConverterMap.put(Pair.of(FLOAT_CLASS, DOUBLE_CLASS), narrowingToFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, NUMBER_CLASS), narrowingToFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, DOUBLE_TYPE), narrowingToFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, DOUBLE_CLASS), narrowingToFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, NUMBER_CLASS), narrowingToFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, DOUBLE_TYPE), narrowingToFloat);

        /* exact towards Float */
        Converter<Float> toFloat = new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                if (o == null) return null;
                return ((Number) o).floatValue();
            }
        };
        standardConverterMap.put(Pair.of(FLOAT_CLASS, SHORT_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, INTEGER_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, LONG_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, SHORT_TYPE), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, INTEGER_TYPE), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, LONG_TYPE), toFloat);

        /* string to float */
        Converter<Float> stringToFloat = new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                return Float.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(FLOAT_CLASS, STRING_CLASS), stringToFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, STRING_CLASS), stringToFloat);

        /* exact or widening towards Double */
        Converter<Double> toDouble = new Converter<Double>()
        {
            @Override
            public Double convert(Object o)
            {
                if (o == null) return null;
                return ((Number) o).doubleValue();
            }
        };
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, SHORT_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, INTEGER_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, LONG_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, FLOAT_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, NUMBER_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, SHORT_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, INTEGER_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, LONG_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, FLOAT_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_TYPE, NUMBER_CLASS), toDouble);

        /* string to double */
        Converter<Double> stringToDouble = new Converter<Double>()
        {
            @Override
            public Double convert(Object o)
            {
                return Double.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, STRING_CLASS), stringToDouble);
        standardConverterMap.put(Pair.of(DOUBLE_TYPE, STRING_CLASS), stringToDouble);

        /* boolean to byte */
        Converter<Byte> booleanToByte = new Converter<Byte>()
        {
            @Override
            public Byte convert(Object o)
            {
                return o == null ? null : (Boolean) o ? (byte)1 : (byte)0;
            }
        };
        standardConverterMap.put(Pair.of(BYTE_CLASS, BOOLEAN_CLASS), booleanToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, BOOLEAN_TYPE), booleanToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, BOOLEAN_CLASS), booleanToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, BOOLEAN_TYPE), booleanToByte);

        /* boolean to short */
        Converter<Short> booleanToShort = new Converter<Short>()
        {
            @Override
            public Short convert(Object o)
            {
                return o == null ? null : (Boolean) o ? (short)1 : (short)0;
            }
        };
        standardConverterMap.put(Pair.of(SHORT_CLASS, BOOLEAN_CLASS), booleanToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, BOOLEAN_TYPE), booleanToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, BOOLEAN_CLASS), booleanToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, BOOLEAN_TYPE), booleanToShort);

        /* boolean to integer */
        Converter<Integer> booleanToInteger = new Converter<Integer>()
        {
            @Override
            public Integer convert(Object o)
            {
                return o == null ? null : (Boolean) o ? (Integer)1 : (Integer)0;
            }
        };
        standardConverterMap.put(Pair.of(INTEGER_CLASS, BOOLEAN_CLASS), booleanToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, BOOLEAN_TYPE), booleanToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, BOOLEAN_CLASS), booleanToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, BOOLEAN_TYPE), booleanToInteger);

        /* boolean to lonf */
        Converter<Long> booleanToLong = new Converter<Long>()
        {
            @Override
            public Long convert(Object o)
            {
                return o == null ? null : (Boolean) o ? 1L : 0L;
            }
        };
        standardConverterMap.put(Pair.of(LONG_CLASS, BOOLEAN_CLASS), booleanToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, BOOLEAN_TYPE), booleanToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, BOOLEAN_CLASS), booleanToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, BOOLEAN_TYPE), booleanToLong);

        /* to string */
        toString = new Converter<String>()
        {
            @Override
            public String convert(Object o)
            {
                return String.valueOf(o);
            }
        };
    }

    /**
     * Constructor
     */
    public TypeConversionHandlerImpl()
    {
        converterCacheMap = new ConcurrentHashMap<>();
    }

    /**
     * Check to see if the conversion can be done using an explicit conversion
     * @param actual found argument type
     * @param formal expected formal type
     * @return true if actual class can be explicitely converted to expected formal type
     * @since 2.1
     */
    @Override
    public boolean isExplicitlyConvertible(Type formal, Class actual, boolean possibleVarArg)
    {
        /*
         * for consistency, we also have to check standard implicit convertibility
         * since it may not have been checked before by the calling code
         */
        Class formalClass = IntrospectionUtils.getTypeClass(formal);
        if (formalClass != null && formalClass == actual ||
            IntrospectionUtils.isMethodInvocationConvertible(formal, actual, possibleVarArg) ||
            getNeededConverter(formal, actual) != null)
        {
            return true;
        }

        /* Check var arg */
        if (possibleVarArg && TypeUtils.isArrayType(formal))
        {
            if (actual.isArray())
            {
                actual = actual.getComponentType();
            }
            return isExplicitlyConvertible(TypeUtils.getArrayComponentType(formal), actual, false);
        }
        return false;
    }


    /**
     * Returns the appropriate Converter object needed for an explicit conversion
     * Returns null if no conversion is needed.
     *
     * @param actual found argument type
     * @param formal expected formal type
     * @return null if no conversion is needed, or the appropriate Converter object
     * @since 2.1
     */
    @Override
    public Converter getNeededConverter(Type formal, Class actual)
    {
        if (actual == null)
        {
            return null;
        }
        Pair<String, String> key = Pair.of(formal.getTypeName(), actual.getTypeName());

        /* first check for a standard conversion */
        Converter converter = standardConverterMap.get(key);
        if (converter == null)
        {
            /* then the converters cache map */
            converter = converterCacheMap.get(key);
            if (converter == null)
            {
                Class formalClass = IntrospectionUtils.getTypeClass(formal);
                /* check for conversion towards string */
                if (formal == String.class)
                {
                    converter = toString;
                }
                /* check for String -> Enum constant conversion */
                else if (formalClass != null && formalClass.isEnum() && actual == String.class)
                {
                    final Class<Enum> enumClass = (Class<Enum>)formalClass;
                    converter = new Converter()
                    {
                        @Override
                        public Object convert(Object o)
                        {
                            return Enum.valueOf(enumClass, (String) o);
                        }
                    };
                }

                converterCacheMap.put(key, converter == null ? cacheMiss : converter);
            }
        }
        return converter == cacheMiss ? null : converter;
    }

    /**
     * Add the given converter to the handler.
     *
     * @param formal expected formal type
     * @param actual provided argument type
     * @param converter converter
     * @since 2.1
     */
    @Override
    public void addConverter(Type formal, Class actual, Converter converter)
    {
        Pair<String, String> key = Pair.of(formal.getTypeName(), actual.getTypeName());
        converterCacheMap.put(key, converter);
        Class formalClass = IntrospectionUtils.getTypeClass(formal);
        if (formalClass != null)
        {
            if (formalClass.isPrimitive())
            {
                key = Pair.of(IntrospectionUtils.getBoxedClass(formalClass).getTypeName(), actual.getTypeName());
                converterCacheMap.put(key, converter);
            }
            else
            {
                Class unboxedFormal = IntrospectionUtils.getUnboxedClass(formalClass);
                if (unboxedFormal != formalClass)
                {
                    key = Pair.of(unboxedFormal.getTypeName(), actual.getTypeName());
                    converterCacheMap.put(key, converter);
                }
            }
        }
    }
}
