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

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
    static Map<Pair<String, String>, Converter<?>> standardConverterMap;

    /**
     * basic toString converter
     */
    static Converter<?> toString;

    /**
     * cache miss converter
     */
    static Converter<?> cacheMiss;

    /**
     * a converters cache map, initialized with the standard narrowing and string parsing conversions.
     */
    Map<Pair<String, String>, Converter<?>> converterCacheMap;

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
    static final String BIG_INTEGER_CLASS = "java.math.BigInteger";
    static final String FLOAT_CLASS = "java.lang.Float";
    static final String DOUBLE_CLASS = "java.lang.Double";
    static final String BIG_DECIMAL_CLASS = "java.math.BigDecimal";
    static final String NUMBER_CLASS = "java.lang.Number";
    static final String CHARACTER_CLASS = "java.lang.Character";
    static final String STRING_CLASS = "java.lang.String";
    static final String LOCALE_CLASS = "java.util.Locale";

    /*
     * Bounds checking helper
     */

    static boolean checkBounds(Number n, double min, double max)
    {
        double d = n.doubleValue();
        if (d < min || d > max)
        {
            throw new NumberFormatException("value out of range: " + n);
        }
        return true;
    }

    static
    {
        standardConverterMap = new HashMap<>();

        cacheMiss = o -> o;

        /*
         * Conversions towards boolean
         */

        /* number -> boolean */

        Converter<Boolean> numberToBool = o -> Optional.ofNullable((Number)o).map(n -> Double.compare(n.doubleValue(), 0.0) != 0).orElse(null);
        Converter<Boolean> bigIntegerToBool = o -> Optional.ofNullable((BigInteger)o).map(bi -> bi.signum() != 0).orElse(null);
        Converter<Boolean> bigDecimalToBool = o -> Optional.ofNullable((BigDecimal)o).map(bi -> bi.signum() != 0).orElse(null);

        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, BYTE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, SHORT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, INTEGER_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, LONG_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, BIG_INTEGER_CLASS), bigIntegerToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, FLOAT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, DOUBLE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, BIG_DECIMAL_CLASS), bigDecimalToBool);
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
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, BIG_INTEGER_CLASS), bigIntegerToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, LONG_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, FLOAT_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, DOUBLE_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, BIG_DECIMAL_CLASS), bigDecimalToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, NUMBER_CLASS), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, BYTE_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, SHORT_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, INTEGER_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, LONG_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, FLOAT_TYPE), numberToBool);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, DOUBLE_TYPE), numberToBool);

        /* character -> boolean */

        Converter<Boolean> charToBoolean = o -> Optional.ofNullable((Character)o).map(c -> c != 0).orElse(null);

        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, CHARACTER_CLASS), charToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, CHARACTER_TYPE), charToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, CHARACTER_CLASS), charToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, CHARACTER_TYPE), charToBoolean);

        /* string -> boolean */

        Converter<Boolean> stringToBoolean = o -> Boolean.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(BOOLEAN_CLASS, STRING_CLASS), stringToBoolean);
        standardConverterMap.put(Pair.of(BOOLEAN_TYPE, STRING_CLASS), stringToBoolean);

        /*
         * Conversions towards byte
         */

        /* narrowing towards byte */

        Converter<Byte> narrowingToByte = o -> Optional.ofNullable((Number)o)
            .filter(n -> checkBounds(n, Byte.MIN_VALUE, Byte.MAX_VALUE))
            .map(Number::byteValue)
            .orElse(null);

        Converter<Byte> narrowingBigIntegerToByte = o -> Optional.ofNullable((BigInteger)o)
            .map(BigInteger::byteValueExact)
            .orElse(null);

        Converter<Byte> narrowingBigDecimalToByte = o -> Optional.ofNullable((BigDecimal)o)
            .map(BigDecimal::byteValueExact)
            .orElse(null);

        standardConverterMap.put(Pair.of(BYTE_CLASS, SHORT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, INTEGER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, LONG_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, BIG_INTEGER_CLASS), narrowingBigIntegerToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, FLOAT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, DOUBLE_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, BIG_DECIMAL_CLASS), narrowingBigDecimalToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, NUMBER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, SHORT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, INTEGER_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, LONG_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, FLOAT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, DOUBLE_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, SHORT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, INTEGER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, LONG_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, BIG_INTEGER_CLASS), narrowingBigIntegerToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, FLOAT_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, DOUBLE_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, BIG_DECIMAL_CLASS), narrowingBigDecimalToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, NUMBER_CLASS), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, SHORT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, INTEGER_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, LONG_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, FLOAT_TYPE), narrowingToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, DOUBLE_TYPE), narrowingToByte);

        /* string to byte */

        Converter<Byte> stringToByte = o -> Byte.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(BYTE_CLASS, STRING_CLASS), stringToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, STRING_CLASS), stringToByte);

        /*
         * Conversions towards short
         */

        /* narrowing towards short */

        Converter<Short> narrowingToShort = o -> Optional.ofNullable((Number)o)
            .filter(n -> checkBounds(n, Short.MIN_VALUE, Short.MAX_VALUE))
            .map(Number::shortValue)
            .orElse(null);

        Converter<Short> narrowingBigIntegerToShort = o -> Optional.ofNullable((BigInteger)o)
            .map(BigInteger::shortValueExact)
            .orElse(null);

        Converter<Short> narrowingBigDecimalToShort = o -> Optional.ofNullable((BigDecimal)o)
            .map(BigDecimal::shortValueExact)
            .orElse(null);

        standardConverterMap.put(Pair.of(SHORT_CLASS, INTEGER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, LONG_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, BIG_INTEGER_CLASS), narrowingBigIntegerToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, FLOAT_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, DOUBLE_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, BIG_DECIMAL_CLASS), narrowingBigDecimalToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, NUMBER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, INTEGER_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, LONG_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, FLOAT_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, DOUBLE_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, INTEGER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, LONG_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, BIG_INTEGER_CLASS), narrowingBigIntegerToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, FLOAT_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, DOUBLE_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, BIG_DECIMAL_CLASS), narrowingBigDecimalToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, NUMBER_CLASS), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, INTEGER_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, LONG_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, FLOAT_TYPE), narrowingToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, DOUBLE_TYPE), narrowingToShort);

        /* widening towards short */

        Converter<Short> wideningToShort = o -> Optional.ofNullable((Number)o)
            .map(Number::shortValue)
            .orElse(null);

        standardConverterMap.put(Pair.of(SHORT_CLASS, BYTE_CLASS), wideningToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, BYTE_TYPE), wideningToShort);

        /* string to short */

        Converter<Short> stringToShort = o -> Short.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(SHORT_CLASS, STRING_CLASS), stringToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, STRING_CLASS), stringToShort);

        /*
         * Conversions towards int
         */

        /* narrowing towards int */

        Converter<Integer> narrowingToInteger = o -> Optional.ofNullable((Number)o)
            .filter(n -> checkBounds(n, Integer.MIN_VALUE, Integer.MAX_VALUE))
            .map(Number::intValue)
            .orElse(null);

        Converter<Integer> narrowingBigIntegerToInteger = o -> Optional.ofNullable((BigInteger)o)
            .map(BigInteger::intValueExact)
            .orElse(null);

        Converter<Integer> narrowingBigDecimalToInteger = o -> Optional.ofNullable((BigDecimal)o)
            .map(BigDecimal::intValueExact)
            .orElse(null);

        standardConverterMap.put(Pair.of(INTEGER_CLASS, LONG_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, BIG_INTEGER_CLASS), narrowingBigIntegerToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, FLOAT_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, DOUBLE_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, BIG_DECIMAL_CLASS), narrowingBigDecimalToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, NUMBER_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, LONG_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, FLOAT_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, DOUBLE_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, LONG_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, BIG_INTEGER_CLASS), narrowingBigIntegerToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, FLOAT_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, DOUBLE_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, BIG_DECIMAL_CLASS), narrowingBigDecimalToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, NUMBER_CLASS), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, LONG_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, FLOAT_TYPE), narrowingToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, DOUBLE_TYPE), narrowingToInteger);

        /* widening towards int */

        Converter<Integer> wideningToInteger = o -> Optional.ofNullable((Number)o)
            .map(Number::intValue)
            .orElse(null);

        standardConverterMap.put(Pair.of(INTEGER_CLASS, BYTE_CLASS), wideningToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, SHORT_CLASS), wideningToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, BYTE_TYPE), wideningToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, SHORT_TYPE), wideningToInteger);

        /* string to int */

        Converter<Integer> stringToInteger = o -> Integer.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(INTEGER_CLASS, STRING_CLASS), stringToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, STRING_CLASS), stringToInteger);

        /*
         * Conversions towards long
         */

        /* narrowing towards long */

        Converter<Long> narrowingToLong = o -> Optional.ofNullable((Number)o)
            .filter(n -> checkBounds(n, Long.MIN_VALUE, Long.MAX_VALUE))
            .map(Number::longValue)
            .orElse(null);

        Converter<Long> narrowingBigIntegerToLong = o -> Optional.ofNullable((BigInteger)o)
            .map(BigInteger::longValueExact)
            .orElse(null);

        Converter<Long> narrowingBigDecimalToLong = o -> Optional.ofNullable((BigDecimal)o)
            .map(BigDecimal::longValueExact)
            .orElse(null);

        standardConverterMap.put(Pair.of(LONG_CLASS, BIG_INTEGER_CLASS), narrowingBigIntegerToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, FLOAT_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, DOUBLE_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, BIG_DECIMAL_CLASS), narrowingBigDecimalToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, NUMBER_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, FLOAT_TYPE), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, DOUBLE_TYPE), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, BIG_INTEGER_CLASS), narrowingBigIntegerToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, FLOAT_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, DOUBLE_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, BIG_DECIMAL_CLASS), narrowingBigDecimalToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, NUMBER_CLASS), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, FLOAT_TYPE), narrowingToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, DOUBLE_TYPE), narrowingToLong);

        /* widening towards long */

        Converter<Long> wideningToLong = o -> Optional.ofNullable((Number)o)
            .map(Number::longValue)
            .orElse(null);

        standardConverterMap.put(Pair.of(LONG_CLASS, BYTE_CLASS), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, SHORT_CLASS), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, INTEGER_CLASS), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, BYTE_TYPE), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, SHORT_TYPE), wideningToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, INTEGER_TYPE), wideningToLong);

        /* string to long */

        Converter<Long> stringToLong = o -> Long.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(LONG_CLASS, STRING_CLASS), stringToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, STRING_CLASS), stringToLong);

        /*
         * Conversions towards BigInteger
         */

        /* exact types towards BigInteger */

        Converter<BigInteger> toBigInteger = o -> Optional.ofNullable((Number)o)
            .map(n -> BigInteger.valueOf(n.longValue()))
            .orElse(null);

        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, BYTE_CLASS), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, SHORT_CLASS), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, INTEGER_CLASS), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, LONG_CLASS), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, BYTE_TYPE), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, SHORT_TYPE), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, INTEGER_TYPE), toBigInteger);
        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, LONG_TYPE), toBigInteger);
        
        /* approximate types towards BigInteger */

        /* It makes no sense trying to convert from float or double towards BigInteger
           if we do care about precision loss..
         */

        Converter<BigInteger> bigDecimalToBigInteger = o -> Optional.ofNullable((BigDecimal)o)
            .map(BigDecimal::toBigIntegerExact)
            .orElse(null);

        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, BIG_DECIMAL_CLASS), bigDecimalToBigInteger);

        /* string to BigInteger */

        Converter<BigInteger> stringToBigInteger = o -> Optional.ofNullable(o)
            .map(s -> new BigInteger(String.valueOf(s)))
            .orElse(null);

        standardConverterMap.put(Pair.of(BIG_INTEGER_CLASS, STRING_CLASS), stringToBigInteger);

        /*
         * Conversions towards float
         */
        
        Converter<Float> toFloat = o -> Optional.ofNullable((Number)o)
            .map(Number::floatValue)
            .orElse(null);

        /* narrowing towards float */

        standardConverterMap.put(Pair.of(FLOAT_CLASS, BIG_INTEGER_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, DOUBLE_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, BIG_DECIMAL_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, NUMBER_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, DOUBLE_TYPE), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, BIG_INTEGER_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, DOUBLE_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, BIG_DECIMAL_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, NUMBER_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, DOUBLE_TYPE), toFloat);

        /* exact types towards float */

        standardConverterMap.put(Pair.of(FLOAT_CLASS, BYTE_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, SHORT_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, INTEGER_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, LONG_CLASS), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, BYTE_TYPE), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, SHORT_TYPE), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, INTEGER_TYPE), toFloat);
        standardConverterMap.put(Pair.of(FLOAT_CLASS, LONG_TYPE), toFloat);

        /* string to float */

        Converter<Float> stringToFloat = o -> Float.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(FLOAT_CLASS, STRING_CLASS), stringToFloat);
        standardConverterMap.put(Pair.of(FLOAT_TYPE, STRING_CLASS), stringToFloat);

        /*
         * Conversions towards double
         */

        Converter<Double> toDouble = o -> Optional.ofNullable((Number)o)
            .map(Number::doubleValue)
            .orElse(null);

        /* narrowing towards double */

        standardConverterMap.put(Pair.of(DOUBLE_CLASS, BIG_INTEGER_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, BIG_DECIMAL_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_TYPE, BIG_INTEGER_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_TYPE, BIG_DECIMAL_CLASS), toDouble);
        
        /* exact types or widening towards double */

        standardConverterMap.put(Pair.of(DOUBLE_CLASS, BYTE_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, SHORT_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, INTEGER_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, LONG_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, FLOAT_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, NUMBER_CLASS), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, BYTE_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, SHORT_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, INTEGER_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, LONG_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_CLASS, FLOAT_TYPE), toDouble);
        standardConverterMap.put(Pair.of(DOUBLE_TYPE, NUMBER_CLASS), toDouble);

        /* string to double */

        Converter<Double> stringToDouble = o -> Double.valueOf(String.valueOf(o));

        standardConverterMap.put(Pair.of(DOUBLE_CLASS, STRING_CLASS), stringToDouble);
        standardConverterMap.put(Pair.of(DOUBLE_TYPE, STRING_CLASS), stringToDouble);

        /*
         * Conversions towards BigDecimal
         */

        /* exact types towards BigDecimal */

        Converter<BigDecimal> exactToBigDecimal = o -> Optional.ofNullable((Number)o)
            .map(n -> BigDecimal.valueOf(n.longValue()))
            .orElse(null);

        Converter<BigDecimal> bigIntegerToBigDecimal = o -> Optional.ofNullable((BigInteger)o)
            .map(bi -> new BigDecimal(bi))
            .orElse(null);

        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, BYTE_CLASS), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, SHORT_CLASS), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, INTEGER_CLASS), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, LONG_CLASS), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, BIG_INTEGER_CLASS), bigIntegerToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, BYTE_TYPE), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, SHORT_TYPE), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, INTEGER_TYPE), exactToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, LONG_TYPE), exactToBigDecimal);

        /* approximate types towards BigDecimal */

        Converter<BigDecimal> approxToBigDecimal = o -> Optional.ofNullable((Number)o)
            .map(n -> BigDecimal.valueOf(n.doubleValue()))
            .orElse(null);

        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, FLOAT_CLASS), approxToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, DOUBLE_CLASS), approxToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, FLOAT_TYPE), approxToBigDecimal);
        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, DOUBLE_TYPE), approxToBigDecimal);

        /* string to BigDecimal */

        Converter<BigDecimal> stringToBigDecimal = o -> Optional.ofNullable(o)
            .map(s -> new BigDecimal(String.valueOf(s)))
            .orElse(null);

        standardConverterMap.put(Pair.of(BIG_DECIMAL_CLASS, STRING_CLASS), stringToBigDecimal);

        /*
         * Conversions from boolean to numeric type
         */

        /* boolean to byte */

        Converter<Byte> booleanToByte = o -> Optional.ofNullable((Boolean)o)
            .map(b -> b ? (byte)1 : (byte)0)
            .orElse(null);

        standardConverterMap.put(Pair.of(BYTE_CLASS, BOOLEAN_CLASS), booleanToByte);
        standardConverterMap.put(Pair.of(BYTE_CLASS, BOOLEAN_TYPE), booleanToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, BOOLEAN_CLASS), booleanToByte);
        standardConverterMap.put(Pair.of(BYTE_TYPE, BOOLEAN_TYPE), booleanToByte);

        /* boolean to short */

        Converter<Short> booleanToShort = o -> Optional.ofNullable((Boolean)o)
            .map(b -> b ? (short)1 : (short)0)
            .orElse(null);

        standardConverterMap.put(Pair.of(SHORT_CLASS, BOOLEAN_CLASS), booleanToShort);
        standardConverterMap.put(Pair.of(SHORT_CLASS, BOOLEAN_TYPE), booleanToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, BOOLEAN_CLASS), booleanToShort);
        standardConverterMap.put(Pair.of(SHORT_TYPE, BOOLEAN_TYPE), booleanToShort);

        /* boolean to integer */

        Converter<Integer> booleanToInteger = o -> Optional.ofNullable((Boolean)o)
            .map(b -> b ? (int)1 : (int)0)
            .orElse(null);

        standardConverterMap.put(Pair.of(INTEGER_CLASS, BOOLEAN_CLASS), booleanToInteger);
        standardConverterMap.put(Pair.of(INTEGER_CLASS, BOOLEAN_TYPE), booleanToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, BOOLEAN_CLASS), booleanToInteger);
        standardConverterMap.put(Pair.of(INTEGER_TYPE, BOOLEAN_TYPE), booleanToInteger);

        /* boolean to long */

        Converter<Long> booleanToLong = o -> Optional.ofNullable((Boolean)o)
            .map(b -> b ? 1l : 0l)
            .orElse(null);

        standardConverterMap.put(Pair.of(LONG_CLASS, BOOLEAN_CLASS), booleanToLong);
        standardConverterMap.put(Pair.of(LONG_CLASS, BOOLEAN_TYPE), booleanToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, BOOLEAN_CLASS), booleanToLong);
        standardConverterMap.put(Pair.of(LONG_TYPE, BOOLEAN_TYPE), booleanToLong);

        /* to string */

        toString = o -> String.valueOf(o);

        /* string to locale */
        Converter<Locale> stringToLocale = o -> Optional.ofNullable(o)
            .map(l -> LocaleUtils.toLocale(String.valueOf(l)))
            .orElse(null);

        standardConverterMap.put(Pair.of(LOCALE_CLASS, STRING_CLASS), stringToLocale);
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
    public boolean isExplicitlyConvertible(Type formal, Class<?> actual, boolean possibleVarArg)
    {
        /*
         * for consistency, we also have to check standard implicit convertibility
         * since it may not have been checked before by the calling code
         */
        Class<?> formalClass = IntrospectionUtils.getTypeClass(formal);
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
    public Converter<?> getNeededConverter(Type formal, Class<?> actual)
    {
        if (actual == null)
        {
            return null;
        }
        Pair<String, String> key = Pair.of(formal.getTypeName(), actual.getTypeName());

        /* first check for a standard conversion */
        Converter<?> converter = standardConverterMap.get(key);
        if (converter == null)
        {
            /* then the converters cache map */
            converter = converterCacheMap.get(key);
            if (converter == null)
            {
                Class<?> formalClass = IntrospectionUtils.getTypeClass(formal);
                /* check for conversion towards string */
                if (formal == String.class)
                {
                    converter = toString;
                }
                /* check for String -> Enum constant conversion */
                else if (formalClass != null && formalClass.isEnum() && actual == String.class)
                {
                    final Class<Enum> enumClass = (Class<Enum>)formalClass;
                    converter = o -> Enum.valueOf(enumClass, (String)o);
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
    public void addConverter(Type formal, Class<?> actual, Converter<?> converter)
    {
        Pair<String, String> key = Pair.of(formal.getTypeName(), actual.getTypeName());
        converterCacheMap.put(key, converter);
        Class<?> formalClass = IntrospectionUtils.getTypeClass(formal);
        if (formalClass != null)
        {
            if (formalClass.isPrimitive())
            {
                key = Pair.of(IntrospectionUtils.getBoxedClass(formalClass).getTypeName(), actual.getTypeName());
                converterCacheMap.put(key, converter);
            }
            else
            {
                Class<?> unboxedFormal = IntrospectionUtils.getUnboxedClass(formalClass);
                if (unboxedFormal != formalClass)
                {
                    key = Pair.of(unboxedFormal.getTypeName(), actual.getTypeName());
                    converterCacheMap.put(key, converter);
                }
            }
        }
    }
}
