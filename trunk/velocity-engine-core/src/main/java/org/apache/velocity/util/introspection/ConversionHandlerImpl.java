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

import org.apache.velocity.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A conversion handler adds admissible conversions between Java types whenever Velocity introspection has to map
 * VTL methods and property accessors to Java methods. This implementation is the default Conversion Handler
 * for Velocity.
 *
 * @author <a href="mailto:claude.brisson@gmail.com">Claude Brisson</a>
 * @version $Id: ConversionHandlerImpl.java $
 * @since 2.0
 */

public class ConversionHandlerImpl implements ConversionHandler
{
    /**
     * standard narrowing and string parsing conversions.
     */
    static Map<Pair<? extends Class, ? extends Class>, Converter> standardConverterMap;

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
    Map<Pair<? extends Class, ? extends Class>, Converter> converterCacheMap;

    static
    {
        standardConverterMap = new HashMap<Pair<? extends Class, ? extends Class>, Converter>();

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
        standardConverterMap.put(new Pair<>(Boolean.class, Byte.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Short.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Integer.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Long.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Float.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Double.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Byte.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Short.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Integer.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Long.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Float.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.class, Double.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Byte.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Short.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Integer.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Long.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Float.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Double.class), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Byte.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Short.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Integer.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Long.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Float.TYPE), numberToBool);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Double.TYPE), numberToBool);

        /* character -> boolean */
        Converter<Boolean> charToBoolean = new Converter<Boolean>()
        {
            @Override
            public Boolean convert(Object o)
            {
                return o == null ? null : ((Character) o).charValue() != 0;
            }
        };
        standardConverterMap.put(new Pair<>(Boolean.class, Character.class), charToBoolean);
        standardConverterMap.put(new Pair<>(Boolean.class, Character.TYPE), charToBoolean);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Character.class), charToBoolean);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, Character.TYPE), charToBoolean);

        /* string -> boolean */
        Converter<Boolean> stringToBoolean = new Converter<Boolean>()
        {
            @Override
            public Boolean convert(Object o)
            {
                return Boolean.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Boolean.class, String.class), stringToBoolean);
        standardConverterMap.put(new Pair<>(Boolean.TYPE, String.class), stringToBoolean);

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
        standardConverterMap.put(new Pair<>(Byte.class, Short.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Integer.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Long.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Float.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Double.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Short.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Integer.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Long.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Float.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Double.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Short.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Integer.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Long.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Float.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Double.class), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Short.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Integer.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Long.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Float.TYPE), narrowingToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Double.TYPE), narrowingToByte);

        /* string to byte */
        Converter<Byte> stringToByte = new Converter<Byte>()
        {
            @Override
            public Byte convert(Object o)
            {
                return Byte.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Byte.class, String.class), stringToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, String.class), stringToByte);

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
        standardConverterMap.put(new Pair<>(Short.class, Integer.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Long.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Float.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Double.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Integer.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Long.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Float.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.class, Double.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Integer.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Long.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Float.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Double.class), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Integer.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Long.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Float.TYPE), narrowingToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Double.TYPE), narrowingToShort);

        /* string to short */
        Converter<Short> stringToShort = new Converter<Short>()
        {
            @Override
            public Short convert(Object o)
            {
                return Short.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Short.class, String.class), stringToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, String.class), stringToShort);

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
        standardConverterMap.put(new Pair<>(Integer.class, Long.class), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Float.class), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Double.class), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Long.TYPE), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Float.TYPE), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Double.TYPE), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Long.class), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Float.class), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Double.class), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Long.TYPE), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Float.TYPE), narrowingToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Double.TYPE), narrowingToInteger);

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
        standardConverterMap.put(new Pair<>(Integer.class, Short.class), wideningToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Short.TYPE), wideningToInteger);

        /* string to int */
        Converter<Integer> stringToInteger = new Converter<Integer>()
        {
            @Override
            public Integer convert(Object o)
            {
                return Integer.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Integer.class, String.class), stringToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, String.class), stringToInteger);
        
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
        standardConverterMap.put(new Pair<>(Long.class, Float.class), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.class, Double.class), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.class, Float.TYPE), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.class, Double.TYPE), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, Float.class), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, Double.class), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, Float.TYPE), narrowingToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, Double.TYPE), narrowingToLong);

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
        standardConverterMap.put(new Pair<>(Long.class, Short.class), wideningToLong);
        standardConverterMap.put(new Pair<>(Long.class, Integer.class), wideningToLong);
        standardConverterMap.put(new Pair<>(Long.class, Short.TYPE), wideningToLong);
        standardConverterMap.put(new Pair<>(Long.class, Integer.TYPE), wideningToLong);

        /* string to long */
        Converter<Long> stringToLong = new Converter<Long>()
        {
            @Override
            public Long convert(Object o)
            {
                return Long.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Long.class, String.class), stringToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, String.class), stringToLong);
        
        /* narrowing towards float */
        Converter<Float> narrowingToFloat = new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                return o == null ? null : ((Number) o).floatValue();
            }
        };
        standardConverterMap.put(new Pair<>(Float.class, Double.class), narrowingToFloat);
        standardConverterMap.put(new Pair<>(Float.class, Double.TYPE), narrowingToFloat);
        standardConverterMap.put(new Pair<>(Float.TYPE, Double.class), narrowingToFloat);
        standardConverterMap.put(new Pair<>(Float.TYPE, Double.TYPE), narrowingToFloat);

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
        standardConverterMap.put(new Pair<>(Float.class, Short.class), toFloat);
        standardConverterMap.put(new Pair<>(Float.class, Integer.class), toFloat);
        standardConverterMap.put(new Pair<>(Float.class, Long.class), toFloat);
        standardConverterMap.put(new Pair<>(Float.class, Short.TYPE), toFloat);
        standardConverterMap.put(new Pair<>(Float.class, Integer.TYPE), toFloat);
        standardConverterMap.put(new Pair<>(Float.class, Long.TYPE), toFloat);

        /* string to float */
        Converter<Float> stringToFloat = new Converter<Float>()
        {
            @Override
            public Float convert(Object o)
            {
                return Float.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Float.class, String.class), stringToFloat);
        standardConverterMap.put(new Pair<>(Float.TYPE, String.class), stringToFloat);

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
        standardConverterMap.put(new Pair<>(Double.class, Short.class), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Integer.class), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Long.class), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Float.class), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Short.TYPE), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Integer.TYPE), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Long.TYPE), toDouble);
        standardConverterMap.put(new Pair<>(Double.class, Float.TYPE), toDouble);

        /* string to double */
        Converter<Double> stringToDouble = new Converter<Double>()
        {
            @Override
            public Double convert(Object o)
            {
                return Double.valueOf(String.valueOf(o));
            }
        };
        standardConverterMap.put(new Pair<>(Double.class, String.class), stringToDouble);
        standardConverterMap.put(new Pair<>(Double.TYPE, String.class), stringToDouble);

        /* boolean to byte */
        Converter<Byte> booleanToByte = new Converter<Byte>()
        {
            @Override
            public Byte convert(Object o)
            {
                return o == null ? null : ((Boolean)o).booleanValue() ? (byte)1 : (byte)0;
            }
        };
        standardConverterMap.put(new Pair<>(Byte.class, Boolean.class), booleanToByte);
        standardConverterMap.put(new Pair<>(Byte.class, Boolean.TYPE), booleanToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Boolean.class), booleanToByte);
        standardConverterMap.put(new Pair<>(Byte.TYPE, Boolean.TYPE), booleanToByte);

        /* boolean to short */
        Converter<Short> booleanToShort = new Converter<Short>()
        {
            @Override
            public Short convert(Object o)
            {
                return o == null ? null : ((Boolean)o).booleanValue() ? (short)1 : (short)0;
            }
        };
        standardConverterMap.put(new Pair<>(Short.class, Boolean.class), booleanToShort);
        standardConverterMap.put(new Pair<>(Short.class, Boolean.TYPE), booleanToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Boolean.class), booleanToShort);
        standardConverterMap.put(new Pair<>(Short.TYPE, Boolean.TYPE), booleanToShort);

        /* boolean to integer */
        Converter<Integer> booleanToInteger = new Converter<Integer>()
        {
            @Override
            public Integer convert(Object o)
            {
                return o == null ? null : ((Boolean)o).booleanValue() ? (Integer)1 : (Integer)0;
            }
        };
        standardConverterMap.put(new Pair<>(Integer.class, Boolean.class), booleanToInteger);
        standardConverterMap.put(new Pair<>(Integer.class, Boolean.TYPE), booleanToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Boolean.class), booleanToInteger);
        standardConverterMap.put(new Pair<>(Integer.TYPE, Boolean.TYPE), booleanToInteger);

        /* boolean to lonf */
        Converter<Long> booleanToLong = new Converter<Long>()
        {
            @Override
            public Long convert(Object o)
            {
                return o == null ? null : ((Boolean)o).booleanValue() ? 1L : 0L;
            }
        };
        standardConverterMap.put(new Pair<>(Long.class, Boolean.class), booleanToLong);
        standardConverterMap.put(new Pair<>(Long.class, Boolean.TYPE), booleanToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, Boolean.class), booleanToLong);
        standardConverterMap.put(new Pair<>(Long.TYPE, Boolean.TYPE), booleanToLong);
        
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
    public ConversionHandlerImpl()
    {
        converterCacheMap = new ConcurrentHashMap<Pair<? extends Class, ? extends Class>, Converter>();
    }

    /**
     * Check to see if the conversion can be done using an explicit conversion
     * @param actual found argument type
     * @param formal expected formal type
     * @return null if no conversion is needed, or the appropriate Converter object
     * @since 2.0
     */
    @Override
    public boolean isExplicitlyConvertible(Class formal, Class actual, boolean possibleVarArg)
    {
        if (formal == actual || getNeededConverter(formal, actual) != null)
        {
            return true;
        }

        /* Check var arg */
        if (possibleVarArg && formal.isArray())
        {
            if (actual.isArray())
            {
                actual = actual.getComponentType();
            }
            return isExplicitlyConvertible(formal.getComponentType(), actual, false);
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
     * @since 2.0
     */
    @Override
    public Converter getNeededConverter(final Class formal, final Class actual)
    {
        Pair<Class, Class> key = new Pair<>(formal, actual);

        /* first check for a standard conversion */
        Converter converter = standardConverterMap.get(key);
        if (converter == null)
        {
            /* then the converters cache map */
            converter = converterCacheMap.get(key);
            if (converter == null)
            {
                /* check for conversion towards string */
                if (formal == String.class)
                {
                    converter = toString;
                }
                /* check for String -> Enum constant conversion */
                else if (formal.isEnum() && actual == String.class)
                {
                    converter = new Converter()
                    {
                        @Override
                        public Object convert(Object o)
                        {
                            return Enum.valueOf((Class<Enum>) formal, (String) o);
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
     * @since 2.0
     */
    @Override
    public void addConverter(Class formal, Class actual, Converter converter)
    {
        Pair<Class, Class> key = new Pair<>(formal, actual);
        converterCacheMap.put(key, converter);
        if (formal.isPrimitive())
        {
            key = new Pair<>(IntrospectionUtils.getBoxedClass(formal), actual);
            converterCacheMap.put(key, converter);
        }
        else
        {
            Class unboxedFormal = IntrospectionUtils.getUnboxedClass(formal);
            if (unboxedFormal != formal)
            {
                key = new Pair<>(unboxedFormal, actual);
                converterCacheMap.put(key, converter);
            }
        }
    }
}
