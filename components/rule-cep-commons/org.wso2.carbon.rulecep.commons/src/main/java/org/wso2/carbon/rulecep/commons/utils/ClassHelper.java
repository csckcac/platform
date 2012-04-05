/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.commons.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related with Java classes such as load class, create a instance of a class and
 * check the type of a given class name
 */
public class ClassHelper {

    private static Log log = LogFactory.getLog(ClassHelper.class);

    private final static List<String> WRAPPER_TYPES = new ArrayList<String>();

    public ClassHelper() {
    }

    static {
        WRAPPER_TYPES.add(Boolean.class.getName());
        WRAPPER_TYPES.add(Character.class.getName());
        WRAPPER_TYPES.add(Byte.class.getName());
        WRAPPER_TYPES.add(Short.class.getName());
        WRAPPER_TYPES.add(Integer.class.getName());
        WRAPPER_TYPES.add(Long.class.getName());
        WRAPPER_TYPES.add(Float.class.getName());
        WRAPPER_TYPES.add(Double.class.getName());
        WRAPPER_TYPES.add(Void.class.getName());
        WRAPPER_TYPES.add(String.class.getName());
    }

    /**
     * Load a class with the given name from the default class loader (i.e. ContextClassLoader)
     *
     * @param className Name of the class to be loaded
     * @return <code>Class</code> object of for the given class name
     */
    public static Class loadAClass(String className) {
        return loadAClass(className, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Load a class with the given name from given class loader. If the given class loader is null,
     * then uses the default class loader
     *
     * @param className   the name of class loader
     * @param classLoader class loader to be used to locate the given class
     * @return <code>Class</code> object of for the given class name
     */
    public static Class loadAClass(String className, ClassLoader classLoader) {

        if (className == null || "".equals(className)) {
            return null;
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            return classLoader.loadClass(className);

        } catch (ClassNotFoundException e) {
            throw new LoggedRuntimeException("The class with name '" + className +
                    " ' cannot found " + e, log);

        }
    }

    /**
     * Create an instance of the given class with the given class name
     * This method uses the default class loader (i.e. ContextClassLoader)
     *
     * @param className the name of the class to be loaded and used to create an instance
     * @return an instance of the class with the given class name
     */
    public static Object createInstance(String className) {
        return createInstance(className, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Create an instance of the given class with the given class name.
     * if the given class loader is null, then uses the default class loader
     *
     * @param className   the name of the class to be loaded and used to create an instance
     * @param classLoader class loader to be used to locate the given class
     * @return an instance of the class with the given class name
     */
    public static Object createInstance(String className, ClassLoader classLoader) {
        Class aClass = loadAClass(className, classLoader);
        if (aClass == null) {
            return null;
        }
        try {
            return aClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new LoggedRuntimeException("Error when initiating new class" +
                    " instance with name " +
                    "' " + className + " '", e, log);
        } catch (InstantiationException e) {
            throw new LoggedRuntimeException("Error when initiating new class " +
                    "instance with name " +
                    "' " + className + " '", e, log);
        }
    }

    /**
     * Checks the type of the class to see whether it is a basic type
     *
     * @param className the name of the class
     * @return <code>true</code> if the given class name is a name of a basic type
     */
    public static boolean isWrapperType(String className) {
        return WRAPPER_TYPES.contains(className);
    }

    /**
     * Creates wrappers types based on given string value and class name
     * This methods uses 'valueOf' method of wrapper classes to create an instance
     *
     * @param className the name of the class to be used to create an instance
     * @param value     the string value to be used to create the instance
     * @return an instance of the wrapper class with the given class name
     */
    public static Object createWrapperTypeInstance(String className, String value) {

        if (String.class.getName().equals(className)) {
            return value;
        } else {
            Class aClass = loadAClass(className);
            if (aClass == null) {
                throw new LoggedRuntimeException("Cannot find a class with name : " + className,
                        log);
            }
            String mName = "valueOf";
            try {
                Method method = aClass.getMethod(mName, value.getClass());
                if (log.isDebugEnabled()) {
                    log.debug("Invoking method "
                            + mName + "(" + value + ")");
                }
                return method.invoke(null, value);

            } catch (InvocationTargetException e) {
                throw new LoggedRuntimeException("Error Invoking method : " + mName + "into " +
                        className + " with parameter " + value + ": " + e.getMessage(), e, log);
            } catch (NoSuchMethodException e) {
                throw new LoggedRuntimeException("Error locating a method : " + mName + " from " +
                        className + " with parameter " + value + ": " + e.getMessage(), e, log);
            } catch (IllegalAccessException e) {
                throw new LoggedRuntimeException("Error Invoking method : " + mName + "into " +
                        className + " with parameter " + value + ": " + e.getMessage(), e, log);
            }
        }
    }

}
