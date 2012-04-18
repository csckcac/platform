/**********************************************************************
Copyright (c) 2005 Erik Bengtson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.datanucleus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.datanucleus.identity.OIDImpl;

/**
 * Constants with classes names (class created to reduce overhead on calling Class.class.getName() *performance*  
 * Make sure this class is initialized when the PMF is initialized.
 */
public class ClassNameConstants
{
    /** Object **/
    public static final String Object = Object.class.getName();
    /** Serializable **/
    public static final String Serializable = Serializable.class.getName();

    /** primitive boolean. **/
    public static final String BOOLEAN = boolean.class.getName();
    /** primitive byte. **/
    public static final String BYTE = byte.class.getName();
    /** primitive char. **/
    public static final String CHAR = char.class.getName();
    /** primitive double. **/
    public static final String DOUBLE = double.class.getName();
    /** primitive float. **/
    public static final String FLOAT = float.class.getName();
    /** primitive int. **/
    public static final String INT = int.class.getName();
    /** primitive long. **/
    public static final String LONG = long.class.getName();
    /** primitive short. **/
    public static final String SHORT = short.class.getName();

    /** primitive boolean[]. **/
    public static final String BOOLEAN_ARRAY = boolean[].class.getName();
    /** primitive byte[]. **/
    public static final String BYTE_ARRAY = byte[].class.getName();
    /** primitive char[]. **/
    public static final String CHAR_ARRAY = char[].class.getName();
    /** primitive double[]. **/
    public static final String DOUBLE_ARRAY = double[].class.getName();
    /** primitive float[]. **/
    public static final String FLOAT_ARRAY = float[].class.getName();
    /** primitive int[]. **/
    public static final String INT_ARRAY = int[].class.getName();
    /** primitive long[]. **/
    public static final String LONG_ARRAY = long[].class.getName();
    /** primitive short[]. **/
    public static final String SHORT_ARRAY = short[].class.getName();

    /** java.lang.Boolean **/
    public static final String JAVA_LANG_BOOLEAN = Boolean.class.getName();
    /** java.lang.Byte **/
    public static final String JAVA_LANG_BYTE = Byte.class.getName();
    /** java.lang.Character **/
    public static final String JAVA_LANG_CHARACTER = Character.class.getName();
    /** java.lang.Double **/
    public static final String JAVA_LANG_DOUBLE = Double.class.getName();
    /** java.lang.Float **/
    public static final String JAVA_LANG_FLOAT = Float.class.getName();
    /** java.lang.Integer **/
    public static final String JAVA_LANG_INTEGER = Integer.class.getName();
    /** java.lang.Long **/
    public static final String JAVA_LANG_LONG = Long.class.getName();
    /** java.lang.Short **/
    public static final String JAVA_LANG_SHORT = Short.class.getName();

    /** java.lang.Boolean[] **/
    public static final String JAVA_LANG_BOOLEAN_ARRAY = Boolean[].class.getName();
    /** java.lang.Byte[] **/
    public static final String JAVA_LANG_BYTE_ARRAY= Byte[].class.getName();
    /** java.lang.Character[] **/
    public static final String JAVA_LANG_CHARACTER_ARRAY = Character[].class.getName();
    /** java.lang.Double[] **/
    public static final String JAVA_LANG_DOUBLE_ARRAY = Double[].class.getName();
    /** java.lang.Float[] **/
    public static final String JAVA_LANG_FLOAT_ARRAY = Float[].class.getName();
    /** java.lang.Integer[] **/
    public static final String JAVA_LANG_INTEGER_ARRAY = Integer[].class.getName();
    /** java.lang.Long[] **/
    public static final String JAVA_LANG_LONG_ARRAY = Long[].class.getName();
    /** java.lang.Short[] **/
    public static final String JAVA_LANG_SHORT_ARRAY = Short[].class.getName();

    /** java.lang.String **/
    public static final String JAVA_LANG_STRING = String.class.getName();
    /** java.math.BigDecimal **/
    public static final String JAVA_MATH_BIGDECIMAL = BigDecimal.class.getName();
    /** java.math.BigInteger **/
    public static final String JAVA_MATH_BIGINTEGER = BigInteger.class.getName();
    /** java.sql.Date **/
    public static final String JAVA_SQL_DATE = java.sql.Date.class.getName();
    /** java.sql.Time **/
    public static final String JAVA_SQL_TIME = java.sql.Time.class.getName();
    /** java.sql.Timestamp **/
    public static final String JAVA_SQL_TIMESTAMP = java.sql.Timestamp.class.getName();
    /** java.util.Date **/
    public static final String JAVA_UTIL_DATE = java.util.Date.class.getName();
    /** java.io.Serializable **/
    public static final String JAVA_IO_SERIALIZABLE = java.io.Serializable.class.getName();

    /** OIDImpl **/
    public static final String OIDImpl = OIDImpl.class.getName();
}